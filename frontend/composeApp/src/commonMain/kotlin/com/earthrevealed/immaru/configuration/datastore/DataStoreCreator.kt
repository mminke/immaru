@file:JvmName("CommonDataStoreCreator")
package com.earthrevealed.immaru.configuration.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toPath
import kotlin.jvm.JvmName

fun createDataStore(producePath: () -> String): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath(
        produceFile = { producePath().toPath() }
    )

const val DATASTORE_FILENAME = "immaru.preferences_pb" // Protobuf file
