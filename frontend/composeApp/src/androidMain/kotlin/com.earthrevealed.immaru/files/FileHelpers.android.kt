package com.earthrevealed.immaru.files

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.platform.LocalContext
import dev.zwander.kotlin.file.IPlatformFile
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.toKotlinInstant
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.attribute.BasicFileAttributes

//actual val IPlatformFile.creationDate: Instant
//    get() {
//        val path = Paths.get(this.getAbsolutePath())
//        val attrs = Files.readAttributes(path, BasicFileAttributes::class.java)
//        return attrs.creationTime().toInstant().toKotlinInstant()
//    }

//actual val IPlatformFile.creationDate: Instant
//    get() {
//
//        this.getLastModified()
//
//        val uriString = this.getAbsolutePath()
//        val uri = Uri.parse(uriString)
//
//        // You'll need access to a Context to get the ContentResolver
//        // This might need to be passed into your IPlatformFile implementation
//        // or accessed globally if available in your multiplatform setup.
//        val context: Context = LocalContext.current
//
//            if (uri.scheme == "content") {
//                try {
//                    // Try to get DATE_ADDED or DATE_MODIFIED from MediaStore
//                    // DATE_TAKEN might also be relevant for images/videos
//                    val projection = arrayOf(
//                        MediaStore.MediaColumns.DATE_ADDED,
//                        MediaStore.MediaColumns.DATE_MODIFIED
//                        // MediaStore.Images.Media.DATE_TAKEN // For images
//                        // MediaStore.Video.Media.DATE_TAKEN // For videos
//                    )
//
//                    context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
//                        if (cursor.moveToFirst()) {
//                            val dateAddedIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DATE_ADDED)
//                            val dateModifiedIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DATE_MODIFIED)
//
//                            // Prefer DATE_ADDED if available, otherwise fallback to DATE_MODIFIED
//                            // These are stored in seconds since epoch.
//                            val timestampSeconds = if (dateAddedIndex != -1) {
//                                cursor.getLong(dateAddedIndex)
//                            } else if (dateModifiedIndex != -1) {
//                                cursor.getLong(dateModifiedIndex)
//                            } else {
//                                // No date information found via MediaStore
//                                // You might want to throw an exception or return a default Instant
//                                System.currentTimeMillis() / 1000 // Fallback to current time (as seconds)
//                            }
//                            return Instant.fromEpochSeconds(timestampSeconds)
//                        }
//                    }
//                } catch (e: Exception) {
//                    // Handle exceptions, e.g., SecurityException if permission is missing
//                    // or if the URI is invalid.
//                    // Log the error and potentially fall back or re-throw
//                    e.printStackTrace()
//                }
//                // Fallback if MediaStore query fails or doesn't return date
//                // This is a very rough fallback and might not be accurate.
//                return java.time.Instant.now().toKotlinInstant()
//
//            } else if (uri.scheme == "file") {
//                // Your original code for file URIs (if still needed)
//                // Ensure this part is also robust and handles NoSuchFileException
//                try {
//                    val path = Paths.get(uri.path!!)
//                    val attrs = Files.readAttributes(path, java.nio.file.attribute.BasicFileAttributes::class.java)
//                    return attrs.creationTime().toInstant().toKotlinInstant()
//                } catch (e: java.nio.file.NoSuchFileException) {
//                    // Handle case where file URI is invalid
//                    e.printStackTrace()
//                    // Fallback or re-throw
//                    return Clock.System.now()
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                    return Clock.System.now()
//                }
//            }
//
//        // If URI scheme is neither "content" nor "file", or if all else fails
//        // Consider what the appropriate default or error handling should be.
//        // Returning current time might not be ideal.
//        // You might want to throw an IllegalArgumentException or return a specific error Instant.
//        System.err.println("Unsupported URI scheme or failed to get creation date: $uriString")
//        return java.time.Instant.now().toKotlinInstant() // Placeholder
//    }