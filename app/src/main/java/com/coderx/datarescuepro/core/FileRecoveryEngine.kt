
package com.coderx.datarescuepro.core

import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import com.coderx.datarescuepro.data.model.FileType
import com.coderx.datarescuepro.data.model.RecoverableFile
import com.coderx.datarescuepro.data.model.RecoveryCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class FileRecoveryEngine {
    companion object {
        private const val TAG = "FileRecoveryEngine"

        init {
            System.loadLibrary("datarescuepro")
        }
    }

    private external fun nativeGetVersion(): String
    private external fun nativeDeepScan(path: String, isRooted: Boolean): IntArray
    private external fun nativeDetectRoot(): Boolean
    private external fun nativeIdentifyFileType(signature: ByteArray): Int
    private external fun nativeRecoverDeletedFile(path: String): ByteArray?
    private external fun nativeScanFreeClusters(devicePath: String): Array<String>

    fun getVersion(): String = nativeGetVersion()

    fun detectRootAccess(): Boolean = nativeDetectRoot()

    suspend fun performFullScan(context: Context, isRooted: Boolean = false): List<RecoverableFile> = withContext(Dispatchers.IO) {
        val allFiles = mutableListOf<RecoverableFile>()

        try {
            // Scan media files using MediaStore
            allFiles.addAll(scanMediaFiles(context))

            // Scan accessible storage areas
            allFiles.addAll(scanAccessibleFiles(context))

            // Scan recently deleted files from trash/recycle bin
            allFiles.addAll(scanRecentlyDeletedFiles(context))

            // Scan cache and temporary files
            allFiles.addAll(scanTemporaryFiles(context))

            // Enhanced native scan for both rooted and unrooted devices
            allFiles.addAll(performNativeScan(context, isRooted))

            // Scan for recoverable data in free space clusters
            if (isRooted) {
                allFiles.addAll(scanFreeClusters(context))
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error during full scan", e)
        }

        allFiles.distinctBy { it.path }.sortedByDescending { it.recoveryConfidence }
    }

    private suspend fun scanMediaFiles(context: Context): List<RecoverableFile> = withContext(Dispatchers.IO) {
        val files = mutableListOf<RecoverableFile>()
        
        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.SIZE,
            MediaStore.Files.FileColumns.DATE_MODIFIED,
            MediaStore.Files.FileColumns.MIME_TYPE
        )

        try {
            val cursor: Cursor? = context.contentResolver.query(
                MediaStore.Files.getContentUri("external"),
                projection,
                null,
                null,
                "${MediaStore.Files.FileColumns.DATE_MODIFIED} DESC"
            )

            cursor?.use { c ->
                val nameColumn = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
                val pathColumn = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
                val sizeColumn = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
                val modifiedColumn = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_MODIFIED)
                val mimeColumn = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)

                while (c.moveToNext()) {
                    val name = c.getString(nameColumn) ?: continue
                    val path = c.getString(pathColumn) ?: continue
                    val size = c.getLong(sizeColumn)
                    val modified = c.getLong(modifiedColumn) * 1000
                    val mimeType = c.getString(mimeColumn) ?: ""

                    val file = File(path)
                    if (!file.exists()) {
                        // File is deleted but still in MediaStore - potential recovery candidate
                        files.add(
                            RecoverableFile(
                                name = name,
                                path = path,
                                size = size,
                                type = getFileTypeFromMime(mimeType),
                                lastModified = modified,
                                isRecoverable = true,
                                recoveryLocation = path,
                                recoveryConfidence = 0.8f,
                                recoveryCategory = RecoveryCategory.MEDIA_STORE
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error scanning media files", e)
        }

        files
    }

    private suspend fun scanAccessibleFiles(context: Context): List<RecoverableFile> = withContext(Dispatchers.IO) {
        val files = mutableListOf<RecoverableFile>()
        val commonPaths = listOf(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
            context.getExternalFilesDir(null),
            context.cacheDir
        )

        commonPaths.filterNotNull().forEach { dir ->
            scanDirectoryRecursively(dir)?.let { files.addAll(it) }
        }

        files
    }

    private fun scanDirectoryRecursively(directory: File): List<RecoverableFile>? {
        if (!directory.exists() || !directory.canRead()) return null

        val files = mutableListOf<RecoverableFile>()
        
        try {
            directory.listFiles()?.forEach { file ->
                if (file.isFile) {
                    files.add(
                        RecoverableFile(
                            name = file.name,
                            path = file.absolutePath,
                            size = file.length(),
                            type = getFileTypeFromExtension(file.extension),
                            lastModified = file.lastModified(),
                            isRecoverable = true,
                            recoveryLocation = file.absolutePath,
                            recoveryConfidence = 0.9f,
                            recoveryCategory = RecoveryCategory.RECENTLY_DELETED
                        )
                    )
                } else if (file.isDirectory) {
                    scanDirectoryRecursively(file)?.let { files.addAll(it) }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error scanning directory: ${directory.path}", e)
        }

        return files
    }

    private suspend fun scanRecentlyDeletedFiles(context: Context): List<RecoverableFile> = withContext(Dispatchers.IO) {
        val files = mutableListOf<RecoverableFile>()
        
        // Check Android 11+ trash
        val trashPaths = listOf(
            File(Environment.getExternalStorageDirectory(), ".Trash"),
            File(Environment.getExternalStorageDirectory(), ".trash"),
            File(context.filesDir, ".Trash"),
            File("/data/data/.trash") // Requires root
        )

        trashPaths.forEach { trashDir ->
            if (trashDir.exists() && trashDir.canRead()) {
                scanDirectoryRecursively(trashDir)?.let { files.addAll(it) }
            }
        }

        files
    }

    private suspend fun scanTemporaryFiles(context: Context): List<RecoverableFile> = withContext(Dispatchers.IO) {
        val files = mutableListOf<RecoverableFile>()
        
        val tempDirs = listOf(
            context.cacheDir,
            context.externalCacheDir,
            File("/data/tmp"),
            File("/tmp")
        )

        tempDirs.filterNotNull().forEach { tempDir ->
            if (tempDir.exists() && tempDir.canRead()) {
                scanDirectoryRecursively(tempDir)?.let { 
                    files.addAll(it.map { file ->
                        file.copy(recoveryCategory = RecoveryCategory.CACHE_FILES)
                    })
                }
            }
        }

        files
    }

    private suspend fun performNativeScan(context: Context, isRooted: Boolean): List<RecoverableFile> = withContext(Dispatchers.IO) {
        val files = mutableListOf<RecoverableFile>()
        
        try {
            val scanPaths = if (isRooted) {
                arrayOf("/data", "/system", "/sdcard", "/storage")
            } else {
                arrayOf("/sdcard", context.filesDir.absolutePath, context.cacheDir.absolutePath)
            }

            scanPaths.forEach { path ->
                val results = nativeDeepScan(path, isRooted)
                results.forEach { fileId ->
                    // Convert native scan results to RecoverableFile objects
                    files.add(
                        RecoverableFile(
                            name = "recovered_file_$fileId",
                            path = "$path/recovered_$fileId",
                            size = 0L, // Will be determined during recovery
                            type = FileType.UNKNOWN,
                            lastModified = System.currentTimeMillis(),
                            isRecoverable = true,
                            recoveryLocation = "$path/recovered_$fileId",
                            recoveryConfidence = if (isRooted) 0.7f else 0.5f,
                            recoveryCategory = if (isRooted) RecoveryCategory.ROOT_SCAN else RecoveryCategory.DEEP_SCAN
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in native scan", e)
        }

        files
    }

    private suspend fun scanFreeClusters(context: Context): List<RecoverableFile> = withContext(Dispatchers.IO) {
        val files = mutableListOf<RecoverableFile>()
        
        try {
            val devicePaths = arrayOf("/dev/block/mmcblk0", "/dev/block/sda1")
            
            devicePaths.forEach { devicePath ->
                val clusters = nativeScanFreeClusters(devicePath)
                clusters.forEach { cluster ->
                    files.add(
                        RecoverableFile(
                            name = "cluster_recovery_${cluster.hashCode()}",
                            path = cluster,
                            size = 0L,
                            type = FileType.UNKNOWN,
                            lastModified = System.currentTimeMillis(),
                            isRecoverable = true,
                            recoveryLocation = cluster,
                            recoveryConfidence = 0.6f,
                            recoveryCategory = RecoveryCategory.DEEP_SCAN
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error scanning free clusters", e)
        }

        files
    }

    suspend fun recoverFile(file: RecoverableFile, outputDir: File): Boolean = withContext(Dispatchers.IO) {
        try {
            if (!outputDir.exists()) {
                outputDir.mkdirs()
            }

            val outputFile = File(outputDir, file.name)
            
            when (file.recoveryCategory) {
                RecoveryCategory.MEDIA_STORE, RecoveryCategory.RECENTLY_DELETED -> {
                    // Standard file copy
                    val sourceFile = File(file.path)
                    if (sourceFile.exists()) {
                        sourceFile.copyTo(outputFile, overwrite = true)
                        return@withContext true
                    }
                }
                RecoveryCategory.DEEP_SCAN, RecoveryCategory.ROOT_SCAN -> {
                    // Use native recovery
                    val recoveredData = nativeRecoverDeletedFile(file.path)
                    if (recoveredData != null && recoveredData.isNotEmpty()) {
                        FileOutputStream(outputFile).use { fos ->
                            fos.write(recoveredData)
                        }
                        return@withContext true
                    }
                }
                RecoveryCategory.CACHE_FILES -> {
                    // Cache file recovery
                    val sourceFile = File(file.path)
                    if (sourceFile.exists()) {
                        sourceFile.copyTo(outputFile, overwrite = true)
                        return@withContext true
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error recovering file: ${file.name}", e)
        }
        
        false
    }

    fun generatePreview(file: RecoverableFile): Bitmap? {
        return try {
            when (file.type) {
                FileType.JPEG, FileType.PNG, FileType.GIF -> {
                    val sourceFile = File(file.path)
                    if (sourceFile.exists()) {
                        BitmapFactory.decodeFile(file.path)
                    } else {
                        // Try to recover preview from native scan
                        val recoveredData = nativeRecoverDeletedFile(file.path)
                        if (recoveredData != null) {
                            BitmapFactory.decodeByteArray(recoveredData, 0, recoveredData.size)
                        } else null
                    }
                }
                FileType.MP4, FileType.MP3 -> {
                    val retriever = MediaMetadataRetriever()
                    try {
                        retriever.setDataSource(file.path)
                        retriever.getFrameAtTime(0)
                    } catch (e: Exception) {
                        null
                    } finally {
                        retriever.release()
                    }
                }
                else -> null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error generating preview for ${file.name}", e)
            null
        }
    }

    private fun getFileTypeFromMime(mimeType: String): FileType {
        return when {
            mimeType.startsWith("image/jpeg") -> FileType.JPEG
            mimeType.startsWith("image/png") -> FileType.PNG
            mimeType.startsWith("image/gif") -> FileType.GIF
            mimeType.startsWith("video/mp4") -> FileType.MP4
            mimeType.startsWith("audio/mpeg") -> FileType.MP3
            mimeType.startsWith("application/pdf") -> FileType.PDF
            mimeType.startsWith("application/zip") -> FileType.ZIP
            mimeType.startsWith("image/") -> FileType.IMAGE
            mimeType.startsWith("video/") -> FileType.VIDEO
            mimeType.startsWith("audio/") -> FileType.AUDIO
            mimeType.contains("document") -> FileType.DOCUMENT
            else -> FileType.UNKNOWN
        }
    }

    private fun getFileTypeFromExtension(extension: String): FileType {
        return when (extension.lowercase()) {
            "jpg", "jpeg" -> FileType.JPEG
            "png" -> FileType.PNG
            "gif" -> FileType.GIF
            "mp4", "mov", "avi" -> FileType.MP4
            "mp3", "wav", "flac" -> FileType.MP3
            "pdf" -> FileType.PDF
            "zip", "rar" -> FileType.ZIP
            "doc", "docx" -> FileType.DOC
            "xls", "xlsx" -> FileType.XLS
            else -> FileType.UNKNOWN
        }
    }
}
