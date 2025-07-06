
package com.coderx.datarescuepro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.coderx.datarescuepro.core.FileRecoveryEngine
import com.coderx.datarescuepro.data.model.DeviceInfo
import com.coderx.datarescuepro.ui.components.AnimatedPulse
import com.coderx.datarescuepro.ui.components.GradientBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onNavigateToScan: () -> Unit
) {
    val context = LocalContext.current
    val fileRecoveryEngine = remember { FileRecoveryEngine() }
    var deviceInfo by remember { mutableStateOf<DeviceInfo?>(null) }
    var isRooted by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        deviceInfo = DeviceInfo.getCurrent()
        isRooted = fileRecoveryEngine.detectRootAccess()
    }

    GradientBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "📱",
                        style = MaterialTheme.typography.displayLarge
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "DataRescue Pro",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Text(
                        text = "Professional File Recovery",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Device Information
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Storage,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Device Information",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    deviceInfo?.let { info ->
                        DeviceInfoItem(
                            label = "Device",
                            value = "${info.manufacturer} ${info.model}"
                        )
                        DeviceInfoItem(
                            label = "Android Version",
                            value = "Android ${info.androidVersion} (API ${info.apiLevel})"
                        )
                        DeviceInfoItem(
                            label = "Storage",
                            value = "${info.formatStorage(info.availableStorage)} / ${info.formatStorage(info.totalStorage)}"
                        )
                        DeviceInfoItem(
                            label = "CPU Architecture",
                            value = info.cpuArchitecture
                        )
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = if (isRooted) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = if (isRooted) "Root Access Detected" else "No Root Access",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isRooted) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Quick Stats
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickStatCard(
                    title = "Recovery Rate",
                    value = "85%",
                    modifier = Modifier.weight(1f)
                )
                QuickStatCard(
                    title = "Scan Speed",
                    value = "Fast",
                    modifier = Modifier.weight(1f)
                )
                QuickStatCard(
                    title = "File Types",
                    value = "12+",
                    modifier = Modifier.weight(1f)
                )
            }

            // Main Action Button
            AnimatedPulse {
                Button(
                    onClick = onNavigateToScan,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = "Start Recovery Scan",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Features List
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Recovery Features",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    FeatureItem("📷", "Photo Recovery", "Recover deleted photos from gallery")
                    FeatureItem("🎵", "Audio Recovery", "Restore lost music and audio files")
                    FeatureItem("🎬", "Video Recovery", "Recover deleted video files")
                    FeatureItem("📄", "Document Recovery", "Restore lost documents and files")
                    FeatureItem("🗂️", "Deep Scan", "Advanced recovery algorithms")
                }
            }
        }
    }
}

@Composable
private fun DeviceInfoItem(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun QuickStatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FeatureItem(
    icon: String,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = icon,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(end = 12.dp)
        )
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
