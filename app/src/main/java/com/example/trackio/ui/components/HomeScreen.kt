package com.example.trackio.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trackio.data.local.entity.CategoryEntity
import com.example.trackio.data.local.entity.TransactionDirection
import com.example.trackio.data.local.entity.TransactionEntity
import com.example.trackio.data.repository.DashboardState
import com.example.trackio.domain.SpendStatusColor
import com.example.trackio.ui.theme.DarkBackground
import com.example.trackio.ui.theme.DarkCardBorder
import com.example.trackio.ui.theme.DarkSurface
import com.example.trackio.ui.theme.DarkSurfaceVariant
import com.example.trackio.ui.theme.ElectricIndigo
import com.example.trackio.ui.theme.MintAccent
import com.example.trackio.ui.theme.StatusAmber
import com.example.trackio.ui.theme.StatusAmberBg
import com.example.trackio.ui.theme.StatusGreen
import com.example.trackio.ui.theme.StatusGreenBg
import com.example.trackio.ui.theme.StatusRed
import com.example.trackio.ui.theme.StatusRedBg
import com.example.trackio.ui.theme.TextPrimary
import com.example.trackio.ui.theme.TextSecondary
import com.example.trackio.ui.theme.TextTertiary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.trackio.data.local.entity.ProjectEntity
import com.example.trackio.data.local.entity.WishlistItemEntity

enum class HomeTab {
    DASHBOARD,
    PROJECTS,
    WISHLIST,
    INSIGHTS
}

@Composable
fun HomeScreen(
    dashboardState: DashboardState,
    categories: List<CategoryEntity>,
    projects: List<ProjectEntity> = emptyList(),
    wishlistItems: List<WishlistItemEntity> = emptyList(),
    onQuickAddClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onAddProject: (String, Double?) -> Unit = { _, _ -> },
    onDeleteProject: (ProjectEntity) -> Unit = {},
    onAddWishlistItem: (String, Double, String?) -> Unit = { _, _, _ -> },
    onToggleWishlistPurchased: (WishlistItemEntity) -> Unit = {},
    onDeleteWishlistItem: (WishlistItemEntity) -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(HomeTab.DASHBOARD) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = DarkSurface,
                tonalElevation = 8.dp,
                modifier = Modifier.border(1.dp, DarkCardBorder)
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Dashboard") },
                    label = { Text("Daily", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    selected = selectedTab == HomeTab.DASHBOARD,
                    onClick = { selectedTab = HomeTab.DASHBOARD },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ElectricIndigo,
                        selectedTextColor = ElectricIndigo,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary,
                        indicatorColor = ElectricIndigo.copy(alpha = 0.15f)
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Tag, contentDescription = "Projects") },
                    label = { Text("Projects", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    selected = selectedTab == HomeTab.PROJECTS,
                    onClick = { selectedTab = HomeTab.PROJECTS },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ElectricIndigo,
                        selectedTextColor = ElectricIndigo,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary,
                        indicatorColor = ElectricIndigo.copy(alpha = 0.15f)
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.CardGiftcard, contentDescription = "Wishlist") },
                    label = { Text("Wishlist", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    selected = selectedTab == HomeTab.WISHLIST,
                    onClick = { selectedTab = HomeTab.WISHLIST },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ElectricIndigo,
                        selectedTextColor = ElectricIndigo,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary,
                        indicatorColor = ElectricIndigo.copy(alpha = 0.15f)
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.BarChart, contentDescription = "Insights") },
                    label = { Text("Insights", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    selected = selectedTab == HomeTab.INSIGHTS,
                    onClick = { selectedTab = HomeTab.INSIGHTS },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ElectricIndigo,
                        selectedTextColor = ElectricIndigo,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary,
                        indicatorColor = ElectricIndigo.copy(alpha = 0.15f)
                    )
                )
            }
        },
        floatingActionButton = {
            if (selectedTab == HomeTab.DASHBOARD || selectedTab == HomeTab.PROJECTS) {
                FloatingActionButton(
                    onClick = onQuickAddClick,
                    containerColor = ElectricIndigo,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Quick Add",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                HomeTab.DASHBOARD -> {
                    DashboardContent(
                        dashboardState = dashboardState,
                        categories = categories,
                        onSettingsClick = onSettingsClick
                    )
                }
                HomeTab.PROJECTS -> {
                    ProjectsTab(
                        projects = projects,
                        transactions = dashboardState.recentTransactions,
                        onAddProject = onAddProject,
                        onDeleteProject = onDeleteProject
                    )
                }
                HomeTab.WISHLIST -> {
                    WishlistTab(
                        wishlistItems = wishlistItems,
                        calculation = dashboardState.calculation,
                        onAddWishlistItem = onAddWishlistItem,
                        onTogglePurchased = onToggleWishlistPurchased,
                        onDeleteWishlistItem = onDeleteWishlistItem
                    )
                }
                HomeTab.INSIGHTS -> {
                    InsightsTab(
                        transactions = dashboardState.recentTransactions,
                        categories = categories,
                        calculation = dashboardState.calculation
                    )
                }
            }
        }
    }
}

@Composable
fun DashboardContent(
    dashboardState: DashboardState,
    categories: List<CategoryEntity>,
    onSettingsClick: () -> Unit
) {
    val calc = dashboardState.calculation

    val statusColor = when (calc.statusColor) {
        SpendStatusColor.GREEN -> StatusGreen
        SpendStatusColor.AMBER -> StatusAmber
        SpendStatusColor.RED -> StatusRed
    }

    val statusBgColor = when (calc.statusColor) {
        SpendStatusColor.GREEN -> StatusGreenBg
        SpendStatusColor.AMBER -> StatusAmberBg
        SpendStatusColor.RED -> StatusRedBg
    }

    val progressFraction = if (calc.allowance > 0) {
        (calc.spentSoFar / calc.allowance).coerceIn(0.0, 1.0).toFloat()
    } else 1.0f

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))

                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Trackio",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = ElectricIndigo.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "STUDENT COPILOT",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ElectricIndigo
                                )
                            }
                        }
                        Text(
                            text = "${calc.daysLeftInCycle} days left in cycle",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                        )
                    }

                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = "Settings",
                            tint = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Hero Card: The Safe-to-Spend Number
                Card(
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(26.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "SAFE TO SPEND TODAY",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextSecondary,
                                    letterSpacing = 1.2.sp
                                )
                            )

                            // Status badge
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = statusBgColor
                            ) {
                                Text(
                                    text = calc.statusMessage.uppercase(),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = statusColor
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Giant Number
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "₹",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = statusColor
                                ),
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = String.format(Locale.US, "%.0f", calc.safeDailySpend),
                                style = MaterialTheme.typography.displayMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Text(
                                text = " / day",
                                style = MaterialTheme.typography.titleMedium.copy(color = TextSecondary),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Today's Spent vs Daily Target bar
                        LinearProgressIndicator(
                            progress = { progressFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = statusColor,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Spent today: ₹${calc.todaySpent.toInt()}",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                            )
                            Text(
                                text = "Today left: ₹${calc.todayRemaining.toInt()}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = statusColor
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Pocket Money Breakdown Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Remaining",
                                style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "₹${calc.remainingBalance.toInt()}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (calc.remainingBalance > 0) MaterialTheme.colorScheme.onSurface else StatusRed
                                )
                            )
                            Text(
                                text = "of ₹${calc.allowance.toInt()}",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextTertiary)
                            )
                        }
                    }

                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Fixed Items",
                                style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "₹${calc.upcomingFixedAmount.toInt()}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Text(
                                text = "Held aside",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextTertiary)
                            )
                        }
                    }

                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Cycle Days",
                                style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${calc.daysLeftInCycle} Left",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Text(
                                text = "of ${calc.daysTotalInCycle} days",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextTertiary)
                            )
                        }
                    }
                }

                // Needs Review Banner
                if (dashboardState.needsReviewCount > 0) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = StatusAmberBg),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, StatusAmber.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = StatusAmber)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "${dashboardState.needsReviewCount} spend(s) need review",
                                    fontWeight = FontWeight.Bold,
                                    color = StatusAmber,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "Tap to verify category and purpose",
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Recent Transactions Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Transactions",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    )
                    Text(
                        text = "${dashboardState.recentTransactions.size} total",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            // Transactions List
            if (dashboardState.recentTransactions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = TextTertiary,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No transactions logged yet",
                                color = TextSecondary,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Tap the + button to log your first spend",
                                color = TextTertiary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            } else {
                items(dashboardState.recentTransactions) { txn ->
                    val category = categories.find { it.id == txn.categoryId }
                    TransactionRowItem(txn = txn, category = category)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp)) // Padding for FAB
            }
        }
}

@Composable
fun TransactionRowItem(txn: TransactionEntity, category: CategoryEntity?) {
    val dateFormat = remember { SimpleDateFormat("hh:mm a, dd MMM", Locale.getDefault()) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Category icon box
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            category?.colorHex?.let {
                                try {
                                    Color(android.graphics.Color.parseColor(it)).copy(alpha = 0.15f)
                                } catch (e: Exception) {
                                    ElectricIndigo.copy(alpha = 0.15f)
                                }
                            } ?: ElectricIndigo.copy(alpha = 0.15f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = CategoryIconHelper.getIcon(category?.iconName ?: "Category"),
                        contentDescription = null,
                        tint = category?.colorHex?.let {
                            try {
                                Color(android.graphics.Color.parseColor(it))
                            } catch (e: Exception) {
                                ElectricIndigo
                            }
                        } ?: ElectricIndigo,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = txn.vendorName,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        category?.name?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                                fontSize = 12.sp
                            )
                        }

                        if (!txn.tag.isNullOrBlank()) {
                            Text(
                                text = " • #${txn.tag}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = MintAccent
                                ),
                                fontSize = 12.sp
                            )
                        }
                    }

                    Text(
                        text = dateFormat.format(Date(txn.timestamp)),
                        style = MaterialTheme.typography.labelSmall.copy(color = TextTertiary),
                        fontSize = 11.sp
                    )
                }
            }

            // Amount
            Text(
                text = "${if (txn.direction == TransactionDirection.PAID) "-" else "+"} ₹${txn.amount.toInt()}",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = if (txn.direction == TransactionDirection.PAID) MaterialTheme.colorScheme.onSurface else StatusGreen
                )
            )
        }
    }
}
