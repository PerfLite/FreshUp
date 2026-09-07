package com.example.freshup.domain.model

data class Product(
    val id: Int = 0,
    val name: String,
    val category: String,
    val expirationTimestamp: Long,
    val isArchived: Boolean = false,
    val notificationShown: Boolean = false,
    val photoPath: String? = null,
    val notifyDaysBefore: Int = 3,
    val storageLocation: StorageLocation = StorageLocation.FRIDGE,
    val status: ProductStatus = ProductStatus.ACTIVE,
    val consumedTimestamp: Long? = null,
    val barcode: String? = null
) {
    val daysLeft: Long
        get() {
            val now = System.currentTimeMillis()
            val diff = expirationTimestamp - now
            return diff / (1000 * 60 * 60 * 24)
        }

    /**
     * Момент, в который пора слать уведомление: за [notifyDaysBefore] дней до истечения срока.
     * Если 0 — уведомление в день истечения; можно и «0 дней» (в момент срока).
     */
    fun notifyAtTimestamp(): Long =
        expirationTimestamp - notifyDaysBefore.toLong() * DAY_MS

    private companion object {
        const val DAY_MS = 24L * 60 * 60 * 1000
    }
}
