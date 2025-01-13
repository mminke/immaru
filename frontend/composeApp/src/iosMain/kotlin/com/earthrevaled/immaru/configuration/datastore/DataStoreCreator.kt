package com.earthrevaled.immaru.configuration.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.earthrevealed.immaru.configuration.datastore.DATASTORE_FILENAME
import com.earthrevealed.immaru.configuration.datastore.createDataStore
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

@OptIn(ExperimentalForeignApi::class)
fun createIosDataStore(): DataStore<Preferences> = createDataStore(
    producePath = {
        val documentDirectory: NSURL? = NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null,
        )
        requireNotNull(documentDirectory).path + "/$DATASTORE_FILENAME"
    }
)