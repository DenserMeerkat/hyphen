package com.denser.hyphen.sample.shared.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.denser.hyphen.sample.shared.EditorMode
import com.denser.hyphen.sample.shared.HyphenToolbar
import com.denser.hyphen.sample.shared.PlaygroundState
import com.denser.hyphen.state.HyphenTextState
import hyphen.sample.shared.generated.resources.Res
import hyphen.sample.shared.generated.resources.dark_mode_24dp
import hyphen.sample.shared.generated.resources.format_textdirection_r_to_l_24dp
import hyphen.sample.shared.generated.resources.github
import hyphen.sample.shared.generated.resources.light_mode_24dp
import hyphen.sample.shared.generated.resources.more_vert_24dp
import hyphen.sample.shared.generated.resources.restart_alt_24dp
import hyphen.sample.shared.generated.resources.restore_page_24dp
import hyphen.sample.shared.generated.resources.save_24dp
import hyphen.sample.shared.generated.resources.text_fields_alt_24dp
import org.jetbrains.compose.resources.painterResource

@Composable
fun PlaygroundTopBar(
    state: HyphenTextState,
    playgroundState: PlaygroundState,
    isDarkTheme: Boolean,
    isWide: Boolean,
    onToggleTheme: () -> Unit,
    onSave: () -> Unit,
    onReload: () -> Unit,
    onReset: () -> Unit,
) {
    var showMenu by remember { mutableStateOf(false) }

    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 0.dp,
    ) {
        Column {
            // ── IntelliJ IDE Top Header Row ──────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Top-Left: Brand Title & RTL toggle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "Hyphen",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )

                    // RTL toggle badge
                    FilterChip(
                        selected = playgroundState.isRtl,
                        onClick = { playgroundState.isRtl = !playgroundState.isRtl },
                        leadingIcon = {
                            Icon(
                                painterResource(Res.drawable.format_textdirection_r_to_l_24dp),
                                contentDescription = null,
                                modifier = Modifier.size(13.dp),
                            )
                        },
                        label = { Text("RTL", style = MaterialTheme.typography.labelSmall) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onTertiaryContainer,
                        ),
                        modifier = Modifier.height(28.dp),
                    )
                }

                // Top-Right: Quick Actions (Theme, Save/Reload/Reset menu, GitHub)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    val uriHandler = LocalUriHandler.current

                    IconButton(
                        onClick = onToggleTheme,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .size(32.dp)
                            .focusProperties { canFocus = false },
                    ) {
                        Icon(
                            painterResource(
                                if (isDarkTheme) Res.drawable.light_mode_24dp
                                else Res.drawable.dark_mode_24dp,
                            ),
                            contentDescription = "Toggle theme",
                            modifier = Modifier.size(16.dp),
                        )
                    }

                    IconButton(
                        onClick = { uriHandler.openUri("https://github.com/DenserMeerkat/hyphen") },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .size(32.dp)
                            .focusProperties { canFocus = false },
                    ) {
                        Icon(
                            painterResource(Res.drawable.github),
                            contentDescription = "GitHub repository",
                            modifier = Modifier.size(16.dp),
                        )
                    }

                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .size(32.dp)
                                .focusProperties { canFocus = false },
                        ) {
                            Icon(
                                painterResource(Res.drawable.more_vert_24dp),
                                contentDescription = "More actions",
                                modifier = Modifier.size(16.dp),
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                        ) {
                            DropdownMenuItem(
                                text = { Text("Save draft") },
                                onClick = { onSave(); showMenu = false },
                                leadingIcon = {
                                    Icon(
                                        painterResource(Res.drawable.save_24dp),
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                    )
                                },
                            )
                            DropdownMenuItem(
                                text = { Text("Reload draft") },
                                onClick = { onReload(); showMenu = false },
                                leadingIcon = {
                                    Icon(
                                        painterResource(Res.drawable.restore_page_24dp),
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                    )
                                },
                            )
                            DropdownMenuItem(
                                text = { Text("Reset to demo text") },
                                onClick = { onReset(); showMenu = false },
                                leadingIcon = {
                                    Icon(
                                        painterResource(Res.drawable.restart_alt_24dp),
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                    )
                                },
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // ── Formatting Toolbar Row ───────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceContainerLow),
            ) {
                HyphenToolbar(state = state)
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }
    }
}
