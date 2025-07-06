package com.coderx.datarescuepro.viewmodel

import android.content.Context
import android.os.Environment
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coderx.datarescuepro.core.FileRecoveryEngine
import com.coderx.datarescuepro.data.model.FileType
import com.coderx.datarescuepro.data.model.RecoverableFile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.io.File

class ResultsViewModel : ViewModel() {
    private val fileRecoveryEngine = FileRecoveryEngine()

    private val _recoveredFiles = MutableStateFlow<List<RecoverableFile>>(emptyList())
    val recoveredFiles: StateFlow<List<RecoverableFile>> = _recoveredFiles

    private val _selectedFilter = MutableStateFlow("all")
    val selectedFilter: StateFlow<String> = _selectedFilter

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    val filteredFiles = combine(
        recoveredFiles,
        selectedFilter
    ) { files, filter ->
        when (filter) {
            "all" -> files
            "images" -> files.filter { it.type == FileType.IMAGE }
            "videos" -> files.filter { it.type == FileType.VIDEO }
            "audio" -> files.filter { it.type == FileType.AUDIO }
            "documents" -> files.filter { it.type == FileType.DOCUMENT }
            "others" -> files.filter { it.type !in listOf(FileType.IMAGE, FileType.VIDEO, FileType.AUDIO, FileType.DOCUMENT) }
            else -> files
        }
    }

    fun loadRecoveredFiles(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val isRooted = fileRecoveryEngine.detectRootAccess()
                val files = fileRecoveryEngine.performFullScan(context, isRooted)
                _recoveredFiles.value = files
            } catch (e: Exception) {
                Toast.makeText(context, "Error loading files: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setFilter(filter: String) {
        _selectedFilter.value = filter
    }

    fun recoverFile(context: Context, file: RecoverableFile) {
        viewModelScope.launch {
            try {
                val recoveryDir = File(Environment.getExternalStorageDirectory(), "DataRescue/Recovered")
                if (!recoveryDir.exists()) {
                    recoveryDir.mkdirs()
                }

                val success = fileRecoveryEngine.recoverFile(file, recoveryDir)

                if (success) {
                    Toast.makeText(context, "File recovered successfully to DataRescue folder", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Failed to recover file", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error recovering file: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
