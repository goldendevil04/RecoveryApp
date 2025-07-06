package com.coderx.datarescuepro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.coderx.datarescuepro.data.model.RecoverableFile
import com.coderx.datarescuepro.ui.components.FilePreviewDialog
import com.coderx.datarescuepro.ui.components.RecoverableFileItem
import com.coderx.datarescuepro.viewmodel.ResultsViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultsScreen(
    onNavigateBack: () -> Unit,
    viewModel: ResultsViewModel = viewModel(
        initializer = { ResultsViewModel() }
    )
) {
    val context = LocalContext.current
    val recoveredFiles by viewModel.recoveredFiles.collectAsState()
    val filteredFiles by viewModel.filteredFiles.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var showPreviewDialog by remember { mutableStateOf(false) }
    var selectedFile by remember { mutableStateOf<RecoverableFile?>(null) }
    var showFilterMenu by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadRecoveredFiles(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Recovered Files (${filteredFiles.size})",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showFilterMenu = true }) {
                            Icon(Icons.Default.FilterList, contentDescription = "Filter")
                        }

                        DropdownMenu(
                            expanded = showFilterMenu,
                            onDismissRequest = { showFilterMenu = false }
                        ) {
                            val filters = listOf("All", "Images", "Videos", "Audio", "Documents", "Others")
                            filters.forEach { filter ->
                                DropdownMenuItem(
                                    text = { Text(filter) },
                                    onClick = {
                                        viewModel.setFilter(filter.lowercase())
                                        showFilterMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                filteredFiles.isEmpty() -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No recoverable files found",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Try running a new scan or check your storage permissions",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(filteredFiles) { file ->
                            RecoverableFileItem(
                                file = file,
                                onPreview = { 
                                    selectedFile = it
                                    showPreviewDialog = true
                                },
                                onRecover = { viewModel.recoverFile(context, it) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Preview dialog
    if (showPreviewDialog && selectedFile != null) {
        FilePreviewDialog(
            file = selectedFile!!,
            onDismiss = { 
                showPreviewDialog = false
                selectedFile = null
            },
            onRecover = { file ->
                viewModel.recoverFile(context, file)
                showPreviewDialog = false
                selectedFile = null
            }
        )
    }
}