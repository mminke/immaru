package com.earthrevealed.immaru.configuration.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.earthrevealed.immaru.configuration.Configuration
import com.earthrevealed.immaru.configuration.ConfigurationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class DataStoreConfigurationRepository(
    private val dataStore: DataStore<Preferences>
): ConfigurationRepository {
    override suspend fun save(configuration: Configuration) {
        dataStore.edit { preferences ->
            preferences[CONFIGURATION_PREFERENCE_KEY] = Json.encodeToString(configuration)
        }
    }

    override val configuration: Flow<Configuration>
        get() = dataStore.data.map { preferences ->
            preferences[CONFIGURATION_PREFERENCE_KEY]?.let {
                Json.decodeFromString<Configuration>(it)
            } ?: Configuration()
        }

    private val CONFIGURATION_PREFERENCE_KEY = stringPreferencesKey("immaru.configuration")
}
