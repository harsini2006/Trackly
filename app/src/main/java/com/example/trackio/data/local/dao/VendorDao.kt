package com.example.trackio.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.trackio.data.local.entity.VendorEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VendorDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVendor(vendor: VendorEntity): Long

    @Update
    suspend fun updateVendor(vendor: VendorEntity)

    @Query("SELECT * FROM vendors WHERE name = :name COLLATE NOCASE LIMIT 1")
    suspend fun findVendorByName(name: String): VendorEntity?

    @Query("SELECT * FROM vendors ORDER BY name ASC")
    fun getAllVendors(): Flow<List<VendorEntity>>

    @Query("SELECT name FROM vendors WHERE name LIKE :query || '%' ORDER BY name ASC LIMIT 10")
    suspend fun autocompleteVendorNames(query: String): List<String>

    @Query("DELETE FROM vendors")
    suspend fun deleteAllVendors()
}
