package com.example.trackio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.trackio.ui.MainViewModel
import com.example.trackio.ui.components.HomeScreen
import com.example.trackio.ui.components.OnboardingScreen
import com.example.trackio.ui.components.QuickAddBottomSheet
import com.example.trackio.ui.components.SettingsBottomSheet
import com.example.trackio.ui.theme.ElectricIndigo
import com.example.trackio.ui.theme.TrackioTheme
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TrackioTheme {
                val viewModel: MainViewModel = viewModel()
                val uiState by viewModel.uiState.collectAsState()
                val coroutineScope = rememberCoroutineScope()

                var showQuickAddSheet by remember { mutableStateOf(false) }
                var showSettingsSheet by remember { mutableStateOf(false) }

                val quickAddSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                val settingsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = ElectricIndigo)
                    }
                } else if (!uiState.isOnboardingCompleted) {
                    OnboardingScreen(
                        onFinish = { allowance, cycleType, startDay, fixedItems ->
                            viewModel.finishOnboarding(
                                allowance = allowance,
                                cycleType = cycleType,
                                startDay = startDay,
                                initialFixedItems = fixedItems
                            )
                        }
                    )
                } else {
                    uiState.dashboard?.let { dashboard ->
                        HomeScreen(
                            dashboardState = dashboard,
                            categories = uiState.categories,
                            projects = uiState.projects,
                            wishlistItems = uiState.wishlistItems,
                            onQuickAddClick = { showQuickAddSheet = true },
                            onSettingsClick = { showSettingsSheet = true },
                            onAddProject = { name, cap -> viewModel.addProject(name, cap) },
                            onDeleteProject = { project -> viewModel.deleteProject(project) },
                            onAddWishlistItem = { name, price, notes -> viewModel.addWishlistItem(name, price, notes) },
                            onToggleWishlistPurchased = { item -> viewModel.toggleWishlistPurchased(item) },
                            onDeleteWishlistItem = { item -> viewModel.deleteWishlistItem(item) }
                        )
                    }
                }

                if (showQuickAddSheet) {
                    QuickAddBottomSheet(
                        sheetState = quickAddSheetState,
                        categories = uiState.categories,
                        onDismiss = { showQuickAddSheet = false },
                        onAddExpense = { amount, vendor, categoryId, tag ->
                            viewModel.quickAddTransaction(amount, vendor, categoryId, tag)
                        }
                    )
                }

                if (showSettingsSheet) {
                    SettingsBottomSheet(
                        sheetState = settingsSheetState,
                        onDismiss = { showSettingsSheet = false },
                        onExportCsv = {
                            var exported = ""
                            runBlocking { exported = viewModel.exportCsv() }
                            exported
                        },
                        onImportCsv = { csv ->
                            viewModel.importCsv(csv)
                        },
                        onDeleteAllData = {
                            viewModel.resetAllData()
                        }
                    )
                }
            }
        }
    }
}