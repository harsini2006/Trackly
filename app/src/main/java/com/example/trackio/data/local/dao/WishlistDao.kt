package com.example.trackio.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.trackio.data.local.entity.WishlistItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WishlistDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: WishlistItemEntity): Long

    @Update
    suspend fun updateItem(item: WishlistItemEntity)

    @Delete
    suspend fun deleteItem(item: WishlistItemEntity)

    @Query("SELECT * FROM wishlist ORDER BY isPurchased ASC, createdAt DESC")
    fun getAllWishlistItems(): Flow<List<WishlistItemEntity>>

    @Query("DELETE FROM wishlist")
    suspend fun deleteAllWishlistItems()
}
