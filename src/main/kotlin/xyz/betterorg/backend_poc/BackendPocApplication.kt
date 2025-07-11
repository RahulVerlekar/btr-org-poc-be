package xyz.betterorg.backend_poc

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableAsync

@SpringBootApplication
@EnableAsync
class BackendPocApplication

fun main(args: Array<String>) {
	runApplication<BackendPocApplication>(*args)
}
