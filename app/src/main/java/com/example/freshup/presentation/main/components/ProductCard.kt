package com.example.freshup.presentation.main.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.freshup.domain.model.Product
import java.io.File

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProductCard(
    product: Product,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
    onConsume: () -> Unit = {},
    onDiscard: () -> Unit = {}
) {
    val isUrgent = product.daysLeft <= 2
    val isSoon = product.daysLeft in 3..5

    val containerColor = when {
        isUrgent -> MaterialTheme.colorScheme.errorContainer
        isSoon -> Color(0xFFFFF3CD)
        else -> MaterialTheme.colorScheme.primaryContainer
    }

    val contentColor = when {
        isUrgent -> MaterialTheme.colorScheme.onErrorContainer
        isSoon -> Color(0xFF856404)
        else -> MaterialTheme.colorScheme.onPrimaryContainer
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
                role = Role.Button
            ),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Миниатюра фото (52dp)
                val photoPath = product.photoPath
                val photoFile = photoPath?.let { File(it) }
                if (photoFile != null && photoFile.exists()) {
                    AsyncImage(
                        model = photoFile,
                        contentDescription = "Фото ${product.name}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(contentColor.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Image,
                            contentDescription = null,
                            tint = contentColor.copy(alpha = 0.7f),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = contentColor
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Text(
                            text = product.category,
                            style = MaterialTheme.typography.bodySmall,
                            color = contentColor.copy(alpha = 0.75f)
                        )
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodySmall,
                            color = contentColor.copy(alpha = 0.5f)
                        )
                        // Место хранения (без иконки)
                        Text(
                            text = product.storageLocation.displayName,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = contentColor.copy(alpha = 0.9f)
                        )
                    }
                }

                // Индикатор срока
                Column(horizontalAlignment = Alignment.End) {
                    val isExpired = product.daysLeft <= 0
                    Text(
                        text = if (isExpired) "0 дн." else "${product.daysLeft} дн.",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = contentColor
                    )
                    Text(
                        text = if (isExpired) "Просрочено" else "Осталось",
                        style = MaterialTheme.typography.labelSmall,
                        color = contentColor.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Высококонтрастные кнопки статуса «Съедено» / «Выброшено»
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Кнопка «Выброшено» — темная контрастная подложка с рамкой и четким текстом
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black.copy(alpha = 0.35f),
                    border = BorderStroke(1.dp, contentColor.copy(alpha = 0.45f)),
                    modifier = Modifier.clickable(onClick = onDiscard)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            Icons.Filled.DeleteOutline,
                            contentDescription = "Выброшено",
                            tint = contentColor,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "Выброшено",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = contentColor,
                            modifier = Modifier.padding(start = 5.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.size(8.dp))

                // Кнопка «Съедено» — плотный зеленый фон с ярко-белым текстом (отличный контраст на красном)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF2E7D32),
                    shadowElevation = 2.dp,
                    modifier = Modifier.clickable(onClick = onConsume)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            Icons.Filled.Check,
                            contentDescription = "Съедено",
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "Съедено",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(start = 5.dp)
                        )
                    }
                }
            }
        }
    }
}
