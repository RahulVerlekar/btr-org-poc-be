package xyz.betterorg.backend_poc.app.errors

class Error {
}

public class UnsupportedMediaTypeException(override val message: String?): RuntimeException(message) {}

public class FileUploadException(override val message: String?, override val cause: Throwable?): RuntimeException(message, cause) {}
