package com.example.freshup.presentation.main

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.freshup.domain.model.Product
import com.example.freshup.domain.model.StorageLocation
import com.example.freshup.presentation.main.components.AddProductDialog
import com.example.freshup.presentation.main.components.ProductCard
import com.example.freshup.presentation.main.components.StatisticsDialog
import com.example.freshup.presentation.main.components.SummaryDashboard
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ProductListScreen(
    onNavigateToSettings: () -> Unit,
    highlightedProductId: Int? = null,
    viewModel: ProductViewModel = viewModel()
) {
    val state by viewModel.screenState.collectAsState()
    val allProducts by viewModel.allActiveProducts.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedUrgency by viewModel.selectedUrgency.collectAsState()
    val selectedLocation by viewModel.selectedLocation.collectAsState()

    val consumedCount by viewModel.consumedCount.collectAsState()
    val discardedCount by viewModel.discardedCount.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showStatsDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showClearConfirm by remember { mutableStateOf(false) }
    var productToDelete by remember { mutableStateOf<Product?>(null) }
    var editingProduct by remember { mutableStateOf<Product?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val undoProduct by viewModel.undoProduct.collectAsState()

    LaunchedEffect(undoProduct) {
        val product = undoProduct
        if (product != null) {
            val result = snackbarHostState.showSnackbar(
                message = "«${product.name}» обновлён",
                actionLabel = "Отменить",
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.restoreLast()
            } else {
                viewModel.clearUndo()
            }
        }
    }

    if (showStatsDialog) {
        StatisticsDialog(
            consumedCount = consumedCount,
            discardedCount = discardedCount,
            onDismiss = { showStatsDialog = false }
        )
    }

    if (showAddDialog) {
        AddProductDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { id, name, category, expirationTimestamp, photoUri, notifyDaysBefore, storageLocation, barcode ->
                viewModel.addProduct(
                    name = name,
                    category = category,
                    expirationTimestamp = expirationTimestamp,
                    photoUri = photoUri,
                    notifyDaysBefore = notifyDaysBefore,
                    storageLocation = storageLocation,
                    barcode = barcode
                )
                showAddDialog = false
            }
        )
    }

    editingProduct?.let { product ->
        AddProductDialog(
            product = product,
            onDismiss = { editingProduct = null },
            onConfirm = { id, name, category, expirationTimestamp, photoUri, notifyDaysBefore, storageLocation, barcode ->
                viewModel.updateProduct(
                    id = id,
                    name = name,
                    category = category,
                    expirationTimestamp = expirationTimestamp,
                    photoUri = photoUri,
                    notifyDaysBefore = notifyDaysBefore,
                    storageLocation = storageLocation,
                    barcode = barcode
                )
                editingProduct = null
            }
        )
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Очистить список?") },
            text = { Text("Все продукты будут удалены. Действие нельзя отменить.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteAll()
                    showClearConfirm = false
                }) { Text("Очистить") }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) { Text("Отмена") }
            }
        )
    }

    if (productToDelete != null) {
        val product = productToDelete!!
        AlertDialog(
            onDismissRequest = { productToDelete = null },
            title = { Text("Удалить продукт?") },
            text = { Text("«${product.name}» будет удалён.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteProduct(product.id)
                    productToDelete = null
                }) { Text("Удалить") }
            },
            dismissButton = {
                TextButton(onClick = { productToDelete = null }) { Text("Отмена") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("FreshUp") },
                actions = {
                    IconButton(onClick = { showStatsDialog = true }) {
                        Icon(Icons.Filled.PieChart, contentDescription = "Статистика Zero Waste")
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "Настройки")
                    }
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "Меню")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Очистить список") },
                            onClick = {
                                showMenu = false
                                showClearConfirm = true
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Добавить продукт")
            }
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    actionColor = MaterialTheme.colorScheme.primary,
                    dismissActionContentColor = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 1. Поисковая строка
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Поиск продуктов...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Filled.Close, contentDescription = "Очистить")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            )

            // 2. Сводка статусов (Дашборд)
            SummaryDashboard(
                products = allProducts,
                selectedFilter = selectedUrgency,
                onFilterSelected = { viewModel.setUrgencyFilter(it) }
            )

            // 3. Чипсы фильтрации по местам хранения
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = selectedLocation == null,
                    onClick = { viewModel.setStorageLocation(null) },
                    label = { Text("Все места") }
                )
                StorageLocation.values().forEach { loc ->
                    FilterChip(
                        selected = selectedLocation == loc,
                        onClick = {
                            viewModel.setStorageLocation(if (selectedLocation == loc) null else loc)
                        },
                        label = { Text(loc.displayName) }
                    )
                }
            }

            // 4. Список продуктов
            when (val currentState = state) {
                is ProductScreenState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Загрузка...", style = MaterialTheme.typography.bodyLarge)
                    }
                }
                is ProductScreenState.Empty -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "Нет продуктов",
                                style = MaterialTheme.typography.headlineMedium
                            )
                            Text(
                                if (searchQuery.isNotBlank() || selectedUrgency != com.example.freshup.presentation.main.components.UrgencyFilter.ALL || selectedLocation != null)
                                    "По выбранным фильтрам ничего не найдено"
                                else "Нажмите + чтобы добавить",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                is ProductScreenState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            top = 8.dp,
                            end = 16.dp,
                            bottom = 96.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(
                            items = currentState.products,
                            key = { it.id }
                        ) { product ->
                            val revealWidth = 88.dp
                            val revealPx = with(LocalDensity.current) { revealWidth.toPx() }
                            val offsetX = remember { Animatable(0f) }
                            val scope = rememberCoroutineScope()

                            val isHighlighted = product.id == highlightedProductId
                            val isExpired = product.daysLeft <= 0
                            val density = LocalDensity.current
                            val floatY = remember { Animatable(0f) }
                            val cardAlpha = remember { Animatable(1f) }
                            val popScale = remember { Animatable(1f) }

                            LaunchedEffect(isHighlighted) {
                                if (isHighlighted) {
                                    val startY = with(density) { 20.dp.toPx() }
                                    floatY.snapTo(startY)
                                    cardAlpha.snapTo(0.45f)
                                    popScale.snapTo(1.06f)
                                    launch { floatY.animateTo(0f, tween(550, easing = EaseOutCubic)) }
                                    launch { cardAlpha.animateTo(1f, tween(550)) }
                                    launch { popScale.animateTo(1f, tween(600, easing = EaseOutCubic)) }
                                }
                            }

                            val bob by rememberInfiniteTransition(label = "bob")
                                .animateFloat(
                                    initialValue = -5f,
                                    targetValue = 5f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(1600, easing = FastOutSlowInEasing),
                                        repeatMode = RepeatMode.Reverse
                                    ),
                                    label = "bob"
                                )

                            val glowColor = MaterialTheme.colorScheme.primary
                            val glow = remember { Animatable(0f) }
                            LaunchedEffect(isHighlighted) {
                                if (isHighlighted) {
                                    glow.snapTo(0.9f)
                                    launch {
                                        glow.animateTo(0f, tween(3500, easing = FastOutSlowInEasing))
                                    }
                                }
                            }

                            val draggableState = rememberDraggableState { delta ->
                                scope.launch {
                                    offsetX.snapTo(
                                        (offsetX.value + delta).coerceIn(-revealPx, 0f)
                                    )
                                }
                            }

                            Box(modifier = Modifier.fillMaxWidth()) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.CenterEnd)
                                        .width(revealWidth),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    IconButton(
                                        onClick = { viewModel.deleteProduct(product.id) },
                                        modifier = Modifier
                                            .padding(start = 24.dp)
                                            .size(48.dp)
                                            .background(
                                                MaterialTheme.colorScheme.error,
                                                CircleShape
                                            )
                                    ) {
                                        Icon(
                                            Icons.Filled.Delete,
                                            contentDescription = "Удалить",
                                            tint = MaterialTheme.colorScheme.onError
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .graphicsLayer {
                                            translationY = floatY.value +
                                                if (isExpired && !isHighlighted) bob else 0f
                                            alpha = cardAlpha.value
                                            scaleX = popScale.value
                                            scaleY = popScale.value
                                            shadowElevation = glow.value * 28f
                                            spotShadowColor = glowColor
                                            ambientShadowColor = glowColor
                                        }
                                        .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                                ) {
                                    ProductCard(
                                        product = product,
                                        onClick = { editingProduct = product },
                                        onLongClick = { productToDelete = product },
                                        onConsume = { viewModel.markConsumed(product.id) },
                                        onDiscard = { viewModel.markDiscarded(product.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
