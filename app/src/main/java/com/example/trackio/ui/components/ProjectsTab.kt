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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trackio.data.local.entity.ProjectEntity
import com.example.trackio.data.local.entity.TransactionDirection
import com.example.trackio.data.local.entity.TransactionEntity
import com.example.trackio.data.local.entity.TransactionStatus
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
fun ProjectsTab(
    projects: List<ProjectEntity>,
    transactions: List<TransactionEntity>,
    onAddProject: (String, Double?) -> Unit,
    onDeleteProject: (ProjectEntity) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var projectName by remember { mutableStateOf("") }
    var projectCap by remember { mutableStateOf("") }

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
                    text = "Project Budgets",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
                Text(
                    text = "Track tagged spending goals & events",
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
                Text("New Project", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (projects.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = null,
                        tint = TextTertiary,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No projects yet",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextSecondary)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Create buckets like 'Goa Trip' or 'Hackathon' to cap your spending.",
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
                items(projects, key = { it.id }) { project ->
                    // Calculate spending for this project tag
                    val normalizedName = project.name.trim().lowercase()
                    val taggedTxns = transactions.filter { txn ->
                        val txnTag = txn.tag?.trim()?.removePrefix("#")?.lowercase()
                        txnTag == normalizedName &&
                                txn.direction == TransactionDirection.PAID &&
                                txn.status == TransactionStatus.CONFIRMED
                    }
                    val spentAmount = taggedTxns.sumOf { it.amount }
                    val cap = project.capAmount

                    ProjectCard(
                        project = project,
                        spentAmount = spentAmount,
                        capAmount = cap,
                        transactionCount = taggedTxns.size,
                        onDelete = { onDeleteProject(project) }
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
                projectName = ""
                projectCap = ""
            },
            containerColor = DarkSurface,
            title = {
                Text(
                    text = "Create Project Budget",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
            },
            text = {
                Column {
                    Text(
                        text = "Set a tagged bucket to cap expenses for specific occasions.",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedTextField(
                        value = projectName,
                        onValueChange = { projectName = it },
                        label = { Text("Project Name (e.g. Goa Trip, Books)") },
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
                        value = projectCap,
                        onValueChange = { projectCap = it },
                        label = { Text("Spending Cap (₹) [Optional]") },
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
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cap = projectCap.toDoubleOrNull()
                        if (projectName.isNotBlank()) {
                            onAddProject(projectName.trim(), cap)
                            showAddDialog = false
                            projectName = ""
                            projectCap = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricIndigo)
                ) {
                    Text("Save Project", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAddDialog = false
                        projectName = ""
                        projectCap = ""
                    }
                ) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
fun ProjectCard(
    project: ProjectEntity,
    spentAmount: Double,
    capAmount: Double?,
    transactionCount: Int,
    onDelete: () -> Unit
) {
    val progress = if (capAmount != null && capAmount > 0) {
        (spentAmount / capAmount).coerceIn(0.0, 1.0).toFloat()
    } else 0f

    val isOverBudget = capAmount != null && spentAmount > capAmount

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, DarkCardBorder, RoundedCornerShape(20.dp))
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(ElectricIndigo.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tag,
                            contentDescription = null,
                            tint = ElectricIndigo,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "#${project.name}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                        Text(
                            text = "$transactionCount expenses tagged",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                        )
                    }
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = TextTertiary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "TOTAL SPENT",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            letterSpacing = 1.sp
                        )
                    )
                    Text(
                        text = "₹${spentAmount.toInt()}",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isOverBudget) StatusRed else TextPrimary
                        )
                    )
                }

                if (capAmount != null) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = if (isOverBudget) "OVER CAP BY" else "REMAINING",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary,
                                letterSpacing = 1.sp
                            )
                        )
                        Text(
                            text = if (isOverBudget) "₹${(spentAmount - capAmount).toInt()}" else "₹${(capAmount - spentAmount).toInt()}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isOverBudget) StatusRed else MintAccent
                            )
                        )
                    }
                }
            }

            if (capAmount != null && capAmount > 0) {
                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (isOverBudget) StatusRed else if (progress > 0.8f) StatusAmber else MintAccent,
                    trackColor = DarkCardBorder
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${(progress * 100).toInt()}% of ₹${capAmount.toInt()} cap",
                        style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                    )
                    if (isOverBudget) {
                        Text(
                            text = "Exceeded",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = StatusRed
                            )
                        )
                    }
                }
            }
        }
    }
}
