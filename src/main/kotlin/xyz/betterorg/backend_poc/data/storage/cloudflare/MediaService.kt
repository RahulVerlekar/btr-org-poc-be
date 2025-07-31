package xyz.betterorg.backend_poc.data.storage.cloudflare

import com.google.api.services.gmail.model.Message
import io.jsonwebtoken.io.IOException
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import xyz.betterorg.backend_poc.app.errors.FileUploadException
import xyz.betterorg.backend_poc.app.errors.UnsupportedMediaTypeException
import java.util.UUID

enum class DocumentType {
    IMAGE,
    DOCUMENT,
    TEXT,
    OTHER
}

@Service
class MediaService(
    @Value("\${cloudflare.r2.bucket}") private val bucket: String,
    private val client: S3Client
) {



    public fun upload(message: Message): Map<String, String> {
        val parts = message.payload.parts ?: return emptyMap()
        val attachments = mutableMapOf<String, String>()

        for (part in parts) {
            if (part.filename.isNullOrEmpty() || part.body == null) continue

            val filename = part.filename
            val contentType = part.mimeType ?: "application/octet-stream"
            val ext = getFileExtension(filename)
            val folder = when (ext?.toLowerCase()) {
                "jpg", "jpeg", "png", "gif" -> "images"
                "mp4", "avi", "mov" -> "videos"
                "mp3", "wav" -> "audio"
                "pdf", "docx", "txt" -> "documents"
                else -> {
                    println("Unsupported file type: $ext for file: $filename")
//                    throw UnsupportedMediaTypeException("Unsupported file type: $ext")
                    continue
                }
            }

            val key = java.lang.String.format("%s/%s-%s", folder, UUID.randomUUID(), filename)

            // Create the PutObjectRequest with metadata
            val metadata = mutableMapOf<String, String>()
            message.payload.headers.forEach { header ->
                metadata[header.name] = header.value
            }
            metadata["threadId"] = message.threadId
            metadata["id"] = message.id


            val request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .metadata(metadata)
                .build()
            try {
                client.putObject(request, RequestBody.fromBytes(part.body.data.toByteArray()))
                attachments[filename] = key
            } catch (e: Exception) {
                println("Failed to upload attachment: $filename, error: ${e.message}")
//                throw FileUploadException(
//                    "Failed to upload attachment: $filename",
//                    e
//                )
            }
        }

        return attachments
    }


    public fun upload(data: UploadablePart): String {
        val original: String? = data.filename
        val contentType = data.contentType

        val ext = getFileExtension(original)
        val folder = when (ext?.lowercase()) {
            "jpg", "jpeg", "png", "gif" -> "${data.messageId}/images"
            "mp4", "avi", "mov" -> "${data.messageId}/videos"
            "mp3", "wav" -> "audio"
            "pdf", "docx", "txt" -> "${data.messageId}/documents"
            else -> contentType?.replace("/", "-")?.lowercase() ?: "others"
        }

        val key = "${data.messageId}/${data.contentType?.replace("/","-")?:"other"}/${data.attachmentId}${if (ext.isNullOrEmpty()) "" else ".$ext"}"

        val request = PutObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .contentType(contentType)
            .metadata(data.metadata.plus("threadId" to data.messageId).plus("id" to data.attachmentId))
            .build()

        try {
            client.putObject(request, RequestBody.fromBytes(data.data))
        } catch (e: IOException) {
            throw FileUploadException(
                "Failed to upload file: $original",
                e
            )
        }

        return key
    }

    public fun uploadFile(file: MultipartFile): String {
        val original: String = file.originalFilename
            ?: throw UnsupportedMediaTypeException("Filename is missing")
        val contentType = file.contentType ?: throw UnsupportedMediaTypeException("Content type is missing")

        val ext = getFileExtension(original)
        val folder = when (ext) {
            "jpg", "jpeg", "png", "gif" -> "images"
            "mp4", "avi", "mov" -> "videos"
            "mp3", "wav" -> "audio"
            "pdf", "docx", "txt" -> "documents"
            else -> throw UnsupportedMediaTypeException("Unsupported file type: $ext")
        }

        val key = java.lang.String.format("%s/%s-%s", folder, UUID.randomUUID(), original)

        val request = PutObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .contentType(contentType)
            .build()

        try {
            client.putObject(request, RequestBody.fromBytes(file.bytes))
        } catch (e: IOException) {
            throw FileUploadException(
                "Failed to upload file: $original",
                e
            )
        }

        return key
    }

    private fun getFileExtension(filename: String?): String? {
        if (filename.isNullOrEmpty()) {
            return null
        }
        val idx = filename.lastIndexOf('.')
        if (!(idx < 0 || idx == filename.length - 1)) {
            return null
        }
        return filename.substring(idx + 1)
    }
}