package nl.connectplay.scoreplay.exceptions

/**
 * Base class for all image upload–related exceptions.
 *
 * This class is **sealed** to restrict inheritance to only the known
 * upload exceptions defined in this file. This provides two main benefits:
 * 1. Safety: Only the predefined types (e.g., [NoFileUploadedException],
 *    [TooManyFilesUploadedException]) can exist, reducing unexpected errors.
 * 2. Exhaustiveness: When using `when` expressions to handle exceptions,
 *    the compiler can ensure all possible upload exceptions are covered.
 *
 * Use this as the parent class for all exceptions related to image uploads.
 */
sealed class ImageUploadException(message: String) : Exception(message)

class NoFileUploadedException : ImageUploadException("No file was uploaded")
class TooManyFilesUploadedException : ImageUploadException("Only one image may be uploaded at a time")
class InvalidFileTypeException : ImageUploadException("Unsupported file type")
class FileUploadFailedException(val status: String? = null) : ImageUploadException("Failed to upload image to CDN, got $status")
