package com.example.freshup

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.freshup.notification.NotificationHelper
import com.example.freshup.presentation.main.ProductListScreen
import com.example.freshup.presentation.settings.SettingsScreen
import com.example.freshup.presentation.settings.SettingsViewModel
import com.example.freshup.ui.theme.FreshUpTheme

private enum class Screen { LIST, SETTINGS }

class MainActivity : ComponentActivity() {
    // Какой продукт подсветить при открытии по тапу из уведомления.
    private val highlightedProductId = mutableStateOf<Int?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleIntent(intent)

        // Запрос разрешения на уведомления (только Android 13+ / API 33+).
        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            val settingsViewModel: SettingsViewModel = viewModel()
            val themeMode by settingsViewModel.themeMode.collectAsState()

            FreshUpTheme(themeMode = themeMode) {
                AppContent(highlightedProductId = highlightedProductId.value)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        val id = intent.getIntExtra(NotificationHelper.EXTRA_OPEN_PRODUCT_ID, -1)
        highlightedProductId.value = if (id >= 0) id else null
    }
}

@Composable
private fun AppContent(highlightedProductId: Int? = null) {
    var screen by remember { mutableStateOf(Screen.LIST) }

    when (screen) {
        Screen.LIST -> ProductListScreen(
            onNavigateToSettings = { screen = Screen.SETTINGS },
            highlightedProductId = highlightedProductId
        )
        Screen.SETTINGS -> SettingsScreen(
            onBack = { screen = Screen.LIST }
        )
    }
}
