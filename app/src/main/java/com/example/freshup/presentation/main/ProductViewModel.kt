package com.example.freshup.presentation.main

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.freshup.FreshUpApp
import com.example.freshup.data.ProductPhotoManager
import com.example.freshup.data.repository.ProductRepositoryImpl
import com.example.freshup.domain.model.Product
import com.example.freshup.domain.model.ProductStatus
import com.example.freshup.domain.model.StorageLocation
import com.example.freshup.notification.ProductAlarmScheduler
import com.example.freshup.presentation.main.components.UrgencyFilter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProductViewModel(application: Application) : AndroidViewModel(application) {
    private companion object {
        const val DAY_MS = 24L * 60 * 60 * 1000
    }

    private val repository: ProductRepositoryImpl

    // Фильтры и поиск
    val searchQuery = MutableStateFlow("")
    val selectedUrgency = MutableStateFlow(UrgencyFilter.ALL)
    val selectedLocation = MutableStateFlow<StorageLocation?>(null) // null = Все места

    // Все активные продукты (для дашборда)
    val allActiveProducts: StateFlow<List<Product>>

    // Экранное состояние с учётом фильтрации и поиска
    val screenState: StateFlow<ProductScreenState>

    // Статистика Zero Waste
    val consumedCount: StateFlow<Int>
    val discardedCount: StateFlow<Int>

    private val _undoProduct = MutableStateFlow<Product?>(null)
    val undoProduct: StateFlow<Product?> = _undoProduct.asStateFlow()

    init {
        val dao = (application as FreshUpApp).database.productDao()
        repository = ProductRepositoryImpl(dao)

        allActiveProducts = repository.getActiveProducts()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

        consumedCount = repository.getConsumedCount()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

        discardedCount = repository.getDiscardedCount()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

        screenState = combine(
            repository.getActiveProducts(),
            searchQuery,
            selectedUrgency,
            selectedLocation
        ) { products, query, urgency, location ->
            if (products.isEmpty()) {
                ProductScreenState.Empty
            } else {
                val filtered = products.filter { product ->
                    val matchesQuery = query.isBlank() || product.name.contains(query, ignoreCase = true)
                    val matchesLocation = location == null || product.storageLocation == location
                    val matchesUrgency = when (urgency) {
                        UrgencyFilter.ALL -> true
                        UrgencyFilter.EXPIRED_OR_URGENT -> product.daysLeft <= 2
                        UrgencyFilter.SOON -> product.daysLeft in 3..5
                        UrgencyFilter.FRESH -> product.daysLeft > 5
                    }
                    matchesQuery && matchesLocation && matchesUrgency
                }
                ProductScreenState.Success(filtered)
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ProductScreenState.Loading
        )
    }

    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun setUrgencyFilter(filter: UrgencyFilter) {
        selectedUrgency.value = filter
    }

    fun setStorageLocation(location: StorageLocation?) {
        selectedLocation.value = location
    }

    fun addProduct(
        name: String,
        category: String,
        expirationTimestamp: Long,
        photoUri: Uri?,
        notifyDaysBefore: Int,
        storageLocation: StorageLocation = StorageLocation.FRIDGE,
        barcode: String? = null
    ) {
        viewModelScope.launch {
            val safeNotify = notifyDaysBefore.coerceAtLeast(0)
            val photoPath = photoUri?.let { uri ->
                runCatching {
                    ProductPhotoManager.savePhoto(getApplication(), uri)
                }.getOrNull()
            }
            val id = repository.insert(
                Product(
                    name = name,
                    category = category,
                    expirationTimestamp = expirationTimestamp,
                    photoPath = photoPath,
                    notifyDaysBefore = safeNotify,
                    storageLocation = storageLocation,
                    barcode = barcode
                )
            )
            ProductAlarmScheduler.scheduleOne(
                getApplication(),
                id.toInt(),
                expirationTimestamp - safeNotify.toLong() * DAY_MS
            )
        }
    }

    fun markConsumed(id: Int) {
        viewModelScope.launch {
            val product = repository.getById(id) ?: return@launch
            _undoProduct.value = product
            repository.setProductStatus(id, ProductStatus.CONSUMED)
            ProductAlarmScheduler.cancelOne(getApplication(), id)
        }
    }

    fun markDiscarded(id: Int) {
        viewModelScope.launch {
            val product = repository.getById(id) ?: return@launch
            _undoProduct.value = product
            repository.setProductStatus(id, ProductStatus.DISCARDED)
            ProductAlarmScheduler.cancelOne(getApplication(), id)
        }
    }

    fun archiveProduct(id: Int) {
        viewModelScope.launch {
            repository.archiveProduct(id)
        }
    }

    fun deleteAll() {
        viewModelScope.launch {
            repository.getActiveProducts().first().forEach {
                ProductAlarmScheduler.cancelOne(getApplication(), it.id)
            }
            repository.deleteAll()
        }
    }

    fun deleteProduct(id: Int) {
        viewModelScope.launch {
            val product = repository.getById(id) ?: return@launch
            _undoProduct.value = product
            repository.deleteProduct(id)
            ProductAlarmScheduler.cancelOne(getApplication(), id)
        }
    }

    fun restoreLast() {
        viewModelScope.launch {
            _undoProduct.value?.let {
                // Восстанавливаем в статус ACTIVE
                repository.insert(it.copy(status = ProductStatus.ACTIVE))
            }
            _undoProduct.value = null
        }
    }

    fun clearUndo() {
        _undoProduct.value = null
    }

    fun updateProduct(
        id: Int,
        name: String,
        category: String,
        expirationTimestamp: Long,
        photoUri: Uri?,
        notifyDaysBefore: Int,
        storageLocation: StorageLocation,
        barcode: String?
    ) {
        viewModelScope.launch {
            val existing = repository.getById(id) ?: return@launch
            val newPhotoPath = if (photoUri != null) {
                runCatching {
                    ProductPhotoManager.savePhoto(getApplication(), photoUri)
                }.getOrNull()?.also { path ->
                    if (existing.photoPath != null && existing.photoPath != path) {
                        ProductPhotoManager.deletePhoto(existing.photoPath)
                    }
                }
            } else existing.photoPath

            repository.update(
                existing.copy(
                    name = name,
                    category = category,
                    expirationTimestamp = expirationTimestamp,
                    photoPath = newPhotoPath,
                    notifyDaysBefore = notifyDaysBefore,
                    storageLocation = storageLocation,
                    barcode = barcode
                )
            )
            val safeNotify = notifyDaysBefore.coerceAtLeast(0)
            ProductAlarmScheduler.cancelOne(getApplication(), id)
            ProductAlarmScheduler.scheduleOne(
                getApplication(),
                id,
                expirationTimestamp - safeNotify.toLong() * DAY_MS
            )
        }
    }
}
