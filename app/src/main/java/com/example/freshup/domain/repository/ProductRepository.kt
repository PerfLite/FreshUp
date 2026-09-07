package com.example.freshup.domain.repository

import com.example.freshup.domain.model.Product
import com.example.freshup.domain.model.ProductStatus
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun getActiveProducts(): Flow<List<Product>>
    fun getAllProducts(): Flow<List<Product>>
    suspend fun insert(product: Product): Long
    suspend fun update(product: Product)
    suspend fun archiveProduct(id: Int)
    suspend fun setProductStatus(id: Int, status: ProductStatus)
    fun getConsumedCount(): Flow<Int>
    fun getDiscardedCount(): Flow<Int>
    suspend fun deleteAll()
    suspend fun deleteProduct(id: Int)
    suspend fun getById(id: Int): Product?
}
