package com.example.trackio.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.trackio.data.local.entity.TransactionDirection
import com.example.trackio.data.local.entity.TransactionEntity
import com.example.trackio.data.local.entity.TransactionStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<TransactionEntity>): List<Long>

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getTransactionById(id: Long): TransactionEntity?

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp DESC")
    fun getTransactionsInRange(startTime: Long, endTime: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE timestamp >= :startTime AND timestamp <= :endTime AND direction = :direction AND status = :status")
    suspend fun getTransactionsInRangeSync(
        startTime: Long,
        endTime: Long,
        direction: TransactionDirection = TransactionDirection.PAID,
        status: TransactionStatus = TransactionStatus.CONFIRMED
    ): List<TransactionEntity>

    @Query("SELECT SUM(amount) FROM transactions WHERE timestamp >= :startTime AND timestamp <= :endTime AND direction = 'PAID' AND status = 'CONFIRMED'")
    fun observeTotalSpentInRange(startTime: Long, endTime: Long): Flow<Double?>

    @Query("SELECT SUM(amount) FROM transactions WHERE timestamp >= :startTime AND timestamp <= :endTime AND direction = 'PAID' AND status = 'CONFIRMED'")
    suspend fun getTotalSpentInRangeSync(startTime: Long, endTime: Long): Double?

    @Query("SELECT * FROM transactions WHERE status = 'NEEDS_REVIEW' ORDER BY timestamp DESC")
    fun getNeedsReviewTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT COUNT(*) FROM transactions WHERE status = 'NEEDS_REVIEW'")
    fun getNeedsReviewCount(): Flow<Int>

    @Query("SELECT * FROM transactions WHERE upiRef = :upiRef LIMIT 1")
    suspend fun findByUpiRef(upiRef: String): TransactionEntity?

    @Query("SELECT * FROM transactions WHERE amount = :amount AND timestamp BETWEEN :startTime AND :endTime LIMIT 1")
    suspend fun findDuplicateCandidate(amount: Double, startTime: Long, endTime: Long): TransactionEntity?

    @Query("SELECT * FROM transactions WHERE tag = :tag ORDER BY timestamp DESC")
    fun getTransactionsByTag(tag: String): Flow<List<TransactionEntity>>

    @Query("SELECT DISTINCT tag FROM transactions WHERE tag IS NOT NULL AND tag != ''")
    fun getAllTags(): Flow<List<String>>

    @Query("DELETE FROM transactions")
    suspend fun deleteAllTransactions()
}
