package com.example.trackio.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.ui.graphics.vector.ImageVector

object CategoryIconHelper {
    fun getIcon(iconName: String): ImageVector {
        return when (iconName) {
            "Restaurant" -> Icons.Default.Restaurant
            "ShoppingBag" -> Icons.Default.ShoppingBag
            "MenuBook" -> Icons.Default.MenuBook
            "DirectionsBus" -> Icons.Default.DirectionsBus
            "Receipt" -> Icons.Default.Receipt
            "CardGiftcard" -> Icons.Default.CardGiftcard
            "Engineering" -> Icons.Default.Engineering
            else -> Icons.Default.Category
        }
    }
}
