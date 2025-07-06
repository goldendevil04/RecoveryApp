
package com.coderx.datarescuepro.utils

import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.provider.Settings
import android.telephony.TelephonyManager
import java.io.File

data class DeviceInfo(
    val deviceName: String,
    val manufacturer: String,
    val model: String,
    val androidVersion: String,
    val apiLevel: Int,
    val totalStorage: Long,
    val availableStorage: Long,
    val ramInfo: String,
    val deviceId: String,
    val isRooted: Boolean,
    val cpuArchitecture: String,
    val kernelVersion: String
)

class DeviceInfoProvider {
    companion object {
        fun getDeviceInfo(context: Context, isRooted: Boolean): DeviceInfo {
            return DeviceInfo(
                deviceName = getDeviceName(),
                manufacturer = Build.MANUFACTURER,
                model = Build.MODEL,
                androidVersion = Build.VERSION.RELEASE,
                apiLevel = Build.VERSION.SDK_INT,
                totalStorage = getTotalStorage(),
                availableStorage = getAvailableStorage(),
                ramInfo = getRamInfo(),
                deviceId = getDeviceId(context),
                isRooted = isRooted,
                cpuArchitecture = Build.SUPPORTED_ABIS.firstOrNull() ?: "Unknown",
                kernelVersion = System.getProperty("os.version") ?: "Unknown"
            )
        }
        
        private fun getDeviceName(): String {
            val manufacturer = Build.MANUFACTURER
            val model = Build.MODEL
            return if (model.lowercase().startsWith(manufacturer.lowercase())) {
                model.replaceFirstChar { it.uppercase() }
            } else {
                "${manufacturer.replaceFirstChar { it.uppercase() }} $model"
            }
        }
        
        private fun getTotalStorage(): Long {
            return try {
                val path = Environment.getDataDirectory()
                val stat = StatFs(path.path)
                stat.blockCountLong * stat.blockSizeLong
            } catch (e: Exception) {
                0L
            }
        }
        
        private fun getAvailableStorage(): Long {
            return try {
                val path = Environment.getDataDirectory()
                val stat = StatFs(path.path)
                stat.availableBlocksLong * stat.blockSizeLong
            } catch (e: Exception) {
                0L
            }
        }
        
        private fun getRamInfo(): String {
            return try {
                val memInfo = Runtime.getRuntime()
                val totalMemory = memInfo.totalMemory()
                val freeMemory = memInfo.freeMemory()
                val usedMemory = totalMemory - freeMemory
                "${formatBytes(usedMemory)} / ${formatBytes(totalMemory)}"
            } catch (e: Exception) {
                "Unknown"
            }
        }
        
        private fun getDeviceId(context: Context): String {
            return try {
                Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            } catch (e: Exception) {
                "Unknown"
            }
        }
        
        fun formatBytes(bytes: Long): String {
            val kb = bytes / 1024.0
            val mb = kb / 1024.0
            val gb = mb / 1024.0
            
            return when {
                gb >= 1 -> String.format("%.1f GB", gb)
                mb >= 1 -> String.format("%.1f MB", mb)
                kb >= 1 -> String.format("%.1f KB", kb)
                else -> "$bytes B"
            }
        }
    }
}
