import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.earthrevealed.immaru.ImmaruApp
import org.koin.dsl.module

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Immaru",
    ) {
        ImmaruApp(
            module {
                single {
                    createJvmDataStore()
                }
            }
        )
    }
}