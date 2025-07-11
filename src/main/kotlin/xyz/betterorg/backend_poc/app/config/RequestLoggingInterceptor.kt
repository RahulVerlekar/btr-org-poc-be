package xyz.betterorg.backend_poc.app.config // Replace with your actual package

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.util.ContentCachingRequestWrapper
import java.io.BufferedReader
import java.io.IOException
import java.lang.String

@Component
class RequestLoggingInterceptor : HandlerInterceptor {

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(RequestLoggingInterceptor::class.java)
    }

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        // Wrap the request to allow multiple reads of the input stream (for body logging)
        val wrappedRequest = if (request is ContentCachingRequestWrapper) {
            request // Already wrapped if another interceptor did it or it's a retry
        } else {
            ContentCachingRequestWrapper(request)
        }

        // Log Request Headers
        logger.info("--- Incoming Request ---")
        logger.info("Request URL: {}", wrappedRequest.requestURL)
        logger.info("Request Method: {}", wrappedRequest.method)

        // Log Request Body
        try {
            val requestBody = String(wrappedRequest.contentAsByteArray, wrappedRequest.characterEncoding ?: "UTF-8")
            if (requestBody.isNotBlank()) {
                logger.info("Body: {}", requestBody)
            } else {
                logger.info("Body: (No body or empty body)")
            }
        } catch (e: IOException) {
            logger.error("Error reading request body: {}", e.message)
        }
        logger.info("------------------------")

        return true
    }
}