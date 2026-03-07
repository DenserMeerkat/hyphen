import androidx.compose.foundation.LocalScrollbarStyle
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.denser.hyphen.sample.shared.HyphenSampleApp

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport(viewportContainerId = "ComposeTarget") {
        HyphenSampleApp(
            verticalScrollbar = { scrollState, modifier ->
                VerticalScrollbar(
                    adapter = rememberScrollbarAdapter(scrollState),
                    modifier = modifier,
                    style = LocalScrollbarStyle.current
                )
            }
        )
    }
}