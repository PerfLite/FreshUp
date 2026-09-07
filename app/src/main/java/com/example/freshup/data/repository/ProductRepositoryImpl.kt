package com.example.freshup.data.repository

import com.example.freshup.data.ProductPhotoManager
import com.example.freshup.data.database.ProductDao
import com.example.freshup.data.database.ProductEntity
import com.example.freshup.domain.model.Product
import com.example.freshup.domain.model.ProductStatus
import com.example.freshup.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProductRepositoryImpl(private val productDao: ProductDao) : ProductRepository {
    override fun getActiveProducts(): Flow<List<Product>> =
        productDao.getActiveProducts().map { entities ->
            entities.map { it.toDomainModel() }
        }

    override fun getAllProducts(): Flow<List<Product>> =
        productDao.getAllProducts().map { entities ->
            entities.map { it.toDomainModel() }
        }

    override suspend fun insert(product: Product): Long {
        return productDao.insert(ProductEntity.fromDomainModel(product))
    }

    override suspend fun update(product: Product) {
        productDao.update(ProductEntity.fromDomainModel(product))
    }

    override suspend fun archiveProduct(id: Int) {
        productDao.archiveById(id)
    }

    override suspend fun setProductStatus(id: Int, status: ProductStatus) {
        productDao.updateProductStatus(id, status.name, System.currentTimeMillis())
    }

    override fun getConsumedCount(): Flow<Int> = productDao.getConsumedCount()

    override fun getDiscardedCount(): Flow<Int> = productDao.getDiscardedCount()

    override suspend fun deleteAll() {
        // Чистим файлы фото всех продуктов перед удалением строк.
        productDao.getAllPhotoPaths().forEach { path ->
            ProductPhotoManager.deletePhoto(path)
        }
        productDao.deleteAll()
    }

    override suspend fun deleteProduct(id: Int) {
        productDao.getById(id)?.toDomainModel()?.let { product ->
            ProductPhotoManager.deletePhoto(product.photoPath)
        }
        productDao.deleteById(id)
    }

    override suspend fun getById(id: Int): Product? =
        productDao.getById(id)?.toDomainModel()
}
