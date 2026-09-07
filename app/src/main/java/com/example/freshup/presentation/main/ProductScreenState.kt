package com.example.freshup.presentation.main

import com.example.freshup.domain.model.Product

sealed class ProductScreenState {
    data object Loading : ProductScreenState()
    data class Success(val products: List<Product>) : ProductScreenState()
    data object Empty : ProductScreenState()
}
