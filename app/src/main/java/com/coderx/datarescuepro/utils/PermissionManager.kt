
package com.coderx.datarescuepro.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PermissionManager {
    companion object {
        const val STORAGE_PERMISSION_CODE = 1001
        const val MANAGE_STORAGE_PERMISSION_CODE = 1002
        const val PHONE_STATE_PERMISSION_CODE = 1003
        
        fun hasStoragePermissions(context: Context): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                hasMediaPermissions(context)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Environment.isExternalStorageManager()
            } else {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            }
        }
        
        private fun hasMediaPermissions(context: Context): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_MEDIA_IMAGES
                ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_MEDIA_VIDEO
                ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_MEDIA_AUDIO
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        }
        
        fun requestStoragePermissions(activity: Activity) {
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                    ActivityCompat.requestPermissions(
                        activity,
                        arrayOf(
                            Manifest.permission.READ_MEDIA_IMAGES,
                            Manifest.permission.READ_MEDIA_VIDEO,
                            Manifest.permission.READ_MEDIA_AUDIO
                        ),
                        STORAGE_PERMISSION_CODE
                    )
                }
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                    requestManageExternalStorage(activity)
                }
                else -> {
                    ActivityCompat.requestPermissions(
                        activity,
                        arrayOf(
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ),
                        STORAGE_PERMISSION_CODE
                    )
                }
            }
        }
        
        private fun requestManageExternalStorage(activity: Activity) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.data = Uri.parse("package:${activity.packageName}")
                activity.startActivityForResult(intent, MANAGE_STORAGE_PERMISSION_CODE)
            } catch (e: Exception) {
                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                activity.startActivityForResult(intent, MANAGE_STORAGE_PERMISSION_CODE)
            }
        }
        
        fun requestPhoneStatePermission(activity: Activity) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.READ_PHONE_STATE),
                PHONE_STATE_PERMISSION_CODE
            )
        }
        
        fun hasPhoneStatePermission(context: Context): Boolean {
            return ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_PHONE_STATE
            ) == PackageManager.PERMISSION_GRANTED
        }
        
        fun getManufacturerSpecificPermissions(): List<String> {
            val manufacturer = Build.MANUFACTURER.lowercase()
            return when {
                manufacturer.contains("samsung") -> listOf(
                    "com.samsung.android.permission.WRITE_EXTERNAL_STORAGE"
                )
                manufacturer.contains("xiaomi") || manufacturer.contains("redmi") -> listOf(
                    "miui.permission.USE_INTERNAL_GENERAL_API"
                )
                manufacturer.contains("huawei") || manufacturer.contains("honor") -> listOf(
                    "com.huawei.permission.external_app_settings.USE_COMPONENT"
                )
                manufacturer.contains("oneplus") -> listOf(
                    "oneplus.permission.OP_CAMERA"
                )
                manufacturer.contains("oppo") -> listOf(
                    "oppo.permission.OPPO_COMPONENT_SAFE"
                )
                manufacturer.contains("vivo") -> listOf(
                    "com.vivo.permissionmanager.permission.ACCESS"
                )
                else -> emptyList()
            }
        }
    }
}
