package com.coderx.datarescuepro.ui.screens.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.coderx.datarescuepro.ui.components.AdBanner
import com.coderx.datarescuepro.ui.components.FileTypeCard

data class FileTypeItem(
    val type: String,
    val displayName: String,
    val icon: ImageVector,
    val description: String
)

@Composable
fun DashboardScreen(
    onNavigateToScan: (String) -> Unit,
    viewModel: DashboardViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    val fileTypes = listOf(
        FileTypeItem("photos", "Photos", Icons.Default.Image, "JPG, PNG, GIF, HEIC"),
        FileTypeItem("videos", "Videos", Icons.Default.VideoFile, "MP4, AVI, MOV, MKV"),
        FileTypeItem("documents", "Documents", Icons.Default.Description, "PDF, DOC, XLS, PPT"),
        FileTypeItem("audio", "Audio", Icons.Default.AudioFile, "MP3, WAV, AAC, FLAC")
    )
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "DataRescue Pro",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Root Status Card
        RootStatusCard(
            isRooted = uiState.isRooted,
            isRootEnabled = uiState.isRootEnabled,
            onRootToggle = viewModel::toggleRootAccess
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Select File Type to Recover",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.SemiBold
            )
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // File Type Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(fileTypes) { fileType ->
                FileTypeCard(
                    fileType = fileType,
                    onClick = { onNavigateToScan(fileType.type) }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Ad Banner
        AdBanner()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RootStatusCard(
    isRooted: Boolean,
    isRootEnabled: Boolean,
    onRootToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isRooted) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isRooted) "Root Access Available" else "Standard Mode",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Text(
                        text = if (isRooted) {
                            if (isRootEnabled) "Enhanced recovery enabled" else "Tap to enable enhanced recovery"
                        } else {
                            "Basic file recovery available"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                if (isRooted) {
                    Switch(
                        checked = isRootEnabled,
                        onCheckedChange = onRootToggle
                    )
                }
            }
        }
    }
}