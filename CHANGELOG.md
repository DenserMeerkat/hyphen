## [0.6.0-alpha01] - 2026-07-05

### 🚀 Added
* **Blockquote Block:** Added support for rendering blockquote blocks with background color, rounded corners, and a customizable left border.

### 🛠️ Fixed
* **Shift + Enter Behaviour:** Allowed insertion of standard newline characters using Shift + Enter key combination to bypass the editor's block auto-formatting / smart enter logic.

---

## [0.5.0-alpha07] - 2026-06-20

### 🛠️ Fixed
* **Text Wrapping:** Fixed text wrapping behavior by converting trailing space characters to non-breaking spaces (`\u00A0`) and restoring them to normal spaces when they are no longer at the line boundary.
* **Popup Positioning:** Fixed out-of-bounds positioning logic for the inline suggestions popup.

---

## [0.5.0-alpha06] - 2026-06-07

### 🛠️ Fixed
* **Nested Inline Styles:** Fixed parsing failure of nested formatting (e.g. `***__nestedformatting__***`) during editor reloads due to strict overlap checks.

---

## [0.5.0-alpha05] - 2026-06-03

### 🛠️ Fixed
* **Link Display Name Editing:** Resolved an issue where editing a link's display text through the modal dialogue interface broke the link styling.

---

## [0.5.0-alpha04] - 2026-05-30

### ✨ Added
* **Raw Clipboard Bypass (`LocalHyphenRawClipboard`):** Exposes the raw, unintercepted system clipboard so custom editor UI (like link context menus) can write clean values directly without being overridden by Markdown serialization of active editor selections. Essential for Android.

### 🛠️ Fixed
* **Mention Entity Integrity:** Mentions are now treated as single, atomic entities during navigation and backspace/deletions, preventing orphaned syntax.
* **Editor Deletion & State Stability:** Fixed edge-case boundary overlaps on inter-span deletions and visual cursor consistency.

---

## [0.5.0-alpha03] - 2026-05-28

### 🛠️ Fixed
* **Clipboard Serialization Regression:** Reverted the clipboard-overriding guard introduced in `alpha02` which caused serialization to silently break for all text copies containing headings or checkboxes, due to a mismatch between the visual buffer text and the raw state text used in the equality check.

---

## [0.5.0-alpha02] - 2026-05-28

### 🛠️ Fixed
* **Autocomplete & Mention Robustness:** Added the optional `trigger` parameter to `completeMention()` to allow passing explicit trigger context, and prevented active trigger resetting on unfocused selection changes to avoid unintended suggestion popup dismissals.
* **Clipboard Selection Overriding:** Fixed an issue where programmatic or platform-specific copy operations (e.g. copying just the URL of a link) were incorrectly intercepted and overridden by the editor's active text selection checks.

---

## [0.5.0-alpha01] - 2026-05-25

### ✨ Added
* **Mentions & Autocomplete:** Added robust trigger-based autocomplete and interactive mention handling support.
* **Built-in Autocomplete UI:** Introduced the Material 3 `TriggerSuggestions` popup helper for managing option rendering and key events automatically.
* **Flexible State Initialization:** Added optional `triggerConfigs` constructor support to `HyphenTextState` and `rememberHyphenTextState()` to parse mention formatting inside initial texts seamlessly.

---

## [0.4.0-alpha01] - 2026-03-30

### ✨ Added
* **Links Support:** Integrated link formatting with full support for custom dropdowns and link-editing dialogs.
* **Shortcut:** Added `Ctrl/Cmd + K` shortcut to toggle links.
* **New Checkbox UI:** Modernized checkbox design with improved visual alignment and interactive feedback.

### 🛠️ Fixed
* **Text Consistency:** Eliminated manual Zero Width Space (ZWSP) insertion, resolving state synchronization issues and redundant characters that caused cursor "ghosting" behavior.

---

## [0.3.0-alpha01] - 2026-03-18

### ✨ Added
* **Task Lists:** Added support for Markdown checkboxes (`- [ ]` and `- [x]`).
* **List Item Styling:** Introduced `ListItemStyle` to independently style list markers (bullets, numbers, checkboxes) and their content.
* **Shortcut:** Added `Ctrl/Cmd + Enter` shortcut to toggle checkbox states.

### 🛠️ Fixed
* **Style Boundaries & Cursor Sync:** Fixed span destruction on partial character deletion.
* **Continuous Typing:** Enabled continuous typing inside empty tags.

---

## [0.2.0-alpha01] - 2026-03-14

### ✨ Added
* **Markdown Headings:** Support for H1–H6 block-level formatting.

### 🛠️ Fixed
* **Clipboard Cut:** Resolved cross-platform race conditions to ensure the "Cut" action captures Markdown instead of plain text.
* **Span Alignment:** Fixed logic errors in `MarkdownProcessor` that occurred when processing multiple nested styles within the same text range.

---

## [0.1.0-alpha01] - 2026-03-08

### ✨ Added
* **Initial Release:** Core architecture for rich text Markdown editing in Compose.
* **Inline & Block Styles:** Support for Bold, Italic, Lists, and Blockquotes.
* **Clipboard Interception:** Custom clipboard handling to serialize styled text to Markdown.
* **Hardware Shortcuts:** Integrated keybindings for common formatting tasks.
* **Undo/Redo:** Full history management for text and formatting changes.