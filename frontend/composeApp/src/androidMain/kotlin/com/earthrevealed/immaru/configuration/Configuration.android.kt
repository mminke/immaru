import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

actual object Configuration {
    actual val immaruUrl = "http://10.0.2.2:8080"
}

fun createAndroidDataStore(context: Context): DataStore<Preferences> = createDataStore(
    producePath = { context.filesDir.resolve(DATASTORE_FILENAME).absolutePath }
)
