import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.preferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okio.Path.Companion.toPath

expect object Configuration {
    val immaruUrl: String
}

fun createDataStore(producePath: () -> String): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath(
        produceFile = { producePath().toPath() }
    )

internal const val DATASTORE_FILENAME = "immaru.preferences_pb" // Protobuf file

class DataStoreConfigurationRepository(
    private val dataStore: DataStore<Preferences>
) {
    fun getValue(key: String): Flow<String?> {
        return dataStore.data.map { preferences ->
            preferences[stringPreferencesKey(key)]
        }
    }

    suspend fun savePreference(key: String, value: String) {
        dataStore.edit { preferences ->
            preferences[stringPreferencesKey(key)] = value
        }
    }
}