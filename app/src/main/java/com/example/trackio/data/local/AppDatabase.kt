package com.example.trackio.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.trackio.data.local.dao.CategoryDao
import com.example.trackio.data.local.dao.FixedItemDao
import com.example.trackio.data.local.dao.SettingsDao
import com.example.trackio.data.local.dao.TransactionDao
import com.example.trackio.data.local.dao.VendorDao
import com.example.trackio.data.local.entity.CategoryEntity
import com.example.trackio.data.local.entity.FixedItemEntity
import com.example.trackio.data.local.entity.ProjectEntity
import com.example.trackio.data.local.entity.SettingsEntity
import com.example.trackio.data.local.entity.TransactionEntity
import com.example.trackio.data.local.entity.VendorEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        TransactionEntity::class,
        CategoryEntity::class,
        VendorEntity::class,
        FixedItemEntity::class,
        SettingsEntity::class,
        ProjectEntity::class,
        com.example.trackio.data.local.entity.WishlistItemEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun vendorDao(): VendorDao
    abstract fun fixedItemDao(): FixedItemDao
    abstract fun settingsDao(): SettingsDao
    abstract fun projectDao(): com.example.trackio.data.local.dao.ProjectDao
    abstract fun wishlistDao(): com.example.trackio.data.local.dao.WishlistDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        const val DATABASE_NAME = "trackio_db"

        val DEFAULT_CATEGORIES = listOf(
            CategoryEntity(1, "Food", "Restaurant", isDefault = true, colorHex = "#EF4444"),
            CategoryEntity(2, "Online shopping", "ShoppingBag", isDefault = true, colorHex = "#F59E0B"),
            CategoryEntity(3, "Stationery", "MenuBook", isDefault = true, colorHex = "#10B981"),
            CategoryEntity(4, "Travel", "DirectionsBus", isDefault = true, colorHex = "#3B82F6"),
            CategoryEntity(5, "Bills and recharge", "Receipt", isDefault = true, colorHex = "#8B5CF6"),
            CategoryEntity(6, "Friends and gifts", "CardGiftcard", isDefault = true, colorHex = "#EC4899"),
            CategoryEntity(7, "Projects", "Engineering", isDefault = true, colorHex = "#06B6D4"),
            CategoryEntity(8, "Miscellaneous", "Category", isDefault = true, colorHex = "#64748B")
        )

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .addCallback(DatabaseCallback())
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                CoroutineScope(Dispatchers.IO).launch {
                    INSTANCE?.let { database ->
                        // Pre-populate default categories
                        database.categoryDao().insertCategories(DEFAULT_CATEGORIES)
                        // Pre-populate default settings
                        database.settingsDao().insertOrUpdate(SettingsEntity())
                        // Seed common vendor mappings
                        database.vendorDao().insertVendor(VendorEntity(name = "Swiggy", defaultCategoryId = 1))
                        database.vendorDao().insertVendor(VendorEntity(name = "Zomato", defaultCategoryId = 1))
                        database.vendorDao().insertVendor(VendorEntity(name = "Blinkit", defaultCategoryId = 1))
                        database.vendorDao().insertVendor(VendorEntity(name = "Zepto", defaultCategoryId = 1))
                        database.vendorDao().insertVendor(VendorEntity(name = "Amazon", defaultCategoryId = 2))
                        database.vendorDao().insertVendor(VendorEntity(name = "Flipkart", defaultCategoryId = 2))
                        database.vendorDao().insertVendor(VendorEntity(name = "Uber", defaultCategoryId = 4))
                        database.vendorDao().insertVendor(VendorEntity(name = "Ola", defaultCategoryId = 4))
                        database.vendorDao().insertVendor(VendorEntity(name = "Rapido", defaultCategoryId = 4))
                    }
                }
            }
        }
    }
}
