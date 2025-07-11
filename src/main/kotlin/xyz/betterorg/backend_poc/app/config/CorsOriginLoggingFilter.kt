package xyz.betterorg.backend_poc.app.config

import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.cors.CorsUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.cors.CorsConfigurationSource
import java.io.IOException

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class CorsOriginLoggingFilter : OncePerRequestFilter() {
    private val logger = LoggerFactory.getLogger(CorsOriginLoggingFilter::class.java)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val origin = request.getHeader("Origin")
        if (origin != null && CorsUtils.isCorsRequest(request)) {
            // Log every CORS request's origin
            logger.info("CORS request from origin: $origin to ${request.requestURI}")
        }
        filterChain.doFilter(request, response)
    }
}

