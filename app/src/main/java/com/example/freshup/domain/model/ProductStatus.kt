package com.example.freshup.domain.model

enum class ProductStatus(val title: String) {
    ACTIVE("Активен"),
    CONSUMED("Съедено"),
    DISCARDED("Выброшено");

    companion object {
        fun fromString(value: String?): ProductStatus = when (value?.uppercase()) {
            "CONSUMED" -> CONSUMED
            "DISCARDED" -> DISCARDED
            else -> ACTIVE
        }
    }
}
