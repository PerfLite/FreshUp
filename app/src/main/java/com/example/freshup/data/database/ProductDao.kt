package com.example.freshup.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products WHERE isArchived = 0 AND status = 'ACTIVE' ORDER BY expirationTimestamp ASC")
    fun getActiveProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE isArchived = 0 AND status = 'ACTIVE'")
    suspend fun getActiveProductsList(): List<ProductEntity>

    @Query("SELECT * FROM products ORDER BY expirationTimestamp ASC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getById(id: Int): ProductEntity?

    @Query("SELECT photoPath FROM products WHERE photoPath IS NOT NULL")
    suspend fun getAllPhotoPaths(): List<String?>

    @Insert
    suspend fun insert(product: ProductEntity): Long

    @Update
    suspend fun update(product: ProductEntity)

    @Query("UPDATE products SET isArchived = 1 WHERE id = :id")
    suspend fun archiveById(id: Int)

    @Query("UPDATE products SET status = :status, consumedTimestamp = :timestamp WHERE id = :id")
    suspend fun updateProductStatus(id: Int, status: String, timestamp: Long)

    @Query("SELECT COUNT(*) FROM products WHERE status = 'CONSUMED'")
    fun getConsumedCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM products WHERE status = 'DISCARDED'")
    fun getDiscardedCount(): Flow<Int>

    @Query("DELETE FROM products")
    suspend fun deleteAll()

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query(
        "SELECT * FROM products WHERE isArchived = 0 AND status = 'ACTIVE' AND notificationShown = 0 " +
            "AND (expirationTimestamp - notifyDaysBefore * 86400000) <= :now " +
            "ORDER BY expirationTimestamp ASC"
    )
    suspend fun getProductsToNotify(now: Long): List<ProductEntity>

    @Query("UPDATE products SET notificationShown = 1 WHERE id = :id")
    suspend fun markNotified(id: Int)
}
