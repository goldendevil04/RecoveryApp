package com.coderx.datarescuepro.ui.screens.results

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.coderx.datarescuepro.ui.components.AdBanner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultsScreen(
    onNavigateToRecovery: (List<String>) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: ResultsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recovery Results") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Results Summary
            ResultsSummaryCard(
                totalFiles = uiState.foundFiles.size,
                selectedFiles = uiState.selectedFiles.size
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Select All/None
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Select files to recover:",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
                
                Row {
                    IconButton(
                        onClick = { viewModel.selectAll() }
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Select All",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    IconButton(
                        onClick = { viewModel.selectNone() }
                    ) {
                        Icon(
                            Icons.Default.RadioButtonUnchecked,
                            contentDescription = "Select None"
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Files List
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.foundFiles) { fileName ->
                    FileResultItem(
                        fileName = fileName,
                        isSelected = fileName in uiState.selectedFiles,
                        onSelectionChange = { selected ->
                            if (selected) {
                                viewModel.selectFile(fileName)
                            } else {
                                viewModel.deselectFile(fileName)
                            }
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Recover Button
            Button(
                onClick = { onNavigateToRecovery(uiState.selectedFiles) },
                enabled = uiState.selectedFiles.isNotEmpty(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Recover Selected Files (${uiState.selectedFiles.size})")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Ad Banner
            AdBanner()
        }
    }
}

@Composable
private fun ResultsSummaryCard(
    totalFiles: Int,
    selectedFiles: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Scan Complete!",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Found $totalFiles recoverable files",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            if (selectedFiles > 0) {
                Text(
                    text = "$selectedFiles files selected for recovery",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FileResultItem(
    fileName: String,
    isSelected: Boolean,
    onSelectionChange: (Boolean) -> Unit
) {
    Card(
        onClick = { onSelectionChange(!isSelected) },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.secondaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = onSelectionChange
            )
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                Text(
                    text = fileName,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium
                    )
                )
                
                Text(
                    text = "Size: ${(Math.random() * 10).toInt() + 1} MB",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}