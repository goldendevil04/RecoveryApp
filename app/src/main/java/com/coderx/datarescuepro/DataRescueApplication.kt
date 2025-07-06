package com.coderx.datarescuepro

import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DataRescueApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize AdMob
        initializeAdMob()
    }
    
    private fun initializeAdMob() {
        CoroutineScope(Dispatchers.IO).launch {
            MobileAds.initialize(this@DataRescueApplication) { initializationStatus ->
                // AdMob initialization complete
            }
            
            // Set test device IDs for development
            val testDeviceIds = listOf("33BE2250B43518CCDA7DE426D04EE231")
            val configuration = RequestConfiguration.Builder()
                .setTestDeviceIds(testDeviceIds)
                .build()
            MobileAds.setRequestConfiguration(configuration)
        }
    }
}