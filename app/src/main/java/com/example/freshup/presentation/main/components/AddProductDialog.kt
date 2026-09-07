package com.example.freshup.presentation.main.components

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.Color
import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.freshup.data.BarcodeProductService
import com.example.freshup.data.ProductPhotoManager
import com.example.freshup.domain.model.Categories
import com.example.freshup.domain.model.Product
import com.example.freshup.domain.model.StorageLocation
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        id: Int,
        name: String,
        category: String,
        expirationTimestamp: Long,
        photoUri: Uri?,
        notifyDaysBefore: Int,
        storageLocation: StorageLocation,
        barcode: String?
    ) -> Unit,
    product: Product? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val initialCal = product?.expirationTimestamp?.let { ts ->
        Calendar.getInstance().apply { timeInMillis = ts }
    }
    var name by remember { mutableStateOf(product?.name ?: "") }
    var category by remember { mutableStateOf(product?.category ?: "") }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var selectedDateMillis by remember { mutableStateOf(initialCal?.timeInMillis) }
    var selectedHour by remember { mutableStateOf(initialCal?.get(Calendar.HOUR_OF_DAY)) }
    var selectedMinute by remember { mutableStateOf(initialCal?.get(Calendar.MINUTE)) }
    var expanded by remember { mutableStateOf(false) }

    var storageLocation by remember {
        mutableStateOf(product?.storageLocation ?: StorageLocation.FRIDGE)
    }
    var barcode by remember { mutableStateOf(product?.barcode) }
    var showBarcodeScanner by remember { mutableStateOf(false) }
    var isLookingUpBarcode by remember { mutableStateOf(false) }

    var photoUri by remember {
        mutableStateOf(product?.photoPath?.let { Uri.fromFile(File(it)) })
    }
    var photoTouched by remember { mutableStateOf(false) }
    var notifyDaysBefore by remember {
        mutableStateOf((product?.notifyDaysBefore ?: 3).toString())
    }

    val dateTimeFormatter = remember { SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()) }

    // Дата + время. Если время не выбрано — конец дня (23:59), чтобы продукт «доживал» весь день.
    val composedTimestamp by remember {
        derivedStateOf {
            selectedDateMillis?.let { dateMillis ->
                val calendar = Calendar.getInstance().apply {
                    timeInMillis = dateMillis
                    set(Calendar.HOUR_OF_DAY, selectedHour ?: 23)
                    set(Calendar.MINUTE, selectedMinute ?: 59)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                calendar.timeInMillis
            }
        }
    }

    // Сканер штрихкодов
    if (showBarcodeScanner) {
        BarcodeScannerDialog(
            onDismiss = { showBarcodeScanner = false },
            onBarcodeDetected = { scannedCode ->
                showBarcodeScanner = false
                barcode = scannedCode
                if (name.isBlank()) {
                    name = "Штрихкод $scannedCode"
                }
                scope.launch {
                    isLookingUpBarcode = true
                    val info = BarcodeProductService.lookupProduct(scannedCode)
                    isLookingUpBarcode = false
                    if (info != null && !info.name.isNullOrBlank()) {
                        name = info.name
                        info.category?.let { category = it }
                        Toast.makeText(context, "Найдено: ${info.name}", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Штрихкод $scannedCode считан (в базе не найден)", Toast.LENGTH_LONG).show()
                    }
                }
            }
        )
    }

    // Галерея: современный Photo Picker.
    val pickFromGallery = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            photoUri = uri
            photoTouched = true
        }
    }

    // Камера
    var pendingCapturePath by remember { mutableStateOf<String?>(null) }
    val takePhoto = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && pendingCapturePath != null) {
            photoUri = Uri.fromFile(File(pendingCapturePath!!))
            photoTouched = true
        }
        pendingCapturePath = null
    }

    val requestCamera = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val (uri, path) = ProductPhotoManager.createCameraCaptureUri(context)
            pendingCapturePath = path
            takePhoto.launch(uri)
        }
    }

    fun launchGallery() {
        pickFromGallery.launch(
            androidx.activity.result.PickVisualMediaRequest(
                ActivityResultContracts.PickVisualMedia.ImageOnly
            )
        )
    }

    fun launchCamera() {
        val granted = context.checkSelfPermission(Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED
        if (granted) {
            val (uri, path) = ProductPhotoManager.createCameraCaptureUri(context)
            pendingCapturePath = path
            takePhoto.launch(uri)
        } else {
            requestCamera.launch(Manifest.permission.CAMERA)
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDateMillis ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { selectedDateMillis = it }
                    showDatePicker = false
                }) { Text("ОК") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Отмена") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        val now = Calendar.getInstance()
        val timePickerState = rememberTimePickerState(
            initialHour = selectedHour ?: now.get(Calendar.HOUR_OF_DAY),
            initialMinute = selectedMinute ?: now.get(Calendar.MINUTE),
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    selectedHour = timePickerState.hour
                    selectedMinute = timePickerState.minute
                    showTimePicker = false
                }) { Text("ОК") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Отмена") }
            },
            title = { Text("Время") },
            text = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TimePicker(state = timePickerState)
                }
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        title = { Text(if (product != null) "Редактировать продукт" else "Новый продукт") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                // Фото: превью + кнопки «галерея» / «камера».
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (photoUri != null) {
                        AsyncImage(
                            model = photoUri,
                            contentDescription = "Фото продукта",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        IconButton(onClick = { launchGallery() }) {
                            Icon(Icons.Filled.PhotoLibrary, contentDescription = "Из галереи")
                        }
                        IconButton(onClick = { launchCamera() }) {
                            Icon(Icons.Filled.AddAPhoto, contentDescription = "Сделать снимок")
                        }
                    }
                }

                // Название + кнопка сканера штрихкодов
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Название") },
                    singleLine = true,
                    trailingIcon = {
                        if (isLookingUpBarcode) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            IconButton(onClick = { showBarcodeScanner = true }) {
                                Icon(Icons.Filled.QrCodeScanner, contentDescription = "Сканировать штрихкод")
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth()
                )

                // Место хранения (Холодильник / Морозилка / Шкаф)
                Text(
                    text = "Где хранится:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    StorageLocation.values().forEach { loc ->
                        val isSelected = storageLocation == loc
                        val containerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                        val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(9.dp))
                                .background(containerColor)
                                .clickable { storageLocation = loc }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = loc.title,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = contentColor,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }

                // Категория
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Категория") },
                        singleLine = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        Categories.preset.forEach { presetCategory ->
                            DropdownMenuItem(
                                text = { Text(presetCategory) },
                                onClick = {
                                    category = presetCategory
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                // Быстрые пресеты срока годности
                Text(
                    text = "Быстрый срок годности:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    listOf(
                        "+1 дн" to 1,
                        "+3 дн" to 3,
                        "+1 нед" to 7,
                        "+2 нед" to 14,
                        "+1 мес" to 30,
                        "+6 мес" to 180
                    ).forEach { (label, days) ->
                        AssistChip(
                            onClick = {
                                val cal = Calendar.getInstance().apply {
                                    add(Calendar.DAY_OF_YEAR, days)
                                }
                                selectedDateMillis = cal.timeInMillis
                                selectedHour = 23
                                selectedMinute = 59
                            },
                            label = { Text(label, fontSize = 11.sp) }
                        )
                    }
                }

                // Предпросмотр срока годности (дата + время).
                OutlinedTextField(
                    value = composedTimestamp?.let { dateTimeFormatter.format(Date(it)) }
                        ?: "Не выбрано",
                    onValueChange = {},
                    label = { Text("Срок годности") },
                    readOnly = true,
                    enabled = false,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (selectedDateMillis != null) "Изменить дату" else "Выбрать дату")
                    }
                    TextButton(
                        onClick = { showTimePicker = true },
                        enabled = selectedDateMillis != null,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (selectedHour != null) "Изменить время" else "Время")
                    }
                }

                // За сколько дней до срока уведомить
                OutlinedTextField(
                    value = notifyDaysBefore,
                    onValueChange = { input ->
                        notifyDaysBefore = input.filter { it.isDigit() }.take(3)
                    },
                    label = { Text("Уведомить за (дней)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val timestamp = composedTimestamp ?: System.currentTimeMillis()
                    val days = notifyDaysBefore.trim().toIntOrNull() ?: 0
                    onConfirm(
                        product?.id ?: 0,
                        name.trim(),
                        category.trim().ifEmpty { Categories.DEFAULT },
                        timestamp,
                        photoUri.takeIf { photoTouched },
                        days,
                        storageLocation,
                        barcode
                    )
                },
                enabled = name.isNotBlank() && selectedDateMillis != null
            ) { Text(if (product != null) "Сохранить" else "Добавить") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}
