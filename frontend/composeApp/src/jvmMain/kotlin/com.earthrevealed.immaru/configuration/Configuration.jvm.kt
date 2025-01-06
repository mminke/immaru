import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import kotlin.io.path.Path

actual object Configuration {
    actual val immaruUrl = "http://localhost:8080"
}

fun createJvmDataStore(): DataStore<Preferences> = createDataStore(
    producePath = { Path("config").toAbsolutePath().toString().also {
        println("Using config path: $it")
    } }
)