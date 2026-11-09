package com.example.trackio.data.repository

import com.example.trackio.data.local.AppDatabase
import com.example.trackio.data.local.entity.CategoryEntity
import com.example.trackio.data.local.entity.FixedItemEntity
import com.example.trackio.data.local.entity.SettingsEntity
import com.example.trackio.data.local.entity.TransactionDirection
import com.example.trackio.data.local.entity.TransactionEntity
import com.example.trackio.data.local.entity.TransactionSource
import com.example.trackio.data.local.entity.TransactionStatus
import com.example.trackio.data.local.entity.VendorEntity
import com.example.trackio.domain.SafeDailySpendCalculator
import com.example.trackio.domain.SafeSpendCalculationResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

data class DashboardState(
    val calculation: SafeSpendCalculationResult,
    val recentTransactions: List<TransactionEntity>,
    val needsReviewCount: Int,
    val settings: SettingsEntity
)

class TrackioRepository(private val database: AppDatabase) {

    private val transactionDao = database.transactionDao()
    private val categoryDao = database.categoryDao()
    private val vendorDao = database.vendorDao()
    private val fixedItemDao = database.fixedItemDao()
    private val settingsDao = database.settingsDao()
    private val projectDao = database.projectDao()
    private val wishlistDao = database.wishlistDao()

    fun getDashboardStateFlow(): Flow<DashboardState> {
        val now = System.currentTimeMillis()
        val startOfToday = SafeDailySpendCalculator.getStartOfToday(now)
        val endOfToday = SafeDailySpendCalculator.getEndOfToday(now)

        return combine(
            settingsDao.getSettingsFlow(),
            fixedItemDao.getActiveFixedItems(),
            transactionDao.getAllTransactions(),
            transactionDao.getNeedsReviewCount()
        ) { settingsNullable, fixedItems, allTransactions, needsReviewCount ->
            val settings = settingsNullable ?: SettingsEntity()
            val cycleRange = SafeDailySpendCalculator.computeCycleRange(settings, now)

            // Transactions in this cycle
            val cycleTransactions = allTransactions.filter {
                it.timestamp in cycleRange.startTimestamp..cycleRange.endTimestamp &&
                        it.direction == TransactionDirection.PAID &&
                        it.status == TransactionStatus.CONFIRMED
            }

            val spentSoFar = cycleTransactions.sumOf { it.amount }

            // Transactions today
            val todayTransactions = allTransactions.filter {
                it.timestamp in startOfToday..endOfToday &&
                        it.direction == TransactionDirection.PAID &&
                        it.status == TransactionStatus.CONFIRMED
            }
            val todaySpent = todayTransactions.sumOf { it.amount }

            val upcomingFixed = SafeDailySpendCalculator.calculateUpcomingFixedItems(
                fixedItems,
                cycleRange,
                now
            )

            val calculation = SafeDailySpendCalculator.calculate(
                allowance = settings.allowanceAmount,
                spentSoFarInCycle = spentSoFar,
                upcomingFixedItems = upcomingFixed,
                todaySpent = todaySpent,
                daysLeftInCycle = cycleRange.daysLeft,
                daysTotalInCycle = cycleRange.daysTotal
            )

            DashboardState(
                calculation = calculation,
                recentTransactions = allTransactions.take(20),
                needsReviewCount = needsReviewCount,
                settings = settings
            )
        }
    }

    suspend fun quickAddTransaction(
        amount: Double,
        vendorName: String,
        categoryId: Long? = null,
        tag: String? = null,
        upiRef: String? = null,
        source: TransactionSource = TransactionSource.MANUAL,
        status: TransactionStatus = TransactionStatus.CONFIRMED
    ): Long {
        // Resolve vendor and default category if not provided
        var resolvedCategoryId = categoryId
        val existingVendor = vendorDao.findVendorByName(vendorName)

        if (existingVendor != null) {
            if (resolvedCategoryId == null) {
                resolvedCategoryId = existingVendor.defaultCategoryId
            } else if (existingVendor.defaultCategoryId != resolvedCategoryId) {
                // User explicitly selected a different category -> remember for next time!
                vendorDao.updateVendor(existingVendor.copy(defaultCategoryId = resolvedCategoryId))
            }
        } else if (vendorName.isNotBlank()) {
            // New vendor, save with default category
            vendorDao.insertVendor(
                VendorEntity(
                    name = vendorName.trim(),
                    defaultCategoryId = resolvedCategoryId
                )
            )
        }

        // De-duplication check: if UPI ref already exists, do not duplicate
        if (!upiRef.isNullOrBlank()) {
            val existing = transactionDao.findByUpiRef(upiRef)
            if (existing != null) {
                return existing.id
            }
        }

        val transaction = TransactionEntity(
            amount = amount,
            direction = TransactionDirection.PAID,
            vendorName = vendorName.ifBlank { "Cash / Expense" },
            categoryId = resolvedCategoryId,
            tag = tag?.trim()?.ifBlank { null },
            timestamp = System.currentTimeMillis(),
            upiRef = upiRef,
            source = source,
            status = status
        )

        return transactionDao.insertTransaction(transaction)
    }

    suspend fun updateTransaction(transaction: TransactionEntity) {
        transactionDao.updateTransaction(transaction)
    }

    suspend fun deleteTransaction(transaction: TransactionEntity) {
        transactionDao.deleteTransaction(transaction)
    }

    fun getAllCategories(): Flow<List<CategoryEntity>> = categoryDao.getAllCategories()

    suspend fun addCategory(name: String, iconName: String, colorHex: String): Long {
        return categoryDao.insertCategory(
            CategoryEntity(
                name = name.trim(),
                iconName = iconName,
                isDefault = false,
                colorHex = colorHex
            )
        )
    }

    fun getAllFixedItems(): Flow<List<FixedItemEntity>> = fixedItemDao.getAllFixedItems()

    suspend fun addFixedItem(name: String, amount: Double, dueDay: Int): Long {
        return fixedItemDao.insertFixedItem(
            FixedItemEntity(
                name = name.trim(),
                amount = amount,
                dueDayOfMonth = dueDay.coerceIn(1, 31)
            )
        )
    }

    suspend fun updateFixedItem(item: FixedItemEntity) {
        fixedItemDao.updateFixedItem(item)
    }

    suspend fun deleteFixedItem(item: FixedItemEntity) {
        fixedItemDao.deleteFixedItem(item)
    }

    fun getSettingsFlow(): Flow<SettingsEntity?> = settingsDao.getSettingsFlow()

    suspend fun updateSettings(settings: SettingsEntity) {
        settingsDao.insertOrUpdate(settings)
    }

    suspend fun autocompleteVendors(query: String): List<String> {
        return vendorDao.autocompleteVendorNames(query)
    }

    fun getNeedsReviewTransactions(): Flow<List<TransactionEntity>> =
        transactionDao.getNeedsReviewTransactions()

    // Project Budgets
    fun getAllProjects(): Flow<List<com.example.trackio.data.local.entity.ProjectEntity>> = projectDao.getAllProjects()

    suspend fun addProject(name: String, capAmount: Double?): Long {
        return projectDao.insertProject(
            com.example.trackio.data.local.entity.ProjectEntity(
                name = name.trim().removePrefix("#"),
                capAmount = capAmount
            )
        )
    }

    suspend fun deleteProject(project: com.example.trackio.data.local.entity.ProjectEntity) {
        projectDao.deleteProject(project)
    }

    // Wishlist
    fun getAllWishlistItems(): Flow<com.example.trackio.data.local.entity.WishlistItemEntity> = wishlistDao.getAllWishlistItems().map { it.firstOrNull() ?: com.example.trackio.data.local.entity.WishlistItemEntity(name = "", targetPrice = 0.0) }
    fun observeWishlist(): Flow<List<com.example.trackio.data.local.entity.WishlistItemEntity>> = wishlistDao.getAllWishlistItems()

    suspend fun addWishlistItem(name: String, targetPrice: Double, notesOrUrl: String? = null): Long {
        return wishlistDao.insertItem(
            com.example.trackio.data.local.entity.WishlistItemEntity(
                name = name.trim(),
                targetPrice = targetPrice,
                notesOrUrl = notesOrUrl?.trim()?.ifBlank { null }
            )
        )
    }

    suspend fun toggleWishlistPurchased(item: com.example.trackio.data.local.entity.WishlistItemEntity) {
        wishlistDao.updateItem(item.copy(isPurchased = !item.isPurchased))
    }

    suspend fun deleteWishlistItem(item: com.example.trackio.data.local.entity.WishlistItemEntity) {
        wishlistDao.deleteItem(item)
    }

    suspend fun exportToCsv(): String {
        val all = transactionDao.getAllTransactions().firstOrNull() ?: emptyList()
        val sb = StringBuilder()
        sb.append("id,amount,direction,vendor,tag,timestamp,upiRef,source,status\n")
        all.forEach { t ->
            sb.append("${t.id},${t.amount},${t.direction},\"${t.vendorName}\",\"${t.tag ?: ""}\",${t.timestamp},\"${t.upiRef ?: ""}\",${t.source},${t.status}\n")
        }
        return sb.toString()
    }

    suspend fun deleteAllUserData() {
        transactionDao.deleteAllTransactions()
        fixedItemDao.deleteAllFixedItems()
        vendorDao.deleteAllVendors()
        categoryDao.deleteAllCustomCategories()
        projectDao.deleteAllProjects()
        wishlistDao.deleteAllWishlistItems()
    }
}
