# Hyphen

<picture>
  <source srcset="assets/images/banner.jpg">
  <img alt="Hyphen — WYSIWYG Markdown editor for Compose Multiplatform" src="docs/images/banner.jpg">
</picture>

<br>

<p align="center">
  A <strong>WYSIWYG Markdown editor</strong> for Compose Multiplatform.<br>
  Type in Markdown, see formatting live. Copy as Markdown. Works on Android, Desktop, and Web.
</p>

<p align="center">
  <a href="https://github.com/densermeerkat/hyphen/releases"><img alt="Maven Central" src="https://img.shields.io/maven-central/v/io.github.densermeerkat/hyphen?color=4CAF50&label=Maven%20Central"></a>
  <a href="https://kotlinlang.org"><img alt="Kotlin" src="https://img.shields.io/badge/Kotlin-2.2.21-7F52FF?logo=kotlin&logoColor=white"></a>
  <a href="https://www.jetbrains.com/compose-multiplatform/"><img alt="Compose Multiplatform" src="https://img.shields.io/badge/Compose%20Multiplatform-1.10.1-4285F4?logo=jetpackcompose&logoColor=white"></a>
  <img alt="Platforms" src="https://img.shields.io/badge/Platforms-Android%20%7C%20Desktop%20%7C%20Web-orange">
</p>

<p align="center">
  <a href="https://densermeerkat.github.io/hyphen/"><strong>→ Try the live web demo</strong></a>
</p>

<picture>
  <source media="(prefers-color-scheme: dark)" srcset="assets/images/demo_dark.png">
  <source media="(prefers-color-scheme: light)" srcset="assets/images/demo_light.png">
  <img alt="Hyphen editor Demo screenshot" src="assets/images/demo_light.png" width="100%">
</picture>

## Features

### ✍️ Live Markdown Input

Type Markdown syntax directly and watch it convert as you write — no mode switching, no preview pane required.

| Syntax                  | Style                |
|-------------------------|----------------------|
| `**text**`              | **Bold**             |
| `*text*`                | _Italic_             |
| `__text__`              | Underline            |
| `` `text` ``            | `Inline code`        |
| `~~text~~`              | ~~Strikethrough~~    |
| `==text==`              | Highlight            |
| `# ` at line start      | Heading 1            |
| `## ` at line start     | Heading 2            |
| `### ` at line start    | Heading 3            |
| `#### ` at line start   | Heading 4            |
| `##### ` at line start  | Heading 5            |
| `###### ` at line start | Heading 6            |
| `- ` at line start      | Bullet list          |
| `1. ` at line start     | Ordered list         |
| `> ` at line start      | Blockquote           |
| `- [ ] ` at line start  | Checkbox (unchecked) |
| `- [x] ` at line start  | Checkbox (checked)   |

### 📋 Markdown Clipboard

Cut, copy, and paste all work across Android, Desktop, and Web. Copying a selection serializes it to Markdown automatically, paste into any Markdown-aware editor and all formatting travels with it.

### ⌨️ Keyboard Shortcuts

Full hardware keyboard support on Desktop and Web:

| Shortcut                 | Action                          |
|--------------------------|---------------------------------|
| `Ctrl / Cmd + B`         | Toggle bold                     |
| `Ctrl / Cmd + I`         | Toggle italic                   |
| `Ctrl / Cmd + U`         | Toggle underline                |
| `Ctrl / Cmd + Shift + S` | Toggle strikethrough            |
| `Ctrl / Cmd + Shift + X` | Toggle strikethrough            |
| `Ctrl / Cmd + Alt + X`   | Toggle strikethrough            |
| `Ctrl / Cmd + Shift + H` | Toggle highlight                |
| `Ctrl / Cmd + Space`     | Clear all styles on selection   |
| `Ctrl / Cmd + 1`         | Toggle Heading 1                |
| `Ctrl / Cmd + 2`         | Toggle Heading 2                |
| `Ctrl / Cmd + 3`         | Toggle Heading 3                |
| `Ctrl / Cmd + 4`         | Toggle Heading 4                |
| `Ctrl / Cmd + 5`         | Toggle Heading 5                |
| `Ctrl / Cmd + 6`         | Toggle Heading 6                |
| `Ctrl / Cmd + Enter`     | Toggle checkbox on current line |
| `Ctrl / Cmd + K`         | Toggle link on selection        |
| `Ctrl / Cmd + Z`         | Undo                            |
| `Ctrl / Cmd + Y`         | Redo                            |
| `Ctrl / Cmd + Shift + Z` | Redo                            |

### ↩️ Undo / Redo History

Granular history with snapshots saved at word boundaries, pastes, and Markdown conversions. The redo stack is maintained correctly across all operations, including toolbar toggles and programmatic edits.

### 🌍 Compose Multiplatform

Single shared implementation targeting Android, Desktop (JVM), and Web (WasmJS / JS).

---

## Installation

### Using `libs.versions.toml` (recommended)

Add the version and library entry to your version catalog:

**`gradle/libs.versions.toml`**

```toml
[versions]
hyphen = "0.3.0-alpha01"

[libraries]
hyphen = { group = "io.github.densermeerkat", name = "hyphen", version.ref = "hyphen" }
```

Then reference it in your shared module:

**`shared/build.gradle.kts`**

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.hyphen)
        }
    }
}
```

> `commonMain` is the source set that compiles for every target at once — Android, Desktop, and Web. Declaring Hyphen there means you write the dependency once and all platforms pick it up automatically.

### Using string notation

```kotlin
// shared/build.gradle.kts
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("io.github.densermeerkat:hyphen:0.3.0-alpha01")
        }
    }
}
```

## Quick Start

```kotlin
val state = rememberHyphenTextState(
    initialText = "**Hello**, *Hyphen*!"
)

HyphenTextField(
    state = state,
    label = { Text("Notes") },
)

// Read the result at any time
val markdown = state.toMarkdown()
```

---

## Choosing an Editor Component

Hyphen ships two editor composables. Use whichever fits your design:

### `HyphenBasicTextEditor`

A thin wrapper around `BasicTextField` with no decoration. Use this when you control the layout yourself or want full design freedom.

```kotlin
HyphenBasicTextEditor(
    state = state,
    modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
    textStyle = TextStyle(
        fontSize = 16.sp,
        color = MaterialTheme.colorScheme.onSurface,
    ),
    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
    onMarkdownChange = { markdown -> /* sync to ViewModel */ },
)
```

### `HyphenTextField` _(Material 3)_

Wraps `HyphenBasicTextEditor` inside a standard Material3 filled text field decorator — labels, placeholder, leading/trailing icons, supporting text, and error state all work out of the box.

```kotlin
HyphenTextField(
    state = state,
    label = { Text("Notes") },
    placeholder = { Text("Start typing…") },
    trailingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
    supportingText = { Text("Markdown supported") },
    modifier = Modifier.fillMaxWidth(),
)
```

Both composables accept the same `styleConfig`, `onTextChange`, `onMarkdownChange`, and `clipboardLabel` parameters. The Material3 variant additionally accepts `colors`, `shape`, `labelPosition`, `contentPadding`, and all standard decoration slots.

---

## Usage

### Toolbar buttons — keeping focus on Desktop & Web

On Desktop and Web, clicking a button moves keyboard focus away from the editor. This causes the text selection to be lost before the style toggle runs. Fix this by adding `focusProperties { canFocus = false }` to every toolbar button so focus never leaves the editor when a button is tapped:

```kotlin
IconToggleButton(
    checked = state.hasStyle(MarkupStyle.Bold),
    onCheckedChange = { state.toggleStyle(MarkupStyle.Bold) },
    modifier = Modifier.focusProperties { canFocus = false }, // ← required on Desktop & Web
) {
    Icon(Icons.Default.FormatBold, contentDescription = "Bold")
}
```

This applies to any clickable element in your toolbar — `IconButton`, `Button`, `IconToggleButton`, etc.

### Custom style config

```kotlin
HyphenBasicTextEditor(
    state = state,
    styleConfig = HyphenStyleConfig(
        boldStyle = SpanStyle(
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF1A73E8),
        ),
        highlightStyle = SpanStyle(
            background = Color(0xFFFFF176),
        ),
        inlineCodeStyle = SpanStyle(
            background = Color(0xFFF1F3F4),
            fontFamily = FontFamily.Monospace,
            color = Color(0xFFD93025),
        ),
    ),
)
```

### Programmatic control

```kotlin
// Load new Markdown content (resets undo history)
state.setMarkdown("# New content\n\nHello!")

// Toggle formatting from a custom button
Button(onClick = { state.toggleStyle(MarkupStyle.Bold) }) { Text("B") }

// Remove all inline formatting from the current selection
Button(onClick = { state.clearAllStyles() }) { Text("Clear") }

// Undo / redo
state.undo()
state.redo()
```

### Reactive observation

```kotlin
// Callback — fires on every text or formatting change
HyphenBasicTextEditor(
    state = state,
    onMarkdownChange = { markdown -> viewModel.onContentChanged(markdown) },
)

// Flow — collect anywhere, debounce freely
viewModelScope.launch {
    state.markdownFlow
        .debounce(500)
        .collect { markdown -> repository.save(markdown) }
}
```

---

## API Reference

### `HyphenBasicTextEditor`

| Parameter           | Type                        | Default                       | Description                                                                                     |
|:--------------------|:----------------------------|:------------------------------|:------------------------------------------------------------------------------------------------|
| `state`             | `HyphenTextState`           | **Required**                  | Holds text content, spans, selection, and undo/redo history.                                    |
| `modifier`          | `Modifier`                  | `Modifier`                    | Applied to the underlying `BasicTextField`.                                                     |
| `enabled`           | `Boolean`                   | `true`                        | When `false`, the field is neither editable nor focusable.                                      |
| `readOnly`          | `Boolean`                   | `false`                       | When `true`, the field cannot be modified but can be focused and copied.                        |
| `textStyle`         | `TextStyle`                 | `TextStyle(fontSize = 16.sp)` | Typographic style applied to the visible text.                                                  |
| `styleConfig`       | `HyphenStyleConfig`         | `HyphenStyleConfig()`         | Visual appearance of each `MarkupStyle`.                                                        |
| `linkConfig`        | `HyphenLinkConfig`          | `HyphenLinkConfig()`          | Interaction configuration for link spans (menus, dialogs, opening URLs).                        |
| `keyboardOptions`   | `KeyboardOptions`           | Sentences, no autocorrect     | Software keyboard options.                                                                      |
| `lineLimits`        | `TextFieldLineLimits`       | `TextFieldLineLimits.Default` | Single-line or multi-line behaviour.                                                            |
| `scrollState`       | `ScrollState`               | `rememberScrollState()`       | Controls vertical or horizontal scroll of the field content.                                    |
| `interactionSource` | `MutableInteractionSource?` | `null`                        | Hoist to observe focus, hover, and press interactions externally.                               |
| `cursorBrush`       | `Brush`                     | `SolidColor(Color.Black)`     | Brush used to paint the cursor.                                                                 |
| `decorator`         | `TextFieldDecorator?`       | `null`                        | Optional decorator for external visual styling.                                                 |
| `onTextLayout`      | `(Density.(...) -> Unit)?`  | `null`                        | Callback invoked on every text layout recalculation.                                            |
| `clipboardLabel`    | `String`                    | `"Markdown Text"`             | Label attached to clipboard entries on copy/cut.                                                |
| `onTextChange`      | `((String) -> Unit)?`       | `null`                        | Callback invoked whenever the plain text changes.                                               |
| `onMarkdownChange`  | `((String) -> Unit)?`       | `null`                        | Callback invoked whenever text or formatting changes, providing the serialized Markdown string. |

### `HyphenTextState`

| Property / Method             | Type / Return               | Description                                                                                |
|:------------------------------|:----------------------------|:-------------------------------------------------------------------------------------------|
| `textFieldState`              | `TextFieldState`            | The underlying Compose foundation state driving the editor.                                |
| `text`                        | `String`                    | Plain text with all Markdown syntax stripped.                                              |
| `selection`                   | `TextRange`                 | Current cursor position or selected range.                                                 |
| `spans`                       | `List<MarkupStyleRange>`    | Snapshot-observable list of active formatting spans.                                       |
| `pendingOverrides`            | `Map<MarkupStyle, Boolean>` | Transient style intent applied to the next typed characters.                               |
| `canUndo` / `canRedo`         | `Boolean`                   | `true` if undo/redo actions are available in the history stack.                            |
| `activeLinkForEditing`        | `MarkupStyleRange?`         | The link span currently being edited via the built-in dialog.                              |
| `isFocused`                   | `Boolean`                   | Whether the text field currently has input focus.                                          |
| `toggleStyle(style)`          | `Unit`                      | Toggles an inline or block style on the current selection.                                 |
| `toggleCheckbox(index?)`      | `Unit`                      | Toggles the checked/unchecked state of checkboxes in the selection or at a specific index. |
| `clearAllStyles()`            | `Unit`                      | Removes all inline formatting from the selection; suppresses at cursor.                    |
| `toggleLink()`                | `Unit`                      | Wraps selection in a link, or opens an existing link at the cursor for editing.            |
| `updateLink(span, text, url)` | `Unit`                      | Updates an existing link's display text and URL.                                           |
| `hasStyle(style)`             | `Boolean`                   | `true` if the style is active at the current selection or cursor.                          |
| `isStyleAt(index, style)`     | `Boolean`                   | Point query against the span list (ignores selection / overrides).                         |
| `clearPendingOverrides()`     | `Unit`                      | Resets transient typing intent.                                                            |
| `undo()` / `redo()`           | `Unit`                      | Navigates the undo / redo history stack.                                                   |
| `toMarkdown(start?, end?)`    | `String`                    | Serializes content (or a substring range) to a Markdown formatted string.                  |
| `setMarkdown(markdown)`       | `Unit`                      | Replaces all content programmatically, parses it, and resets history.                      |
| `markdownFlow`                | `Flow<String>`              | Emits the serialized Markdown string on every text or formatting change.                   |

### `HyphenStyleConfig`

| Property                 | Default Value                                                                                      |
|--------------------------|----------------------------------------------------------------------------------------------------|
| `boldStyle`              | `SpanStyle(fontWeight = FontWeight.Bold)`                                                          |
| `italicStyle`            | `SpanStyle(fontStyle = FontStyle.Italic)`                                                          |
| `underlineStyle`         | `SpanStyle(textDecoration = TextDecoration.Underline)`                                             |
| `strikethroughStyle`     | `SpanStyle(textDecoration = TextDecoration.LineThrough)`                                           |
| `highlightStyle`         | `SpanStyle(background = Color(0xFFFFEB3B).copy(alpha = 0.4f))`                                     |
| `inlineCodeStyle`        | `SpanStyle(background = Color.Gray.copy(alpha = 0.15f), fontFamily = FontFamily.Monospace)`        |
| `blockquoteSpanStyle`    | `SpanStyle(fontStyle = FontStyle.Italic, color = Color.Gray, background = Color.Gray.copy(0.05f))` |
| `bulletListStyle`        | `ListItemStyle()`                                                                                  |
| `orderedListStyle`       | `ListItemStyle()`                                                                                  |
| `checkboxCheckedStyle`   | `SpanStyle(textDecoration = TextDecoration.LineThrough)`                                           |
| `checkboxUncheckedStyle` | `null`                                                                                             |
| `h1Style`                | `SpanStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold)`                                        |
| `h2Style`                | `SpanStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold)`                                        |
| `h3Style`                | `SpanStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold)`                                        |
| `h4Style`                | `SpanStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold)`                                        |
| `h5Style`                | `SpanStyle(fontSize = 17.sp, fontWeight = FontWeight.Bold)`                                        |
| `h6Style`                | `SpanStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold)`                                        |
| `linkStyle`              | `SpanStyle(color = Color.Blue, textDecoration = TextDecoration.Underline)`                         |

### `MarkupStyle`

```kotlin
// Inline styles
MarkupStyle.Bold
MarkupStyle.Italic
MarkupStyle.Underline
MarkupStyle.Strikethrough
MarkupStyle.InlineCode
MarkupStyle.Highlight

// Heading styles
MarkupStyle.H1
MarkupStyle.H2
MarkupStyle.H3
MarkupStyle.H4
MarkupStyle.H5
MarkupStyle.H6

// Block styles
MarkupStyle.BulletList
MarkupStyle.OrderedList
MarkupStyle.Blockquote
MarkupStyle.CheckboxUnchecked
MarkupStyle.CheckboxChecked
```

### `ListItemStyle`

Controls the prefix marker and content text of a list item independently. Used by `bulletListStyle`
and `orderedListStyle` on `HyphenStyleConfig`.

> [!NOTE]
> Checklist items (`- [ ]`, `- [x]`) do not use `ListItemStyle`. They use an overlay widget and can
> be styled via `checkboxCheckedStyle` and `checkboxUncheckedStyle` (which take a `SpanStyle?`).

| Property       | Type         | Default | Description                                          |
|----------------|--------------|---------|------------------------------------------------------|
| `prefixStyle`  | `SpanStyle?` | `null`  | Applied to the marker (`-`, `1.`, `- [ ]`, `- [x]`). |
| `contentStyle` | `SpanStyle?` | `null`  | Applied to the text after the marker.                |

### Checklist Styling

Checkboxes in Hyphen are rendered as native Material3 widgets overlaid on the editor. This ensures
they always match your theme and remain perfectly aligned regardless of font size. Use
`checkboxCheckedStyle` and `checkboxUncheckedStyle` to style the **label text** of the checklist
items:

```kotlin
HyphenBasicTextEditor(
    state = state,
    styleConfig = HyphenStyleConfig(
        checkboxCheckedStyle = SpanStyle(
            textDecoration = TextDecoration.LineThrough,
            color = Color.Gray,
        ),
    ),
)
```

---

## Supported Platforms

| Platform      | Status     |
|---------------|------------|
| Android       | ✅          |
| Desktop (JVM) | ✅          |
| Web (WasmJS)  | ✅          |
| Web (JS / IR) | ✅          |
| iOS           | 🚧 Planned |
