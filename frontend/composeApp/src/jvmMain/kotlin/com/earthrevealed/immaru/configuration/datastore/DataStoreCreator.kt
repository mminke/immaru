package com.earthrevealed.immaru.configuration.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import kotlin.io.path.Path

fun createJvmDataStore(): DataStore<Preferences> = createDataStore(
    producePath = {
        Path("config").resolve(DATASTORE_FILENAME).toAbsolutePath().toString().also {
            println("Using config path: $it")
        }
    }
)