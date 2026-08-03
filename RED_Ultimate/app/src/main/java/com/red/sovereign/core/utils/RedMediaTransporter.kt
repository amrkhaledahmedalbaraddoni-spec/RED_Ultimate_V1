package com.red.sovereign.core.utils

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

interface MinioUploader {
    suspend fun uploadFileSync(file: File, remotePath: String): String?
    suspend fun uploadBytes(bytes: ByteArray, remotePath: String, mimeType: String): String?
}

// Fallback implementation using local cache + backend API
@Singleton
class MinioUploaderImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : MinioUploader {
    override suspend fun uploadFileSync(file: File, remotePath: String): String? {
        return try {
            // In production, POST to /api/storage/upload
            // Simulate upload URL
            "https://storage.red.local/$remotePath"
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun uploadBytes(bytes: ByteArray, remotePath: String, mimeType: String): String? {
        val tmp = File(context.cacheDir, remotePath.substringAfterLast("/"))
        tmp.writeBytes(bytes)
        return uploadFileSync(tmp, remotePath)
    }
}

@Singleton
class RedMediaTransporter @Inject constructor(
    private val uploader: MinioUploader,
    @ApplicationContext private val context: Context
) {
    suspend fun transportFile(uri: Uri, type: String): String? {
        val file = uriToFile(uri) ?: return null
        val remotePath = "vault/$type/${System.currentTimeMillis()}_${file.name}"
        return uploader.uploadFileSync(file, remotePath)
    }

    private fun uriToFile(uri: Uri): File? {
        return try {
            val fileName = getFileName(uri) ?: "temp_${System.currentTimeMillis()}"
            val input = context.contentResolver.openInputStream(uri) ?: return null
            val tempFile = File(context.cacheDir, fileName)
            FileOutputStream(tempFile).use { output ->
                input.copyTo(output)
            }
            input.close()
            tempFile
        } catch (e: Exception) {
            println("⚠️ RED MediaTransporter uriToFile failed: ${e.message}")
            null
        }
    }

    private fun getFileName(uri: Uri): String? {
        var name: String? = null
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (idx >= 0 && cursor.moveToFirst()) {
                name = cursor.getString(idx)
            }
        }
        return name ?: uri.lastPathSegment
    }

    fun getVoiceStandard() = "Opus-48kHz-64kbps-HiFi"
    fun getVideoStandard() = "AV1-1080p-4K-HDR"
}
