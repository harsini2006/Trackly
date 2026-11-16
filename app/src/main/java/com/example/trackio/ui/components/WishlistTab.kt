package com.example.trackio.ui.components

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trackio.data.local.entity.WishlistItemEntity
import com.example.trackio.domain.SafeSpendCalculationResult
import com.example.trackio.ui.theme.DarkCardBorder
import com.example.trackio.ui.theme.DarkSurface
import com.example.trackio.ui.theme.ElectricIndigo
import com.example.trackio.ui.theme.MintAccent
import com.example.trackio.ui.theme.StatusAmber
import com.example.trackio.ui.theme.StatusGreen
import com.example.trackio.ui.theme.StatusRed
import com.example.trackio.ui.theme.TextPrimary
import com.example.trackio.ui.theme.TextSecondary
import com.example.trackio.ui.theme.TextTertiary

@Composable
fun WishlistTab(
    wishlistItems: List<WishlistItemEntity>,
    calculation: SafeSpendCalculationResult,
    onAddWishlistItem: (String, Double, String?) -> Unit,
    onTogglePurchased: (WishlistItemEntity) -> Unit,
    onDeleteWishlistItem: (WishlistItemEntity) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var itemName by remember { mutableStateOf("") }
    var itemPrice by remember { mutableStateOf("") }
    var itemNotes by remember { mutableStateOf("") }

    val remainingBuffer = calculation.remainingBalance.coerceAtLeast(0.0)
    var running = 0.0
    val affordable = wishlistItems
    .filter { !it.isPurchased }
    .associate { item -> 
        running += item.targetPrice
        item.id to (running <= remainingBuffer)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Title Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Wishlist & Impact",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
                Text(
                    text = "Opportunity-cost copilot before you buy",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )
            }

            Button(
                onClick = { showAddDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = ElectricIndigo),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add Wish", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (wishlistItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.CardGiftcard,
                        contentDescription = null,
                        tint = TextTertiary,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Your Wishlist is Empty",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextSecondary)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Add things you want to buy. Trackio will calculate how they impact your safe daily spend.",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextTertiary),
                        modifier = Modifier.padding(horizontal = 32.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(wishlistItems, key = { it.id }) { item ->
                    WishlistCard(
                        item = item,
                        remainingBuffer = remainingBuffer,
                        daysLeft = calculation.daysLeftInCycle,
                        currentSafeDaily = calculation.safeDailySpend,
                        onTogglePurchased = { onTogglePurchased(item) },
                        onDelete = { onDeleteWishlistItem(item) }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = {
                showAddDialog = false
                itemName = ""
                itemPrice = ""
                itemNotes = ""
            },
            containerColor = DarkSurface,
            title = {
                Text(
                    text = "Add Wishlist Item",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
            },
            text = {
                Column {
                    Text(
                        text = "Evaluate purchase impact on your remaining allowance.",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedTextField(
                        value = itemName,
                        onValueChange = { itemName = it },
                        label = { Text("Item Name (e.g. Headphones, Shoes)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricIndigo,
                            unfocusedBorderColor = DarkCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = itemPrice,
                        onValueChange = { itemPrice = it },
                        label = { Text("Target Price (₹)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricIndigo,
                            unfocusedBorderColor = DarkCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = itemNotes,
                        onValueChange = { itemNotes = it },
                        label = { Text("Notes / Store Link [Optional]") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricIndigo,
                            unfocusedBorderColor = DarkCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val price = itemPrice.toDoubleOrNull()
                        if (itemName.isNotBlank() && price != null && price > 0) {
                            onAddWishlistItem(itemName.trim(), price, itemNotes.trim().ifBlank { null })
                            showAddDialog = false
                            itemName = ""
                            itemPrice = ""
                            itemNotes = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricIndigo)
                ) {
                    Text("Add to Wishlist", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAddDialog = false
                        itemName = ""
                        itemPrice = ""
                        itemNotes = ""
                    }
                ) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
fun WishlistCard(
    item: WishlistItemEntity,
    remainingBuffer: Double,
    daysLeft: Int,
    currentSafeDaily: Double,
    onTogglePurchased: () -> Unit,
    onDelete: () -> Unit
) {
    val impactPct = if (remainingBuffer > 0) {
        ((item.targetPrice / remainingBuffer) * 100).toInt()
    } else 100

    val newSafeDaily = if (daysLeft > 0) {
        ((remainingBuffer - item.targetPrice) / daysLeft).coerceAtLeast(0.0)
    } else 0.0

    val isAffordableNow = remainingBuffer >= item.targetPrice

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isPurchased) DarkSurface.copy(alpha = 0.6f) else DarkSurface
        ),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (item.isPurchased) MintAccent.copy(alpha = 0.4f) else DarkCardBorder,
                RoundedCornerShape(20.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    IconButton(onClick = onTogglePurchased) {
                        Icon(
                            imageVector = if (item.isPurchased) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
                            contentDescription = if (item.isPurchased) "Purchased" else "Mark Purchased",
                            tint = if (item.isPurchased) MintAccent else TextTertiary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Column {
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (item.isPurchased) TextSecondary else TextPrimary,
                                textDecoration = if (item.isPurchased) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                            )
                        )
                        if (!item.notesOrUrl.isNullOrBlank()) {
                            Text(
                                text = item.notesOrUrl,
                                style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary),
                                maxLines = 1
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "₹${item.targetPrice.toInt()}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = if (item.isPurchased) MintAccent else TextPrimary
                        )
                    )

                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = TextTertiary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            if (!item.isPurchased) {
                Spacer(modifier = Modifier.height(12.dp))

                // Financial Impact Assessment Card
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isAffordableNow) {
                        if (impactPct > 50) StatusAmber.copy(alpha = 0.12f) else ElectricIndigo.copy(alpha = 0.12f)
                    } else StatusRed.copy(alpha = 0.12f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (isAffordableNow) "IMPACT ON CYCLE" else "OVER CURRENT BUDGET",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isAffordableNow) (if (impactPct > 50) StatusAmber else ElectricIndigo) else StatusRed,
                                    letterSpacing = 0.8.sp
                                )
                            )
                            Text(
                                text = if (isAffordableNow) "Uses $impactPct% of left buffer" else "Short by ₹${(item.targetPrice - remainingBuffer).toInt()}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isAffordableNow) (if (impactPct > 50) StatusAmber else ElectricIndigo) else StatusRed
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = if (isAffordableNow) {
                                "Buying today drops your safe daily spend from ₹${currentSafeDaily.toInt()} to ₹${newSafeDaily.toInt()}."
                            } else {
                                "Buying this now exceeds your remaining allowance. Recommend postponing to next cycle."
                            },
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        )
                    }
                }
            }
        }
    }
}
