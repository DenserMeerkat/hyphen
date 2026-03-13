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