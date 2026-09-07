package com.example.freshup.presentation.main.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.freshup.domain.model.Product

enum class UrgencyFilter {
    ALL,
    EXPIRED_OR_URGENT, // <= 2 дней
    SOON,              // 3-5 дней
    FRESH              // > 5 дней
}

@Composable
fun SummaryDashboard(
    products: List<Product>,
    selectedFilter: UrgencyFilter,
    onFilterSelected: (UrgencyFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    val urgentCount = products.count { it.daysLeft <= 2 }
    val soonCount = products.count { it.daysLeft in 3..5 }
    val freshCount = products.count { it.daysLeft > 5 }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        DashboardCard(
            count = urgentCount,
            title = "Срочно",
            subtitle = "до 2 дн",
            isSelected = selectedFilter == UrgencyFilter.EXPIRED_OR_URGENT,
            containerColor = Color(0xFFFFDAD6),
            contentColor = Color(0xFF410002),
            activeBorderColor = Color(0xFFBA1A1A),
            onClick = {
                onFilterSelected(
                    if (selectedFilter == UrgencyFilter.EXPIRED_OR_URGENT) UrgencyFilter.ALL
                    else UrgencyFilter.EXPIRED_OR_URGENT
                )
            },
            modifier = Modifier.weight(1f)
        )

        DashboardCard(
            count = soonCount,
            title = "Скоро",
            subtitle = "3–5 дн",
            isSelected = selectedFilter == UrgencyFilter.SOON,
            containerColor = Color(0xFFFFF3CD),
            contentColor = Color(0xFF664D03),
            activeBorderColor = Color(0xFFFFC107),
            onClick = {
                onFilterSelected(
                    if (selectedFilter == UrgencyFilter.SOON) UrgencyFilter.ALL
                    else UrgencyFilter.SOON
                )
            },
            modifier = Modifier.weight(1f)
        )

        DashboardCard(
            count = freshCount,
            title = "В норме",
            subtitle = "5+ дн",
            isSelected = selectedFilter == UrgencyFilter.FRESH,
            containerColor = Color(0xFFD5E8CD),
            contentColor = Color(0xFF101F10),
            activeBorderColor = Color(0xFF2E7D32),
            onClick = {
                onFilterSelected(
                    if (selectedFilter == UrgencyFilter.FRESH) UrgencyFilter.ALL
                    else UrgencyFilter.FRESH
                )
            },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun DashboardCard(
    count: Int,
    title: String,
    subtitle: String,
    isSelected: Boolean,
    containerColor: Color,
    contentColor: Color,
    activeBorderColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(12.dp),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, activeBorderColor) else null,
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp, horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = count.toString(),
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = contentColor
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = contentColor,
                    maxLines = 1
                )
                Text(
                    text = subtitle,
                    fontSize = 9.sp,
                    color = contentColor.copy(alpha = 0.75f),
                    maxLines = 1
                )
            }
        }
    }
}
