package com.denser.hyphen.sample.shared.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.denser.hyphen.model.TriggerState
import com.denser.hyphen.state.HyphenTextState
import com.denser.hyphen.ui.mention.SuggestionItem
import com.denser.hyphen.ui.mention.TriggerSuggestions
import hyphen.sample.shared.generated.resources.Res
import hyphen.sample.shared.generated.resources.bolt_24dp
import hyphen.sample.shared.generated.resources.label_24dp
import hyphen.sample.shared.generated.resources.person_24dp
import hyphen.sample.shared.generated.resources.restore_page_24dp
import org.jetbrains.compose.resources.painterResource

fun getSuggestionsForTrigger(trigger: TriggerState): List<String> {
    val list = when (trigger.config.trigger) {
        "@" -> listOf("Alice", "Bob", "Charlie", "David", "Eve", "Frank", "Grace")
        "#" -> listOf("bug", "feature", "enhancement", "question", "wontfix", "duplicate")
        "{" -> listOf("project_name", "user_count", "today_date", "current_time", "version")
        "{{" -> listOf("project_name", "user_count", "today_date", "current_time", "version")
        else -> emptyList()
    }
    return list.filter { it.contains(trigger.query, ignoreCase = true) }
}

@Composable
fun SampleTriggerPopup(
    trigger: TriggerState,
    state: HyphenTextState
) {
    val options = remember(trigger.config.trigger, trigger.query) {
        getSuggestionsForTrigger(trigger)
    }

    val suggestions = options.map { option ->
        SuggestionItem(
            id = option,
            display = option,
            subtitle = when(trigger.config.trigger) {
                "@" -> "Team Member"
                "#" -> "Label"
                "{" -> "Variable"
                "{{" -> "Dynamic Var"
                else -> "Suggestion"
            },
            icon = {
                Icon(
                    painterResource(
                        when(trigger.config.trigger) {
                            "@" -> Res.drawable.person_24dp
                            "#" -> Res.drawable.label_24dp
                            "{" -> Res.drawable.bolt_24dp
                            "{{" -> Res.drawable.bolt_24dp
                            else -> Res.drawable.restore_page_24dp
                        }
                    ),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        )
    }

    TriggerSuggestions(
        state = state,
        trigger = trigger,
        items = suggestions,
        onSelect = { item ->
            val triggerPrefix = trigger.config.trigger
            val triggerEnd = trigger.config.endTrigger ?: ""
            state.completeMention(
                id = item.id,
                display = "$triggerPrefix${item.display}$triggerEnd"
            )
        }
    )
}

@Composable
fun SuggestionsBottomBar(
    state: HyphenTextState,
    modifier: Modifier = Modifier
) {
    val trigger = state.activeTrigger ?: return
    
    val options = remember(trigger.config.trigger, trigger.query) {
        getSuggestionsForTrigger(trigger)
    }

    if (options.isEmpty()) return

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            options.forEach { option ->
                Surface(
                    onClick = {
                        val triggerPrefix = trigger.config.trigger
                        val triggerEnd = trigger.config.endTrigger ?: ""
                        state.completeMention(
                            id = option,
                            display = "$triggerPrefix$option$triggerEnd"
                        )
                    },
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            painter = painterResource(
                                when (trigger.config.trigger) {
                                    "@" -> Res.drawable.person_24dp
                                    "#" -> Res.drawable.label_24dp
                                    "{" -> Res.drawable.bolt_24dp
                                    "{{" -> Res.drawable.bolt_24dp
                                    else -> Res.drawable.restore_page_24dp
                                }
                            ),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = option,
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }
            }
        }
    }
}
