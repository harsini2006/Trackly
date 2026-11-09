package com.example.trackio.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.trackio.data.local.AppDatabase
import com.example.trackio.data.local.entity.CategoryEntity
import com.example.trackio.data.local.entity.CycleType
import com.example.trackio.data.local.entity.FixedItemEntity
import com.example.trackio.data.local.entity.SettingsEntity
import com.example.trackio.data.local.entity.TransactionEntity
import com.example.trackio.data.local.entity.TransactionSource
import com.example.trackio.data.local.entity.TransactionStatus
import com.example.trackio.data.repository.DashboardState
import com.example.trackio.data.repository.TrackioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MainUiState(
    val isLoading: Boolean = true,
    val isOnboardingCompleted: Boolean = false,
    val dashboard: DashboardState? = null,
    val categories: List<CategoryEntity> = emptyList(),
    val fixedItems: List<FixedItemEntity> = emptyList(),
    val projects: List<com.example.trackio.data.local.entity.ProjectEntity> = emptyList(),
    val wishlistItems: List<com.example.trackio.data.local.entity.WishlistItemEntity> = emptyList()
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TrackioRepository

    val uiState: StateFlow<MainUiState>

    init {
        val database = AppDatabase.getInstance(application)
        repository = TrackioRepository(database)

        uiState = combine(
            repository.getDashboardStateFlow(),
            repository.getAllCategories(),
            repository.getAllFixedItems(),
            repository.getAllProjects(),
            repository.observeWishlist()
        ) { dashboard, categories, fixedItems, projects, wishlistItems ->
            MainUiState(
                isLoading = false,
                isOnboardingCompleted = dashboard.settings.isOnboardingCompleted,
                dashboard = dashboard,
                categories = categories,
                fixedItems = fixedItems,
                projects = projects,
                wishlistItems = wishlistItems
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = MainUiState(isLoading = true)
        )
    }

    fun finishOnboarding(
        allowance: Double,
        cycleType: CycleType,
        startDay: Int,
        initialFixedItems: List<Pair<String, Double>> = emptyList()
    ) {
        viewModelScope.launch {
            val currentSettings = uiState.value.dashboard?.settings ?: SettingsEntity()
            val updatedSettings = currentSettings.copy(
                allowanceAmount = allowance,
                cycleType = cycleType,
                startDayOfMonth = startDay,
                isOnboardingCompleted = true
            )
            repository.updateSettings(updatedSettings)

            initialFixedItems.forEach { (name, amount) ->
                repository.addFixedItem(name = name, amount = amount, dueDay = 1)
            }
        }
    }

    fun quickAddTransaction(
        amount: Double,
        vendorName: String,
        categoryId: Long? = null,
        tag: String? = null
    ) {
        viewModelScope.launch {
            repository.quickAddTransaction(
                amount = amount,
                vendorName = vendorName,
                categoryId = categoryId,
                tag = tag,
                source = TransactionSource.MANUAL,
                status = TransactionStatus.CONFIRMED
            )
        }
    }

    suspend fun autocompleteVendors(query: String): List<String> {
        return repository.autocompleteVendors(query)
    }

    fun updateTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.updateTransaction(transaction)
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    fun addFixedItem(name: String, amount: Double, dueDay: Int) {
        viewModelScope.launch {
            repository.addFixedItem(name, amount, dueDay)
        }
    }

    fun deleteFixedItem(item: FixedItemEntity) {
        viewModelScope.launch {
            repository.deleteFixedItem(item)
        }
    }

    fun resetAllData() {
        viewModelScope.launch {
            repository.deleteAllUserData()
            repository.updateSettings(SettingsEntity(isOnboardingCompleted = false))
        }
    }

    suspend fun exportCsv(): String {
        return repository.exportToCsv()
    }

    fun importCsv(content: String) {
        viewModelScope.launch {
            com.example.trackio.data.csv.CsvManager.importFromCsv(content, repository)
        }
    }

    // Projects
    fun addProject(name: String, capAmount: Double?) {
        viewModelScope.launch {
            repository.addProject(name, capAmount)
        }
    }

    fun deleteProject(project: com.example.trackio.data.local.entity.ProjectEntity) {
        viewModelScope.launch {
            repository.deleteProject(project)
        }
    }

    // Wishlist
    fun addWishlistItem(name: String, targetPrice: Double, notesOrUrl: String? = null) {
        viewModelScope.launch {
            repository.addWishlistItem(name, targetPrice, notesOrUrl)
        }
    }

    fun toggleWishlistPurchased(item: com.example.trackio.data.local.entity.WishlistItemEntity) {
        viewModelScope.launch {
            repository.toggleWishlistPurchased(item)
        }
    }

    fun deleteWishlistItem(item: com.example.trackio.data.local.entity.WishlistItemEntity) {
        viewModelScope.launch {
            repository.deleteWishlistItem(item)
        }
    }
}
