import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.earthrevealed.immaru.configuration.datastore.DATASTORE_FILENAME
import com.earthrevealed.immaru.configuration.datastore.createDataStore

fun createAndroidDataStore(context: Context): DataStore<Preferences> = createDataStore(
    producePath = { context.filesDir.resolve(DATASTORE_FILENAME).absolutePath }
)
