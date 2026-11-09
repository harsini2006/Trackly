package com.example.trackio.ui.components

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trackio.data.local.entity.CategoryEntity
import com.example.trackio.data.local.entity.TransactionDirection
import com.example.trackio.data.local.entity.TransactionEntity
import com.example.trackio.data.local.entity.TransactionStatus
import com.example.trackio.domain.SafeSpendCalculationResult
import com.example.trackio.ui.theme.DarkCardBorder
import com.example.trackio.ui.theme.DarkSurface
import com.example.trackio.ui.theme.DarkSurfaceVariant
import com.example.trackio.ui.theme.ElectricIndigo
import com.example.trackio.ui.theme.MintAccent
import com.example.trackio.ui.theme.StatusAmber
import com.example.trackio.ui.theme.StatusGreen
import com.example.trackio.ui.theme.StatusRed
import com.example.trackio.ui.theme.TextPrimary
import com.example.trackio.ui.theme.TextSecondary
import com.example.trackio.ui.theme.TextTertiary

data class CategorySpendItem(
    val category: CategoryEntity?,
    val totalSpent: Double,
    val percentage: Float,
    val count: Int
)

@Composable
fun InsightsTab(
    transactions: List<TransactionEntity>,
    categories: List<CategoryEntity>,
    calculation: SafeSpendCalculationResult
) {
    val confirmedPaidTxns = transactions.filter {
        it.direction == TransactionDirection.PAID && it.status == TransactionStatus.CONFIRMED
    }

    val totalSpent = calculation.spentSoFar.coerceAtLeast(0.0)

    // Category breakdown
    val categoryMap = categories.associateBy { it.id }
    val spendByCategory = confirmedPaidTxns
        .groupBy { it.categoryId }
        .map { (catId, txns) ->
            val sum = txns.sumOf { it.amount }
            val pct = if (totalSpent > 0) (sum / totalSpent).toFloat() else 0f
            CategorySpendItem(
                category = categoryMap[catId],
                totalSpent = sum,
                percentage = pct,
                count = txns.size
            )
        }
        .sortedByDescending { it.totalSpent }

    // Top merchant
    val topMerchantGroup = confirmedPaidTxns
        .groupBy { it.vendorName }
        .maxByOrNull { entry -> entry.value.sumOf { it.amount } }

    val topMerchantName = topMerchantGroup?.key ?: "None yet"
    val topMerchantTotal = topMerchantGroup?.value?.sumOf { it.amount } ?: 0.0
    val topMerchantCount = topMerchantGroup?.value?.size ?: 0

    // Burn velocity calculation
    val daysElapsed = (calculation.daysTotalInCycle - calculation.daysLeftInCycle + 1).coerceAtLeast(1)
    val actualDailyBurn = totalSpent / daysElapsed
    val baselineDailyBudget = if (calculation.daysTotalInCycle > 0) calculation.allowance / calculation.daysTotalInCycle else 0.0

    val isOverPaced = actualDailyBurn > baselineDailyBudget * 1.15

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))

            // Title Row
            Column {
                Text(
                    text = "Weekly Insights",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
                Text(
                    text = "Spending velocity & category distributions",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )
            }
        }

        // Card 1: Burn Velocity & Pace
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DarkCardBorder, RoundedCornerShape(22.dp))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (isOverPaced) StatusRed.copy(alpha = 0.15f) else MintAccent.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isOverPaced) Icons.Default.LocalFireDepartment else Icons.Default.TrendingDown,
                                    contentDescription = null,
                                    tint = if (isOverPaced) StatusRed else MintAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "BURN VELOCITY",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextSecondary,
                                    letterSpacing = 1.sp
                                )
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isOverPaced) StatusRed.copy(alpha = 0.15f) else StatusGreen.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = if (isOverPaced) "HIGH BURN" else "ON TRACK",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isOverPaced) StatusRed else StatusGreen
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("YOUR DAILY PACE", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary))
                            Text("₹${actualDailyBurn.toInt()} / day", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold, color = TextPrimary))
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("TARGET BASELINE", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary))
                            Text("₹${baselineDailyBudget.toInt()} / day", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold, color = TextSecondary))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (isOverPaced) {
                            "You are burning ₹${(actualDailyBurn - baselineDailyBudget).toInt()} more per day than your baseline budget."
                        } else {
                            "Great discipline! You are pacing ₹${(baselineDailyBudget - actualDailyBurn).toInt()} under your planned baseline daily rate."
                        },
                        style = MaterialTheme.typography.bodySmall.copy(color = TextTertiary, fontSize = 12.sp)
                    )
                }
            }
        }

        // Card 2: Top Merchant Highlight
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DarkCardBorder, RoundedCornerShape(22.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(ElectricIndigo.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Store,
                            contentDescription = null,
                            tint = ElectricIndigo,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "TOP MERCHANT THIS CYCLE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary,
                                letterSpacing = 0.8.sp
                            )
                        )
                        Text(
                            text = topMerchantName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary
                            )
                        )
                        if (topMerchantCount > 0) {
                            Text(
                                text = "$topMerchantCount transactions",
                                style = MaterialTheme.typography.labelSmall.copy(color = TextTertiary)
                            )
                        }
                    }

                    Text(
                        text = "₹${topMerchantTotal.toInt()}",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }
        }

        // Card 3: Spending Breakdown by Category
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DarkCardBorder, RoundedCornerShape(22.dp))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "CATEGORY BREAKDOWN",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary,
                                letterSpacing = 1.sp
                            )
                        )
                        Text(
                            text = "Total ₹${totalSpent.toInt()}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MintAccent
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (spendByCategory.isEmpty()) {
                        Text(
                            text = "No category data for this cycle yet.",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextTertiary)
                        )
                    } else {
                        spendByCategory.forEach { item ->
                            val catColor = item.category?.colorHex?.let {
                                try {
                                    Color(android.graphics.Color.parseColor(it))
                                } catch (e: Exception) {
                                    ElectricIndigo
                                }
                            } ?: ElectricIndigo

                            Column(modifier = Modifier.padding(vertical = 6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(catColor)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = item.category?.name ?: "Uncategorized",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                color = TextPrimary
                                            )
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "₹${item.totalSpent.toInt()}",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary
                                            )
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "${(item.percentage * 100).toInt()}%",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = TextSecondary
                                            ),
                                            modifier = Modifier.width(36.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                LinearProgressIndicator(
                                    progress = { item.percentage.coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = catColor,
                                    trackColor = DarkCardBorder
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
