package com.example.trackio.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.trackio.data.local.entity.FixedItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FixedItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFixedItem(item: FixedItemEntity): Long

    @Update
    suspend fun updateFixedItem(item: FixedItemEntity)

    @Delete
    suspend fun deleteFixedItem(item: FixedItemEntity)

    @Query("SELECT * FROM fixed_items WHERE isEnabled = 1 ORDER BY dueDayOfMonth ASC")
    fun getActiveFixedItems(): Flow<List<FixedItemEntity>>

    @Query("SELECT * FROM fixed_items ORDER BY dueDayOfMonth ASC")
    fun getAllFixedItems(): Flow<List<FixedItemEntity>>

    @Query("SELECT * FROM fixed_items WHERE isEnabled = 1")
    suspend fun getActiveFixedItemsSync(): List<FixedItemEntity>

    @Query("SELECT SUM(amount) FROM fixed_items WHERE isEnabled = 1")
    fun getTotalFixedExpenses(): Flow<Double?>

    @Query("DELETE FROM fixed_items")
    suspend fun deleteAllFixedItems()
}
