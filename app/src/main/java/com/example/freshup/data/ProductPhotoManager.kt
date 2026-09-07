package com.example.freshup.data

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import java.io.File

/**
 * Управляет фото продуктов: копирует выбранный Uri (галерея или камера) во внутреннее
 * хранилище приложения и удаляет файл при удалении продукта.
 *
 * В БД хранится абсолютный путь к файлу во внутреннем хранилище — он приватен для приложения,
 * переживает перезагрузку и не требует разрешений.
 */
object ProductPhotoManager {
    private const val TAG = "ProductPhotoManager"
    private const val DIR_NAME = "product_photos"

    // Размер стороны миниатюры. Исходные фото могут быть огромными (камера 12+ Мп),
    // поэтому пережимаем в декодере — иначе приложение быстро съест память и диск.
    private const val TARGET_SIZE_PX = 1024

    private fun photosDir(context: Context): File =
        File(context.filesDir, DIR_NAME).apply { if (!exists()) mkdirs() }

    /**
     * Копирует изображение из [sourceUri] во внутреннее хранилище с понижением разрешения.
     * @return абсолютный путь к сохранённому файлу.
     */
    fun savePhoto(context: Context, sourceUri: Uri): String {
        // Сначала измеряем границы, чтобы не грузить полный bitmap в память зря.
        val boundsOpts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(sourceUri)?.use { input ->
            BitmapFactory.decodeStream(input, null, boundsOpts)
        }

        val opts = BitmapFactory.Options().apply {
            inSampleSize = calculateSampleSize(
                boundsOpts.outWidth,
                boundsOpts.outHeight,
                TARGET_SIZE_PX
            )
        }

        val bitmap = context.contentResolver.openInputStream(sourceUri)?.use { input ->
            BitmapFactory.decodeStream(input, null, opts)
        } ?: throw IllegalStateException("Не удалось декодировать изображение")

        val outFile = File(photosDir(context), "product_${System.currentTimeMillis()}.jpg")
        outFile.outputStream().use { out ->
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 85, out)
        }
        bitmap.recycle()

        return outFile.absolutePath
    }

    /**
     * Создаёт временный файл для съёмки камерой и возвращает его content:// Uri через FileProvider.
     */
    fun createCameraCaptureUri(context: Context): Pair<Uri, String> {
        val file = File(photosDir(context), "capture_${System.currentTimeMillis()}.jpg")
        return Pair(
            androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            ),
            file.absolutePath
        )
    }

    /** Удаляет файл фото, если он существует. Молчит при отсутствии файла/пути. */
    fun deletePhoto(path: String?) {
        if (path.isNullOrBlank()) return
        runCatching {
            File(path).takeIf { it.exists() }?.delete()
        }.onFailure { Log.w(TAG, "Не удалось удалить фото: $path", it) }
    }

    private fun calculateSampleSize(width: Int, height: Int, target: Int): Int {
        if (width <= 0 || height <= 0) return 1
        var sample = 1
        while (width / (sample * 2) >= target && height / (sample * 2) >= target) {
            sample *= 2
        }
        return sample
    }
}
