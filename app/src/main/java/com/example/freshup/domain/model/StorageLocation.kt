package com.example.freshup.domain.model

enum class StorageLocation(val title: String) {
    FRIDGE("Холодильник"),
    FREEZER("Морозилка"),
    PANTRY("Шкаф");

    val displayName: String get() = title

    companion object {
        fun fromString(value: String?): StorageLocation = when (value?.uppercase()) {
            "FREEZER" -> FREEZER
            "PANTRY" -> PANTRY
            else -> FRIDGE
        }
    }
}
