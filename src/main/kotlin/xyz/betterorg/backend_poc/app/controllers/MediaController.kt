package xyz.betterorg.backend_poc.app.controllers

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import xyz.betterorg.backend_poc.app.service.MediaService

@RestController
@RequestMapping("/api/media")
class MediaController(
    val mediaService: MediaService
) {

    @PostMapping("/upload")
    public fun uploadMedia(
        @RequestParam(value = "file") file: MultipartFile
    ): ResponseEntity<String?> {
        val key = mediaService.uploadFile(file)
        return ResponseEntity.ok(key)
    }
}