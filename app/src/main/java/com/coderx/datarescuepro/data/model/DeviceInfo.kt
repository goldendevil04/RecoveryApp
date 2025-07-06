
package com.coderx.datarescuepro.data.model

import android.os.Build
import android.os.Environment
import android.os.StatFs

data class DeviceInfo(
    val manufacturer: String,
    val model: String,
    val androidVersion: String,
    val apiLevel: Int,
    val availableStorage: Long,
    val totalStorage: Long,
    val cpuArchitecture: String
) {
    companion object {
        fun getCurrent(): DeviceInfo {
            val stat = StatFs(Environment.getExternalStorageDirectory().path)
            val availableBytes = stat.availableBytes
            val totalBytes = stat.totalBytes

            return DeviceInfo(
                manufacturer = Build.MANUFACTURER,
                model = Build.MODEL,
                androidVersion = Build.VERSION.RELEASE,
                apiLevel = Build.VERSION.SDK_INT,
                availableStorage = availableBytes,
                totalStorage = totalBytes,
                cpuArchitecture = Build.SUPPORTED_ABIS[0]
            )
        }
    }

    fun formatStorage(bytes: Long): String {
        val unit = 1024
        if (bytes < unit) return "$bytes B"
        val exp = (Math.log(bytes.toDouble()) / Math.log(unit.toDouble())).toInt()
        val pre = "KMGTPE"[exp - 1]
        return String.format("%.1f %sB", bytes / Math.pow(unit.toDouble(), exp.toDouble()), pre)
    }
}
