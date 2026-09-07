package com.example.freshup.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.freshup.domain.model.Product
import com.example.freshup.domain.model.ProductStatus
import com.example.freshup.domain.model.StorageLocation

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: String,
    val expirationTimestamp: Long,
    val isArchived: Boolean = false,
    val notificationShown: Boolean = false,
    val photoPath: String? = null,
    val notifyDaysBefore: Int = 3,
    val storageLocation: String = "FRIDGE",
    val status: String = "ACTIVE",
    val consumedTimestamp: Long? = null,
    val barcode: String? = null
) {
    fun toDomainModel(): Product = Product(
        id = id,
        name = name,
        category = category,
        expirationTimestamp = expirationTimestamp,
        isArchived = isArchived,
        notificationShown = notificationShown,
        photoPath = photoPath,
        notifyDaysBefore = notifyDaysBefore,
        storageLocation = StorageLocation.fromString(storageLocation),
        status = ProductStatus.fromString(status),
        consumedTimestamp = consumedTimestamp,
        barcode = barcode
    )

    companion object {
        fun fromDomainModel(product: Product): ProductEntity = ProductEntity(
            id = product.id,
            name = product.name,
            category = product.category,
            expirationTimestamp = product.expirationTimestamp,
            isArchived = product.isArchived,
            notificationShown = product.notificationShown,
            photoPath = product.photoPath,
            notifyDaysBefore = product.notifyDaysBefore,
            storageLocation = product.storageLocation.name,
            status = product.status.name,
            consumedTimestamp = product.consumedTimestamp,
            barcode = product.barcode
        )
    }
}
