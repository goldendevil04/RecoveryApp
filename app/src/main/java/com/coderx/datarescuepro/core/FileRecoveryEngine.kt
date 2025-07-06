package com.coderx.datarescuepro.core

import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
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

    fun getVersion(): String = nativeGetVersion()

    fun detectRootAccess(): Boolean = nativeDetectRoot()

    suspend fun performFullScan(
        context: Context, 
        isRooted: Boolean = false,
        onProgress: (Float, String) -> Unit = { _, _ -> }
    ): List<RecoverableFile> = withContext(Dispatchers.IO) {
        val allFiles = mutableListOf<RecoverableFile>()

        try {
            onProgress(0.1f, "Scanning media files...")
            allFiles.addAll(scanMediaFiles(context))

            onProgress(0.3f, "Scanning accessible storage...")
            allFiles.addAll(scanAccessibleFiles(context))

            onProgress(0.5f, "Searching recently deleted files...")
            allFiles.addAll(scanRecentlyDeletedFiles(context))

            onProgress(0.7f, "Scanning cache and temporary files...")
            allFiles.addAll(scanTemporaryFiles(context))

            onProgress(0.8f, "Performing deep scan...")
            allFiles.addAll(performDeepScan(context))

            if (isRooted) {
                onProgress(0.9f, "Performing root scan...")
                allFiles.addAll(performRootScan(context))
            }

            onProgress(1.0f, "Scan completed")

        } catch (e: Exception) {
            Log.e(TAG, "Error during full scan", e)
        }

        allFiles.distinctBy { it.path }
    }

    private suspend fun scanMediaFiles(context: Context): List<RecoverableFile> = withContext(Dispatchers.IO) {
        val files = mutableListOf<RecoverableFile>()

        val mediaTypes = listOf(
            Triple(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "Images", FileType.IMAGE),
            Triple(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, "Videos", FileType.VIDEO),
            Triple(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, "Audio", FileType.AUDIO)
        )

        mediaTypes.forEach { (uri, type, fileType) ->
            try {
                val projection = arrayOf(
                    MediaStore.MediaColumns.DISPLAY_NAME,
                    MediaStore.MediaColumns.DATA,
                    MediaStore.MediaColumns.SIZE,
                    MediaStore.MediaColumns.DATE_MODIFIED
                )

                context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                    val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
                    val pathColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
                    val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE)
                    val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED)

                    while (cursor.moveToNext()) {
                        val name = cursor.getString(nameColumn)
                        val path = cursor.getString(pathColumn)
                        val size = cursor.getLong(sizeColumn)
                        val dateModified = cursor.getLong(dateColumn)

                        if (path != null && File(path).exists()) {
                            files.add(
                                RecoverableFile(
                                    name = name ?: File(path).name,
                                    path = path,
                                    size = size,
                                    type = fileType,
                                    lastModified = dateModified * 1000,
                                    recoveryLocation = "Media Store",
                                    recoveryConfidence = 0.95f,
                                    recoveryCategory = RecoveryCategory.MEDIA_STORE,
                                    thumbnailPath = generateThumbnail(path, fileType)
                                )
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error scanning media store: $type", e)
            }
        }

        return@withContext files
    }

    private suspend fun scanAccessibleFiles(context: Context): List<RecoverableFile> = withContext(Dispatchers.IO) {
        val files = mutableListOf<RecoverableFile>()

        try {
            // Scan external storage
            val externalDir = Environment.getExternalStorageDirectory()
            if (externalDir.exists() && externalDir.canRead()) {
                scanDirectoryRecursively(externalDir, files, maxDepth = 3)
            }

            // Scan Downloads folder
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (downloadsDir.exists() && downloadsDir.canRead()) {
                scanDirectoryRecursively(downloadsDir, files)
            }

            // Scan DCIM folder
            val dcimDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
            if (dcimDir.exists() && dcimDir.canRead()) {
                scanDirectoryRecursively(dcimDir, files)
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error scanning accessible files", e)
        }

        files
    }

    private suspend fun scanRecentlyDeletedFiles(context: Context): List<RecoverableFile> = withContext(Dispatchers.IO) {
        val recoveredFiles = mutableListOf<RecoverableFile>()

        try {
            // Check for recently deleted files in various locations
            val trashDirs = listOf(
                File(Environment.getExternalStorageDirectory(), ".trash"),
                File(Environment.getExternalStorageDirectory(), ".recycle"),
                File(Environment.getExternalStorageDirectory(), "Android/data/.deleted"),
                File(context.cacheDir, ".deleted"),
                File(Environment.getExternalStorageDirectory(), ".thumbnails/.deleted")
            )

            trashDirs.forEach { trashDir ->
                if (trashDir.exists() && trashDir.canRead()) {
                    trashDir.listFiles()?.forEach { file ->
                        if (file.isFile && file.length() > 0) {
                            recoveredFiles.add(createRecoverableFile(file, "Recently Deleted", RecoveryCategory.RECENTLY_DELETED))
                        }
                    }
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error scanning recently deleted files", e)
        }

        recoveredFiles
    }

    private suspend fun scanTemporaryFiles(context: Context): List<RecoverableFile> = withContext(Dispatchers.IO) {
        val recoveredFiles = mutableListOf<RecoverableFile>()

        try {
            // Scan cache directories
            val cacheDirs = listOf(
                context.cacheDir,
                context.externalCacheDir,
                File(Environment.getExternalStorageDirectory(), "Android/data/${context.packageName}/cache"),
                File(Environment.getExternalStorageDirectory(), ".thumbnails")
            )

            cacheDirs.forEach { cacheDir ->
                cacheDir?.let { dir ->
                    if (dir.exists() && dir.canRead()) {
                        dir.listFiles()?.forEach { file ->
                            if (file.isFile && file.length() > 1024) { // Only files > 1KB
                                recoveredFiles.add(createRecoverableFile(file, "Cache Files", RecoveryCategory.CACHE_FILES))
                            }
                        }
                    }
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error scanning temporary files", e)
        }

        recoveredFiles
    }

    private suspend fun performDeepScan(context: Context): List<RecoverableFile> = withContext(Dispatchers.IO) {
        val recoveredFiles = mutableListOf<RecoverableFile>()

        try {
            // Use native deep scan for better performance
            val scanResults = nativeDeepScan(Environment.getExternalStorageDirectory().absolutePath, false)
            
            // Simulate finding files based on scan results
            scanResults.forEach { result ->
                // This would be replaced with actual file recovery logic
                val simulatedFile = File(Environment.getExternalStorageDirectory(), "recovered_file_$result.tmp")
                if (simulatedFile.exists()) {
                    recoveredFiles.add(createRecoverableFile(simulatedFile, "Deep Scan", RecoveryCategory.DEEP_SCAN))
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error during deep scan", e)
        }

        recoveredFiles
    }

    private suspend fun performRootScan(context: Context): List<RecoverableFile> = withContext(Dispatchers.IO) {
        val recoveredFiles = mutableListOf<RecoverableFile>()

        try {
            // Perform deep scan on system directories (requires root)
            val systemDirs = listOf("/data/lost+found", "/cache", "/tmp")
            
            systemDirs.forEach { dirPath ->
                val dir = File(dirPath)
                if (dir.exists() && dir.canRead()) {
                    dir.listFiles()?.forEach { file ->
                        if (file.isFile) {
                            recoveredFiles.add(createRecoverableFile(file, "Root Scan", RecoveryCategory.ROOT_SCAN))
                        }
                    }
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error during root scan", e)
        }

        recoveredFiles
    }

    private fun scanDirectoryRecursively(
        directory: File,
        files: MutableList<RecoverableFile>,
        maxDepth: Int = 3,
        currentDepth: Int = 0
    ) {
        if (currentDepth >= maxDepth || !directory.canRead()) return

        try {
            directory.listFiles()?.forEach { file ->
                when {
                    file.isFile && file.length() > 0 -> {
                        files.add(createRecoverableFile(file, "Storage Scan", RecoveryCategory.DEEP_SCAN))
                    }
                    file.isDirectory && currentDepth < maxDepth - 1 -> {
                        scanDirectoryRecursively(file, files, maxDepth, currentDepth + 1)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error scanning directory: ${directory.path}", e)
        }
    }

    private fun createRecoverableFile(file: File, location: String, category: RecoveryCategory): RecoverableFile {
        val fileType = determineFileType(file)
        val confidence = when (category) {
            RecoveryCategory.MEDIA_STORE -> 0.95f
            RecoveryCategory.RECENTLY_DELETED -> 0.80f
            RecoveryCategory.CACHE_FILES -> 0.60f
            RecoveryCategory.DEEP_SCAN -> 0.70f
            RecoveryCategory.ROOT_SCAN -> 0.85f
        }

        return RecoverableFile(
            name = file.name,
            path = file.absolutePath,
            size = file.length(),
            type = fileType,
            lastModified = file.lastModified(),
            recoveryLocation = location,
            recoveryConfidence = confidence,
            recoveryCategory = category,
            thumbnailPath = generateThumbnail(file.absolutePath, fileType)
        )
    }

    private fun determineFileType(file: File): FileType {
        val extension = file.extension.lowercase()
        return when (extension) {
            "jpg", "jpeg" -> FileType.JPEG
            "png" -> FileType.PNG
            "gif" -> FileType.GIF
            "mp4", "avi", "mkv", "mov" -> FileType.MP4
            "mp3", "wav", "flac", "aac" -> FileType.MP3
            "pdf" -> FileType.PDF
            "zip", "rar", "7z" -> FileType.ZIP
            "doc" -> FileType.DOC
            "docx" -> FileType.DOCX
            "xls" -> FileType.XLS
            "xlsx" -> FileType.XLSX
            else -> {
                // Use native file type detection for unknown extensions
                try {
                    val signature = ByteArray(16)
                    FileInputStream(file).use { it.read(signature) }
                    val nativeType = nativeIdentifyFileType(signature)
                    FileType.values().getOrNull(nativeType) ?: FileType.UNKNOWN
                } catch (e: Exception) {
                    FileType.UNKNOWN
                }
            }
        }
    }

    private fun generateThumbnail(filePath: String, fileType: FileType): String? {
        return try {
            when (fileType) {
                FileType.IMAGE, FileType.JPEG, FileType.PNG, FileType.GIF -> {
                    generateImageThumbnail(filePath)
                }
                FileType.VIDEO, FileType.MP4 -> {
                    generateVideoThumbnail(filePath)
                }
                else -> null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error generating thumbnail for $filePath", e)
            null
        }
    }

    private fun generateImageThumbnail(imagePath: String): String? {
        return try {
            val options = BitmapFactory.Options().apply {
                inSampleSize = 4 // Reduce size
                inJustDecodeBounds = false
            }
            
            val bitmap = BitmapFactory.decodeFile(imagePath, options)
            if (bitmap != null) {
                val thumbnailDir = File(Environment.getExternalStorageDirectory(), "DataRescue/thumbnails")
                if (!thumbnailDir.exists()) thumbnailDir.mkdirs()
                
                val thumbnailFile = File(thumbnailDir, "thumb_${File(imagePath).nameWithoutExtension}.jpg")
                FileOutputStream(thumbnailFile).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
                }
                bitmap.recycle()
                thumbnailFile.absolutePath
            } else null
        } catch (e: Exception) {
            Log.e(TAG, "Error creating image thumbnail", e)
            null
        }
    }

    private fun generateVideoThumbnail(videoPath: String): String? {
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(videoPath)
            val bitmap = retriever.getFrameAtTime(1000000) // 1 second
            retriever.release()
            
            if (bitmap != null) {
                val thumbnailDir = File(Environment.getExternalStorageDirectory(), "DataRescue/thumbnails")
                if (!thumbnailDir.exists()) thumbnailDir.mkdirs()
                
                val thumbnailFile = File(thumbnailDir, "thumb_${File(videoPath).nameWithoutExtension}.jpg")
                FileOutputStream(thumbnailFile).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
                }
                bitmap.recycle()
                thumbnailFile.absolutePath
            } else null
        } catch (e: Exception) {
            Log.e(TAG, "Error creating video thumbnail", e)
            null
        }
    }

    suspend fun recoverFile(file: RecoverableFile, recoveryDir: File): Boolean = withContext(Dispatchers.IO) {
        try {
            val sourceFile = File(file.path)
            if (!sourceFile.exists()) return@withContext false

            val destinationFile = File(recoveryDir, file.name)
            var counter = 1
            var finalDestination = destinationFile

            // Handle file name conflicts
            while (finalDestination.exists()) {
                val nameWithoutExt = file.name.substringBeforeLast(".")
                val extension = file.name.substringAfterLast(".", "")
                val newName = if (extension.isNotEmpty()) {
                    "${nameWithoutExt}_$counter.$extension"
                } else {
                    "${nameWithoutExt}_$counter"
                }
                finalDestination = File(recoveryDir, newName)
                counter++
            }

            // Copy file
            FileInputStream(sourceFile).use { input ->
                FileOutputStream(finalDestination).use { output ->
                    input.copyTo(output)
                }
            }

            Log.i(TAG, "File recovered successfully: ${finalDestination.absolutePath}")
            return@withContext true

        } catch (e: Exception) {
            Log.e(TAG, "Error recovering file: ${file.path}", e)
            return@withContext false
        }
    }
}