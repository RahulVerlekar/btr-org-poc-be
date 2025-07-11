package xyz.betterorg.backend_poc.app.controllers

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController()
@RequestMapping("/gmail")
class GMailController {

    @PostMapping(path =
        ["/link-email"]
    )
    fun linkEmail(
        @RequestBody request: String
    ): String {
        return request.toString();
        return "Email linked successfully"
    }

    @PostMapping("/start-sync")
    fun startSync(): String {
        return "Sync started successfully"
    }

    @GetMapping("/sync-status")
    fun syncStatus(): String {
        return "Sync stopped successfully"
    }
}