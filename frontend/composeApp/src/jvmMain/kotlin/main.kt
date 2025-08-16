import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.earthrevealed.immaru.ImmaruApp
import io.github.vinceglb.filekit.FileKit
import org.koin.dsl.module

fun main() = application {
    FileKit.init(appId = "Immaru")

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