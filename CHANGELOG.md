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