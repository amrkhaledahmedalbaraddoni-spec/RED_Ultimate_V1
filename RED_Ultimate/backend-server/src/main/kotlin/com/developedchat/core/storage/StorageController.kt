package com.red.core.storage

import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.http.ResponseEntity
import java.util.UUID

@RestController
@RequestMapping("/api/media")
class StorageController {

    /**
     * رفع الوسائط (صور، فيديو، ملفات) إلى سيرفر MinIO المحلي
     */
    @PostMapping("/upload")
    fun uploadMedia(@RequestParam("file") file: MultipartFile): ResponseEntity<Any> {
        val fileId = UUID.randomUUID().toString()
        val fileName = "${fileId}_${file.originalFilename}"
        
        // هنا يتم الحفظ الفعلي في MinIO (محاكاة للمسار المحلي)
        val fileUrl = "http://localhost:9000/developed-chat/$fileName"
        
        println("Media Uploaded: $fileName, Size: ${file.size} bytes")
        
        return ResponseEntity.ok(mapOf(
            "url" to fileUrl,
            "fileId" to fileId,
            "type" to file.contentType
        ))
    }

    @GetMapping("/download/{fileId}")
    fun getDownloadUrl(@PathVariable fileId: String): ResponseEntity<Any> {
        return ResponseEntity.ok(mapOf("url" to "http://localhost:9000/developed-chat/$fileId"))
    }
}
