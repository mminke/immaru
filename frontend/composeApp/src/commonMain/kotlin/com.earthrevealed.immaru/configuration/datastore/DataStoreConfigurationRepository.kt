package com.earthrevealed.immaru.configuration.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.earthrevealed.immaru.configuration.Configuration
import com.earthrevealed.immaru.configuration.ConfigurationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DataStoreConfigurationRepository(
    private val dataStore: DataStore<Preferences>
): ConfigurationRepository {
    override suspend fun update(configuration: Configuration) {
        dataStore.edit { preferences ->
            preferences[SERVER_URL_PREFERENCE_KEY] = configuration.serverUrl?:""
        }
    }

    override val configuration: Flow<Configuration>
        get() = dataStore.data.map { preferences ->
            Configuration(
                preferences[SERVER_URL_PREFERENCE_KEY]
            )
        }

    private val SERVER_URL_PREFERENCE_KEY = stringPreferencesKey("immaru.server.url")
}
