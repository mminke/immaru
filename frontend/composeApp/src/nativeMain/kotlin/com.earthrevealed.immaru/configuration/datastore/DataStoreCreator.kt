import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.earthrevealed.immaru.configuration.datastore.DATASTORE_FILENAME
import com.earthrevealed.immaru.configuration.datastore.createDataStore
import kotlinx.io.files.Path

fun createNativeDataStore(): DataStore<Preferences> = createDataStore(
    producePath = { Path("config", DATASTORE_FILENAME).toString().also {
        println("Using config path: $it")
    } }
)