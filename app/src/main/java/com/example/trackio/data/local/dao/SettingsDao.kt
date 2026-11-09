package com.example.trackio.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.trackio.data.local.entity.SettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(settings: SettingsEntity)

    @Update
    suspend fun update(settings: SettingsEntity)

    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    fun getSettingsFlow(): Flow<SettingsEntity?>

    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettingsSync(): SettingsEntity?

    @Query("UPDATE app_settings SET isOnboardingCompleted = :completed WHERE id = 1")
    suspend fun setOnboardingCompleted(completed: Boolean)

    @Query("UPDATE app_settings SET allowanceAmount = :allowance WHERE id = 1")
    suspend fun updateAllowance(allowance: Double)
}
