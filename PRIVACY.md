# Privacy

Simple Keyboard processes typed text locally to provide suggestions and, when enabled, learns words and word combinations on this device. The app does not request INTERNET permission. Learned data is stored in the app's private storage. Personalized learning can be disabled in Preferences.

Password fields, fields marked to suppress suggestions, and fields requesting no personalized learning are excluded from this keyboard's suggestion and learning pipeline. Email fields do not use the personal learning model; optional common-domain completions and device-account suggestions are separate controls and both are off by default. Account suggestions ask for Android's GET_ACCOUNTS permission when enabled, read addresses locally, and can show only accounts Android makes visible to the app. Autocorrection is off by default. When enabled, an immediate Backspace restores an automatically corrected word; rejected corrections may be saved locally if personalized learning is enabled, and are removed when learned data is cleared.

The clipboard is read only when you use the keyboard's paste action; its contents are not shown as predictive suggestions. Holding the clipboard button shows up to three short texts previously pasted through this keyboard. This history is kept in process memory only, never reads the system clipboard in the background, and is cleared on entering a protected or nonpersonalized field, when the keyboard service stops, or by using the clear button. Text longer than 500 characters and nontext clipboard items are not retained.

Android's app backup is disabled for this app. Uninstalling it removes its app-private data. The settings screen offers the app's learning-data controls.
