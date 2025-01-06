import androidx.compose.ui.window.ComposeUIViewController
import com.earthrevaled.immaru.configuration.createIosDataStore
import com.earthrevealed.immaru.ImmaruApp
import org.koin.dsl.module

fun MainViewController() = ComposeUIViewController {
    ImmaruApp(
        module {
            single {
                createIosDataStore()
            }
        }
    )
}