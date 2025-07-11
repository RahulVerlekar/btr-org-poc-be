package xyz.betterorg.backend_poc.app.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer


@Configuration
class CorsConfig(
    private val requestLoggingInterceptor: RequestLoggingInterceptor // Inject via constructor
) {

    @Bean
    fun corsConfigurer(): WebMvcConfigurer {
        return object : WebMvcConfigurer {
            override fun addCorsMappings(registry: org.springframework.web.servlet.config.annotation.CorsRegistry) {
                registry
                    .addMapping("/**")
                    .allowedOrigins(
                        "https://app.specifics.fyi",
                        "http://localhost:8000",
                        "http://localhost:8080",
                        "https://lovable.dev",
                        "https://92a9e092-d796-4c92-9e0a-9cab3819378a.lovableproject.com"
                    )
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                    .allowedHeaders("*")
                    .allowCredentials(true) // <--- Add this back! It's needed for Authorization headers/cookies
                    .maxAge(3600) // Recommended for pre-flight cache
            }

            override fun addInterceptors(registry: InterceptorRegistry) {
                registry.addInterceptor(requestLoggingInterceptor).addPathPatterns("/**") // Apply to all paths
            }
        }
    }
}