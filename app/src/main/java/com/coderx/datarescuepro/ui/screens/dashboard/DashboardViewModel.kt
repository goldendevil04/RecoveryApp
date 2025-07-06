package com.coderx.datarescuepro.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coderx.datarescuepro.core.recovery.FileRecoveryEngine
import com.coderx.datarescuepro.core.system.RootDetector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DashboardUiState(
    val isRooted: Boolean = false,
    val isRootEnabled: Boolean = false,
    val isLoading: Boolean = true
)

class DashboardViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()
    
    private val rootDetector = RootDetector()
    private val recoveryEngine = FileRecoveryEngine()
    
    init {
        checkRootStatus()
    }
    
    private fun checkRootStatus() {
        viewModelScope.launch {
            val isRooted = rootDetector.isDeviceRooted()
            _uiState.value = _uiState.value.copy(
                isRooted = isRooted,
                isLoading = false
            )
        }
    }
    
    fun toggleRootAccess(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isRootEnabled = enabled)
        recoveryEngine.setRootAccess(enabled)
    }
}