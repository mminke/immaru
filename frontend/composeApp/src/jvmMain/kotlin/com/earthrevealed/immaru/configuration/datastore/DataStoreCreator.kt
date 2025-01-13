import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.earthrevealed.immaru.configuration.datastore.DATASTORE_FILENAME
import com.earthrevealed.immaru.configuration.datastore.createDataStore
import kotlin.io.path.Path

fun createJvmDataStore(): DataStore<Preferences> = createDataStore(
    producePath = { Path("config").resolve(DATASTORE_FILENAME).toAbsolutePath().toString().also {
        println("Using config path: $it")
    } }
)