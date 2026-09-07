package com.example.freshup.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

data class ScannedProductInfo(
    val barcode: String,
    val name: String?,
    val category: String?
)

object BarcodeProductService {

    suspend fun lookupProduct(barcode: String): ScannedProductInfo? = withContext(Dispatchers.IO) {
        val urls = listOf(
            "https://ru.openfoodfacts.org/api/v0/product/$barcode.json",
            "https://world.openfoodfacts.org/api/v0/product/$barcode.json"
        )

        for (urlString in urls) {
            try {
                val url = URL(urlString)
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 3500
                    readTimeout = 3500
                    instanceFollowRedirects = true
                    setRequestProperty("User-Agent", "FreshUpApp - Android - Version 1.0 (alik@dev.local)")
                    setRequestProperty("Accept", "application/json")
                }

                if (conn.responseCode == 200) {
                    val reader = BufferedReader(InputStreamReader(conn.inputStream))
                    val response = reader.readText()
                    reader.close()

                    val json = JSONObject(response)
                    if (json.optInt("status", 0) == 1) {
                        val product = json.optJSONObject("product")
                        if (product != null) {
                            val brand = product.optString("brands", "").trim()
                            val rawName = product.optString("product_name_ru").ifBlank {
                                product.optString("product_name").ifBlank {
                                    product.optString("generic_name_ru").ifBlank {
                                        product.optString("generic_name", "")
                                    }
                                }
                            }.trim()

                            val fullName = when {
                                rawName.isNotBlank() && brand.isNotBlank() && !rawName.contains(brand, ignoreCase = true) -> "$brand $rawName"
                                rawName.isNotBlank() -> rawName
                                brand.isNotBlank() -> brand
                                else -> null
                            }

                            val categoriesStr = product.optString("categories", "").lowercase()
                            val detectedCategory = mapToCategory(categoriesStr)

                            if (!fullName.isNullOrBlank()) {
                                return@withContext ScannedProductInfo(
                                    barcode = barcode,
                                    name = fullName,
                                    category = detectedCategory
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                // пробуем следующий URL
            }
        }
        null
    }

    private fun mapToCategory(categoriesText: String): String {
        return when {
            categoriesText.contains("milk") || categoriesText.contains("dairy") ||
            categoriesText.contains("молок") || categoriesText.contains("сыр") ||
            categoriesText.contains("творог") || categoriesText.contains("йогурт") ||
            categoriesText.contains("масло") || categoriesText.contains("сметан") -> "Молочные продукты"

            categoriesText.contains("meat") || categoriesText.contains("мясо") ||
            categoriesText.contains("птиц") || categoriesText.contains("колбас") ||
            categoriesText.contains("сосиск") || categoriesText.contains("фарш") -> "Мясо"

            categoriesText.contains("fish") || categoriesText.contains("seafood") ||
            categoriesText.contains("рыб") || categoriesText.contains("морепродукт") ||
            categoriesText.contains("икра") || categoriesText.contains("кревет") -> "Рыба"

            categoriesText.contains("vegetable") || categoriesText.contains("овощ") ||
            categoriesText.contains("томат") || categoriesText.contains("огур") -> "Овощи"

            categoriesText.contains("fruit") || categoriesText.contains("фрукт") ||
            categoriesText.contains("ягод") || categoriesText.contains("яблок") ||
            categoriesText.contains("банан") -> "Фрукты"

            categoriesText.contains("bread") || categoriesText.contains("bakery") ||
            categoriesText.contains("хлеб") || categoriesText.contains("выпечк") ||
            categoriesText.contains("булоч") -> "Выпечка"

            categoriesText.contains("frozen") || categoriesText.contains("заморож") ||
            categoriesText.contains("пельмен") || categoriesText.contains("мороженое") -> "Замороженное"

            categoriesText.contains("beverage") || categoriesText.contains("drink") ||
            categoriesText.contains("напит") || categoriesText.contains("сок") ||
            categoriesText.contains("вод") || categoriesText.contains("чай") ||
            categoriesText.contains("кофе") -> "Напитки"

            categoriesText.contains("prepared") || categoriesText.contains("meal") ||
            categoriesText.contains("готов") || categoriesText.contains("салат") ||
            categoriesText.contains("суп") -> "Готовая еда"

            else -> "Другое"
        }
    }
}
