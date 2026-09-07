package com.example.freshup.presentation.main.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun StatisticsDialog(
    consumedCount: Int,
    discardedCount: Int,
    onDismiss: () -> Unit
) {
    val total = consumedCount + discardedCount
    val saveRatio = if (total > 0) (consumedCount.toFloat() / total.toFloat()) else 1f
    val savePercentage = (saveRatio * 100).toInt()

    val motivationText = when {
        total == 0 -> "Добавляйте продукты и отмечайте их, когда съедите! Здесь появится ваша статистика осознанного потребления."
        savePercentage >= 85 -> "🏆 Потрясающе! Вы спасаете почти всю еду и практически ничего не выбрасываете!"
        savePercentage >= 65 -> "👍 Отличный результат! Большинство продуктов съедается вовремя."
        else -> "💡 Совет: проверяйте блок «Срочно» на главном экране почаще, чтобы продукты не успевали испортиться."
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "📊 Статистика Zero Waste", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Большой круговой индикатор эффективности
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(if (savePercentage >= 70) Color(0xFFD5E8CD) else Color(0xFFFFF3CD))
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$savePercentage%",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (savePercentage >= 70) Color(0xFF1B5E20) else Color(0xFF856404)
                        )
                        Text(
                            text = "спасено",
                            fontSize = 12.sp,
                            color = if (savePercentage >= 70) Color(0xFF2E7D32) else Color(0xFF856404)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Карточки: Съедено vs Выброшено
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("✅ Съедено", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2E7D32))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("$consumedCount", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))
                        }
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFDAD6)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("🗑️ Выброшено", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFBA1A1A))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("$discardedCount", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF410002))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Шкала прогресса
                LinearProgressIndicator(
                    progress = { saveRatio },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = Color(0xFF2E7D32),
                    trackColor = Color(0xFFFFDAD6)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = motivationText,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Понятно", fontWeight = FontWeight.Bold)
            }
        }
    )
}
