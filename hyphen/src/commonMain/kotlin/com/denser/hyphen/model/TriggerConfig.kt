package com.denser.hyphen.model

import androidx.compose.runtime.Immutable

/**
 * Configuration for a character sequence that triggers a popup menu in the editor.
 *
 * @property trigger The character sequence that starts the trigger (e.g., "@", "#", "{{").
 * @property scheme The URI scheme used for serialization in Markdown (e.g., "mention", "tag", "var").
 *   Serialized as `[Display](scheme:ID)`.
 * @property endTrigger Optional character sequence that manually terminates the trigger (e.g., "}}").
 * @property endOnSpace Whether a space character automatically terminates the trigger query.
 *   Defaults to `true`.
 */
@Immutable
data class TriggerConfig(
    val trigger: String,
    val scheme: String = "mention",
    val endTrigger: String? = null,
    val addSpaceOnCompletion: Boolean = true,
)

/**
 * Current state of an active trigger being typed by the user.
 *
 * @property config The configuration that was matched.
 * @property startIndex The index in the plain text where the [TriggerConfig.trigger] sequence starts.
 * @property query The text typed after the trigger sequence, used for filtering options.
 */
@Immutable
data class TriggerState(
    val config: TriggerConfig,
    val startIndex: Int,
    val query: String,
)
