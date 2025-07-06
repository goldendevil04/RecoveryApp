package com.coderx.datarescuepro.data.model

import java.text.SimpleDateFormat
import java.util.*

enum class FileType {
    UNKNOWN, JPEG, PNG, GIF, MP4, MP3, PDF, ZIP, DOC, DOCX, XLS, XLSX, VIDEO, AUDIO, IMAGE, DOCUMENT
}

enum class RecoveryCategory {
    MEDIA_STORE, RECENTLY_DELETED, CACHE_FILES, DEEP_SCAN, ROOT_SCAN
}

data class RecoverableFile(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val path: String,
    val size: Long,
    val type: FileType,
    val lastModified: Long,
    val isRecoverable: Boolean = true,
    val recoveryLocation: String,
    val recoveryConfidence: Float,
    val recoveryCategory: RecoveryCategory,
    val thumbnailPath: String? = null
) {
    val formattedSize: String
        get() {
            val bytes = size
            val unit = 1024
            if (bytes < unit) return "$bytes B"
            val exp = (Math.log(bytes.toDouble()) / Math.log(unit.toDouble())).toInt()
            val pre = "KMGTPE"[exp - 1]
            return String.format("%.1f %sB", bytes / Math.pow(unit.toDouble(), exp.toDouble()), pre)
        }

    val formattedDate: String
        get() = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(lastModified))

    val confidenceColor: androidx.compose.ui.graphics.Color
        get() = when {
            recoveryConfidence >= 0.8f -> androidx.compose.ui.graphics.Color(0xFF4CAF50)
            recoveryConfidence >= 0.6f -> androidx.compose.ui.graphics.Color(0xFFFF9800)
            else -> androidx.compose.ui.graphics.Color(0xFFE53935)
        }
}