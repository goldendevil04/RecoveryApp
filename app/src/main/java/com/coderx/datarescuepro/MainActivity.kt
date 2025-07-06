package com.coderx.datarescuepro

import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.coderx.datarescuepro.ui.navigation.DataRescueNavigation
import com.coderx.datarescuepro.ui.theme.DataRescueProTheme
import com.coderx.datarescuepro.utils.PermissionManager

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check and request permissions
        checkPermissions()

        setContent {
            DataRescueProTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DataRescueNavigation()
                }
            }
        }
    }

    private fun checkPermissions() {
        if (!PermissionManager.hasStoragePermissions(this)) {
            PermissionManager.requestStoragePermissions(this)
        }

        if (!PermissionManager.hasPhoneStatePermission(this)) {
            PermissionManager.requestPhoneStatePermission(this)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PermissionManager.STORAGE_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Storage permissions granted", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Storage permissions required for file recovery", Toast.LENGTH_LONG).show()
                }
            }
            PermissionManager.PHONE_STATE_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Device info access granted", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}