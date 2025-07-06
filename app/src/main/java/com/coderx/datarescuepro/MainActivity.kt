package com.coderx.datarescuepro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.coderx.datarescuepro.navigation.DataRescueNavigation
import com.coderx.datarescuepro.ui.theme.DataRescueProTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            DataRescueProTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    DataRescueNavigation(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}