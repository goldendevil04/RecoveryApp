
package com.coderx.datarescuepro.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.coderx.datarescuepro.core.FileRecoveryEngine
import com.coderx.datarescuepro.data.model.FileType
import com.coderx.datarescuepro.data.model.RecoverableFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedFileList(
    files: List<RecoverableFile>,
    onRecoverClick: (RecoverableFile) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(files) { file ->
            EnhancedFileItem(
                file = file,
                onRecoverClick = { onRecoverClick(file) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedFileItem(
    file: RecoverableFile,
    onRecoverClick: () -> Unit
) {
    val context = LocalContext.current
    val fileRecoveryEngine = remember { FileRecoveryEngine() }
    var previewBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoadingPreview by remember { mutableStateOf(false) }

    LaunchedEffect(file.id) {
        if (file.type in listOf(FileType.JPEG, FileType.PNG, FileType.GIF, FileType.MP4)) {
            isLoadingPreview = true
            withContext(Dispatchers.IO) {
                try {
                    previewBitmap = fileRecoveryEngine.generatePreview(file)
                } catch (e: Exception) {
                    // Preview generation failed
                } finally {
                    isLoadingPreview = false
                }
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Preview/Icon Section
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                when {
                    isLoadingPreview -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    }
                    previewBitmap != null -> {
                        Image(
                            bitmap = previewBitmap!!.asImageBitmap(),
                            contentDescription = "File preview",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    else -> {
                        Icon(
                            imageVector = getFileTypeIcon(file.type),
                            contentDescription = "File type",
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // File Info Section
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = file.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = file.formattedSize,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    RecoveryConfidenceBadge(confidence = file.recoveryConfidence)
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = file.formattedDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = file.recoveryCategory.name.replace("_", " "),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
            
            // Action Section
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Button(
                    onClick = onRecoverClick,
                    modifier = Modifier.height(36.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.GetApp,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Recover",
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun RecoveryConfidenceBadge(confidence: Float) {
    val color = when {
        confidence >= 0.8f -> MaterialTheme.colorScheme.primary
        confidence >= 0.6f -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.error
    }
    
    val text = when {
        confidence >= 0.8f -> "High"
        confidence >= 0.6f -> "Medium"
        else -> "Low"
    }
    
    Surface(
        modifier = Modifier.clip(RoundedCornerShape(12.dp)),
        color = color.copy(alpha = 0.1f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun FileTypeFilterChips(
    selectedFilter: String,
    onFilterSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val filters = listOf(
        "All" to Icons.Default.SelectAll,
        "Images" to Icons.Default.Image,
        "Videos" to Icons.Default.VideoFile,
        "Audio" to Icons.Default.AudioFile,
        "Documents" to Icons.Default.Description
    )
    
    LazyColumn(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filters.forEach { (filter, icon) ->
                    FilterChip(
                        onClick = { onFilterSelected(filter) },
                        label = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(filter)
                            }
                        },
                        selected = selectedFilter == filter,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }
        }
    }
}

private fun getFileTypeIcon(fileType: FileType): ImageVector {
    return when (fileType) {
        FileType.JPEG, FileType.PNG, FileType.GIF, FileType.IMAGE -> Icons.Default.Image
        FileType.MP4, FileType.VIDEO -> Icons.Default.VideoFile
        FileType.MP3, FileType.AUDIO -> Icons.Default.AudioFile
        FileType.PDF, FileType.DOC, FileType.DOCX, FileType.DOCUMENT -> Icons.Default.Description
        FileType.ZIP -> Icons.Default.Archive
        FileType.XLS, FileType.XLSX -> Icons.Default.TableChart
        else -> Icons.Default.InsertDriveFile
    }
}
