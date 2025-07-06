package com.coderx.datarescuepro.ui.screens.scan

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.coderx.datarescuepro.ui.components.AdBanner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(
    fileType: String,
    onNavigateToResults: (List<String>) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: ScanViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(fileType) {
        viewModel.setFileType(fileType)
    }
    
    LaunchedEffect(uiState.isCompleted) {
        if (uiState.isCompleted) {
            onNavigateToResults(uiState.foundFiles)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scanning ${fileType.capitalize()}") },
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            
            // Scanning Animation
            ScanningAnimation(isScanning = uiState.isScanning)
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Progress Card
            ScanProgressCard(
                progress = uiState.progress,
                currentPath = uiState.currentPath,
                filesFound = uiState.foundFiles.size,
                isScanning = uiState.isScanning
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Start/Stop Button
            if (!uiState.isScanning && !uiState.isCompleted) {
                Button(
                    onClick = { viewModel.startScan() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Start Scan")
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Ad Banner
            AdBanner()
        }
    }
}

@Composable
private fun ScanningAnimation(isScanning: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "scanning")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    Box(
        modifier = Modifier.size(120.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isScanning) {
            CircularProgressIndicator(
                modifier = Modifier.size(100.dp),
                strokeWidth = 6.dp
            )
        }
        
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Scanning",
            modifier = Modifier
                .size(60.dp)
                .rotate(if (isScanning) rotation else 0f),
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun ScanProgressCard(
    progress: Float,
    currentPath: String,
    filesFound: Int,
    isScanning: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Progress",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Files Found: $filesFound",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.primary
            )
            
            if (isScanning && currentPath.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Scanning: $currentPath",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}