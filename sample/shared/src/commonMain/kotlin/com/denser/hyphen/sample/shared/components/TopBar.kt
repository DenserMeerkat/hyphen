package com.denser.hyphen.sample.shared.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButton
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
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.denser.hyphen.sample.shared.HyphenToolbar
import com.denser.hyphen.state.HyphenTextState
import hyphen.sample.shared.generated.resources.Res
import hyphen.sample.shared.generated.resources.bug_report_24dp
import hyphen.sample.shared.generated.resources.dark_mode_24dp
import hyphen.sample.shared.generated.resources.github
import hyphen.sample.shared.generated.resources.light_mode_24dp
import hyphen.sample.shared.generated.resources.markdown_24dp
import hyphen.sample.shared.generated.resources.more_vert_24dp
import hyphen.sample.shared.generated.resources.restart_alt_24dp
import hyphen.sample.shared.generated.resources.restore_page_24dp
import hyphen.sample.shared.generated.resources.save_24dp
import org.jetbrains.compose.resources.painterResource

@Composable
fun SampleTopBar(
    state: HyphenTextState,
    showPanel: Boolean,
    showMarkdown: Boolean,
    isDarkTheme: Boolean,
    onTogglePanel: () -> Unit,
    onToggleMarkdown: () -> Unit,
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Hyphen",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "demo",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    val uriHandler = LocalUriHandler.current
                    IconButton(
                        onClick = { uriHandler.openUri("https://github.com/DenserMeerkat/hyphen") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .size(40.dp)
                            .focusProperties { canFocus = false },
                    ) {
                        Icon(
                            painterResource(Res.drawable.github),
                            contentDescription = "GitHub",
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    IconToggleButton(
                        checked = showMarkdown,
                        onCheckedChange = { onToggleMarkdown() },
                        modifier = Modifier
                            .size(40.dp)
                            .focusProperties { canFocus = false },
                        shape = RoundedCornerShape(12.dp),
                        colors = IconButtonDefaults.iconToggleButtonColors(
                            checkedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            checkedContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            containerColor = Color.Transparent,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                    ) {
                        Icon(
                            painterResource(Res.drawable.markdown_24dp),
                            contentDescription = "Toggle markdown preview",
                        )
                    }
                    IconToggleButton(
                        checked = showPanel,
                        onCheckedChange = { onTogglePanel() },
                        modifier = Modifier
                            .size(40.dp)
                            .focusProperties { canFocus = false },
                        shape = RoundedCornerShape(12.dp),
                        colors = IconButtonDefaults.iconToggleButtonColors(
                            checkedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            checkedContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            containerColor = Color.Transparent,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                    ) {
                        Icon(
                            painterResource(Res.drawable.bug_report_24dp),
                            contentDescription = "Toggle Debugger",
                        )
                    }

                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .size(40.dp)
                                .focusProperties { canFocus = false },
                        ) {
                            Icon(
                                painterResource(Res.drawable.more_vert_24dp),
                                contentDescription = "More options",
                                modifier = Modifier.size(20.dp),
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                        ) {
                            DropdownMenuItem(
                                text = { Text(if (isDarkTheme) "Light Mode" else "Dark Mode") },
                                onClick = {
                                    onToggleTheme()
                                    showMenu = false
                                },
                                leadingIcon = {
                                    Icon(
                                        painterResource(if (isDarkTheme) Res.drawable.light_mode_24dp else Res.drawable.dark_mode_24dp),
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp),
                                    )
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Save Draft") },
                                onClick = {
                                    onSave()
                                    showMenu = false
                                },
                                leadingIcon = {
                                    Icon(
                                        painterResource(Res.drawable.save_24dp),
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp),
                                    )
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Reload Editor") },
                                onClick = {
                                    onReload()
                                    showMenu = false
                                },
                                leadingIcon = {
                                    Icon(
                                        painterResource(Res.drawable.restore_page_24dp),
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp),
                                    )
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Reset to Demo") },
                                onClick = {
                                    onReset()
                                    showMenu = false
                                },
                                leadingIcon = {
                                    Icon(
                                        painterResource(Res.drawable.restart_alt_24dp),
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp),
                                    )
                                }
                            )
                        }
                    }
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
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
