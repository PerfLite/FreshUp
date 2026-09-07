package com.example.freshup.presentation.main.components

import android.Manifest
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

private fun Context.findActivity(): ComponentActivity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is ComponentActivity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

@OptIn(ExperimentalGetImage::class)
@Composable
fun BarcodeScannerDialog(
    onDismiss: () -> Unit,
    onBarcodeDetected: (String) -> Unit
) {
    val context = LocalContext.current
    val fallbackLifecycleOwner = LocalLifecycleOwner.current
    val targetLifecycleOwner: LifecycleOwner = context.findActivity() ?: fallbackLifecycleOwner

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var cameraInstance by remember { mutableStateOf<Camera?>(null) }
    var isTorchOn by remember { mutableStateOf(false) }
    var hasFlashUnit by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Выключаем фонарик при выходе из диалога
    DisposableEffect(Unit) {
        onDispose {
            try {
                cameraInstance?.cameraControl?.enableTorch(false)
            } catch (_: Exception) {}
        }
    }

    fun triggerVibration() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator?.vibrate(VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                val v = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                v?.vibrate(VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE))
            }
        } catch (_: Exception) {}
    }

    Dialog(
        onDismissRequest = {
            cameraInstance?.cameraControl?.enableTorch(false)
            onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black
        ) {
            if (!hasCameraPermission) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(
                            text = "Для сканирования штрихкода требуется доступ к камере",
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                            Text("Разрешить камеру")
                        }
                    }
                }
            } else {
                var isScanned by remember { mutableStateOf(false) }

                Box(modifier = Modifier.fillMaxSize()) {
                    AndroidView(
                        factory = { ctx ->
                            val previewView = PreviewView(ctx)
                            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                            val executor = ContextCompat.getMainExecutor(ctx)

                            cameraProviderFuture.addListener({
                                val cameraProvider = cameraProviderFuture.get()

                                val preview = Preview.Builder().build().also {
                                    it.surfaceProvider = previewView.surfaceProvider
                                }

                                val options = BarcodeScannerOptions.Builder()
                                    .setBarcodeFormats(
                                        Barcode.FORMAT_ALL_FORMATS
                                    )
                                    .build()
                                val barcodeScanner = BarcodeScanning.getClient(options)
                                val analysisExecutor = Executors.newSingleThreadExecutor()

                                val imageAnalysis = ImageAnalysis.Builder()
                                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                    .build()

                                imageAnalysis.setAnalyzer(analysisExecutor) { imageProxy ->
                                    val mediaImage = imageProxy.image
                                    if (mediaImage != null && !isScanned) {
                                        val image = InputImage.fromMediaImage(
                                            mediaImage,
                                            imageProxy.imageInfo.rotationDegrees
                                        )
                                        barcodeScanner.process(image)
                                            .addOnSuccessListener { barcodes ->
                                                val firstCode = barcodes.firstOrNull()?.rawValue
                                                if (!firstCode.isNullOrBlank() && !isScanned) {
                                                    isScanned = true
                                                    triggerVibration()
                                                    executor.execute {
                                                        cameraInstance?.cameraControl?.enableTorch(false)
                                                        onBarcodeDetected(firstCode)
                                                    }
                                                }
                                            }
                                            .addOnFailureListener { e ->
                                                e.printStackTrace()
                                            }
                                            .addOnCompleteListener {
                                                imageProxy.close()
                                            }
                                    } else {
                                        imageProxy.close()
                                    }
                                }

                                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                                try {
                                    cameraProvider.unbindAll()
                                    val boundCamera = cameraProvider.bindToLifecycle(
                                        targetLifecycleOwner,
                                        cameraSelector,
                                        preview,
                                        imageAnalysis
                                    )
                                    cameraInstance = boundCamera
                                    hasFlashUnit = boundCamera.cameraInfo.hasFlashUnit()
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }, executor)

                            previewView
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    // Рамка видоискателя штрихкода
                    Box(
                        modifier = Modifier
                            .size(width = 280.dp, height = 180.dp)
                            .align(Alignment.Center)
                            .border(2.dp, Color(0xFF81C784), RoundedCornerShape(16.dp))
                    )

                    // Верхняя панель с кнопкой закрытия, подсказкой и кнопкой фонарика
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp, start = 16.dp, end = 16.dp)
                            .align(Alignment.TopCenter),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                cameraInstance?.cameraControl?.enableTorch(false)
                                onDismiss()
                            },
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(20.dp))
                        ) {
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = "Закрыть",
                                tint = Color.White
                            )
                        }

                        Text(
                            text = "Наведите рамку на штрихкод",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        )

                        if (hasFlashUnit) {
                            IconButton(
                                onClick = {
                                    val nextState = !isTorchOn
                                    isTorchOn = nextState
                                    cameraInstance?.cameraControl?.enableTorch(nextState)
                                },
                                modifier = Modifier
                                    .background(
                                        if (isTorchOn) Color(0xFFFFD54F).copy(alpha = 0.35f) else Color.Black.copy(alpha = 0.55f),
                                        RoundedCornerShape(20.dp)
                                    )
                            ) {
                                Icon(
                                    imageVector = if (isTorchOn) Icons.Filled.FlashOn else Icons.Filled.FlashOff,
                                    contentDescription = if (isTorchOn) "Выключить фонарик" else "Включить фонарик",
                                    tint = if (isTorchOn) Color(0xFFFFD54F) else Color.White
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.size(48.dp))
                        }
                    }
                }
            }
        }
    }
}
