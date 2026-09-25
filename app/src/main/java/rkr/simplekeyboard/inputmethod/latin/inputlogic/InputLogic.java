/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package rkr.simplekeyboard.inputmethod.latin.inputlogic;

import android.os.SystemClock;
import android.text.TextUtils;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;

import java.util.TreeSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import rkr.simplekeyboard.inputmethod.event.Event;
import rkr.simplekeyboard.inputmethod.event.InputTransaction;
import rkr.simplekeyboard.inputmethod.latin.LatinIME;
import rkr.simplekeyboard.inputmethod.latin.RichInputConnection;
import rkr.simplekeyboard.inputmethod.latin.common.Constants;
import rkr.simplekeyboard.inputmethod.latin.common.StringUtils;
import rkr.simplekeyboard.inputmethod.latin.learning.LocalLearningEngine;
import rkr.simplekeyboard.inputmethod.latin.settings.SettingsValues;
import rkr.simplekeyboard.inputmethod.latin.utils.InputTypeUtils;
import rkr.simplekeyboard.inputmethod.latin.utils.EmailSuggestionProvider;
import rkr.simplekeyboard.inputmethod.latin.utils.RecapitalizeStatus;
import rkr.simplekeyboard.inputmethod.latin.utils.SubtypeLocaleUtils;

/**
 * This class manages the input logic.
 */
public final class InputLogic {
    // TODO : Remove this member when we can.
    final LatinIME mLatinIME;

    // This has package visibility so it can be accessed from InputLogicHandler.
    public final RichInputConnection mConnection;
    private final RecapitalizeStatus mRecapitalizeStatus = new RecapitalizeStatus();
    
    // Learning engine for intelligent suggestions - initialized lazily
    private LocalLearningEngine mLearningEngine;
    // A single writer keeps the model ordered with respect to completed words and predictions.
    private final ExecutorService learningWorker = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "KeyboardLearning");
        thread.setPriority(Thread.MIN_PRIORITY);
        return thread;
    });
    private volatile int suggestionGeneration;
    private volatile boolean learningClosed;
    private final android.os.Handler suggestionHandler =
            new android.os.Handler(android.os.Looper.getMainLooper());
    private final Runnable suggestionUpdate = this::computeSuggestions;
    
    // Email suggestion provider for proactive email completion - initialized lazily
    private EmailSuggestionProvider mEmailSuggestionProvider;
    
    // Current word being typed for suggestion purposes
    private StringBuilder mCurrentWord = new StringBuilder();

    public final TreeSet<Long> mCurrentlyPressedHardwareKeys = new TreeSet<>();

    /**
     * Create a new instance of the input logic.
     * @param latinIME the instance of the parent LatinIME. We should remove this when we can.
     * dictionary.
     */
    public InputLogic(final LatinIME latinIME) {
        mLatinIME = latinIME;
        mConnection = new RichInputConnection(latinIME);
        // Initialize mLearningEngine lazily to avoid context issues during service creation
    }

    /**
     * Gets the learning engine, initializing it lazily if needed.
     */
    private boolean isPrivateField() {
        EditorInfo info = getCurrentInputEditorInfo();
        if (info == null) return true;
        return !new rkr.simplekeyboard.inputmethod.latin.InputAttributes(info, false)
                .mShouldShowSuggestions
                || (info.imeOptions & EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING) != 0;
    }

    private boolean shouldLearn() {
        return !isPrivateField() && rkr.simplekeyboard.inputmethod.compat.PreferenceManagerCompat
                .getDeviceSharedPreferences(mLatinIME)
                .getBoolean("pref_personalized_learning", true);
    }

    private LocalLearningEngine getLearningEngine() {
        if (android.os.Build.VERSION.SDK_INT >= 24) {
            android.os.UserManager userManager =
                    (android.os.UserManager) mLatinIME.getSystemService(android.content.Context.USER_SERVICE);
            if (userManager != null && !userManager.isUserUnlocked()) return null;
        }
        if (mLearningEngine == null) {
            try {
                // Ensure we have a valid context before initializing
                if (mLatinIME != null && mLatinIME.getApplicationContext() != null) {
                    mLearningEngine = LocalLearningEngine.getInstance(mLatinIME);
                } else {
                    // Context not ready yet, return null to defer initialization
                    return null;
                }
            } catch (Exception e) {
                // If initialization fails, log and return null to prevent crashes
                android.util.Log.e("InputLogic", "Failed to initialize learning engine", e);
                return null;
            }
        }
        return mLearningEngine;
    }

    /**
     * Gets the email suggestion provider, initializing it lazily if needed.
     */
    private EmailSuggestionProvider getEmailSuggestionProvider() {
        if (mEmailSuggestionProvider == null) {
            mEmailSuggestionProvider = new EmailSuggestionProvider(mLatinIME);
        }
        return mEmailSuggestionProvider;
    }

    /**
     * Initializes the input logic for input in an editor.
     *
     * Call this when input starts or restarts in some editor (typically, in onStartInputView).
     */
    public void startInput() {
        suggestionHandler.removeCallbacks(suggestionUpdate);
        suggestionGeneration++;
        mRecapitalizeStatus.disable(); // Do not perform recapitalize until the cursor is moved once
        mCurrentlyPressedHardwareKeys.clear();
        mCurrentWord.setLength(0); // Clear current word tracking
        updateSuggestions(); // Show initial suggestions
    }

    /**
     * Call this when the subtype changes.
     */
    public void onSubtypeChanged() {
        startInput();
    }

    public void finishInput() {
        suggestionHandler.removeCallbacks(suggestionUpdate);
        mCurrentWord.setLength(0);
    }

    /**
     * React to a string input.
     *
     * This is triggered by keys that input many characters at once, like the ".com" key or
     * some additional keys for example.
     *
     * @param settingsValues the current values of the settings.
     * @param event the input event containing the data.
     * @return the complete transaction object
     */
    public InputTransaction onTextInput(final SettingsValues settingsValues, final Event event) {
        final String rawText = event.getTextToCommit().toString();
        final InputTransaction inputTransaction = new InputTransaction(settingsValues);
        final String text = performSpecificTldProcessingOnTextInput(rawText);
        mConnection.commitText(text, 1);
        // Space state must be updated before calling updateShiftState
        inputTransaction.requireShiftUpdate(InputTransaction.SHIFT_UPDATE_NOW);
        return inputTransaction;
    }

    /**
     * Consider an update to the cursor position. Evaluate whether this update has happened as
     * part of normal typing or whether it was an explicit cursor move by the user. In any case,
     * do the necessary adjustments.
     * @param newSelStart new selection start
     * @param newSelEnd new selection end
     */
    public void onUpdateSelection(final int newSelStart, final int newSelEnd) {
        boolean cursorMoved = newSelStart != mConnection.getExpectedSelectionStart()
                || newSelEnd != mConnection.getExpectedSelectionEnd();
        mConnection.updateSelection(newSelStart, newSelEnd);
        
        if (cursorMoved) {
            suggestionGeneration++;
            suggestionHandler.removeCallbacks(suggestionUpdate);
            mCurrentWord.setLength(0);
            // Surrounding text is loaded asynchronously; LatinIME requests suggestions when ready.
        }
    }

    public void reloadTextCache() {
        reloadTextCache(null);
    }

    public void reloadTextCache(Runnable onReady) {
        mConnection.reloadTextCache(onReady);
        mRecapitalizeStatus.enable();
        mRecapitalizeStatus.stop();
    }

    public void refreshCursorSuggestions() {
        updateContextualSuggestions();
    }

    /**
     * React to a code input. It may be a code point to insert, or a symbolic value that influences
     * the keyboard behavior.
     *
     * Typically, this is called whenever a key is pressed on the software keyboard. This is not
     * the entry point for gesture input; see the onBatchInput* family of functions for this.
     *
     * @param settingsValues the current settings values.
     * @param event the event to handle.
     * @return the complete transaction object
     */
    public InputTransaction onCodeInput(final SettingsValues settingsValues, final Event event) {
        final InputTransaction inputTransaction = new InputTransaction(settingsValues);

        Event currentEvent = event;
        while (null != currentEvent) {
            if (currentEvent.isConsumed()) {
                handleConsumedEvent(currentEvent);
            } else if (currentEvent.isFunctionalKeyEvent()) {
                handleFunctionalEvent(currentEvent, inputTransaction);
            } else {
                handleNonFunctionalEvent(currentEvent, inputTransaction);
            }
            currentEvent = currentEvent.mNextEvent;
        }
        return inputTransaction;
    }

    /**
     * Handle a consumed event.
     *
     * Consumed events represent events that have already been consumed, typically by the
     * combining chain.
     *
     * @param event The event to handle.
     */
    private void handleConsumedEvent(final Event event) {
        // A consumed event may have text to commit and an update to the composing state, so
        // we evaluate both. With some combiners, it's possible than an event contains both
        // and we enter both of the following if clauses.
        final CharSequence textToCommit = event.getTextToCommit();
        if (!TextUtils.isEmpty(textToCommit)) {
            mConnection.commitText(textToCommit, 1);
        }
    }

    /**
     * Handle a functional key event.
     *
     * A functional event is a special key, like delete, shift, emoji, or the settings key.
     * Non-special keys are those that generate a single code point.
     * This includes all letters, digits, punctuation, separators, emoji. It excludes keys that
     * manage keyboard-related stuff like shift, language switch, settings, layout switch, or
     * any key that results in multiple code points like the ".com" key.
     *
     * @param event The event to handle.
     * @param inputTransaction The transaction in progress.
     */
    private void handleFunctionalEvent(final Event event, final InputTransaction inputTransaction) {
        switch (event.mKeyCode) {
            case Constants.CODE_DELETE:
                handleBackspaceEvent(event, inputTransaction);
                // Backspace is a functional key, but it affects the contents of the editor.
                break;
            case Constants.CODE_SHIFT:
                performRecapitalization();
                inputTransaction.requireShiftUpdate(InputTransaction.SHIFT_UPDATE_NOW);
                break;
            case Constants.CODE_CAPSLOCK:
                // Note: Changing keyboard to shift lock state is handled in
                // {@link KeyboardSwitcher#onEvent(Event)}.
                break;
            case Constants.CODE_SYMBOL_SHIFT:
                // Note: Calling back to the keyboard on the symbol Shift key is handled in
                // {@link #onPressKey(int,int,boolean)} and {@link #onReleaseKey(int,boolean)}.
                break;
            case Constants.CODE_SWITCH_ALPHA_SYMBOL:
                // Note: Calling back to the keyboard on symbol key is handled in
                // {@link #onPressKey(int,int,boolean)} and {@link #onReleaseKey(int,boolean)}.
                break;
            case Constants.CODE_SETTINGS:
                onSettingsKeyPressed();
                break;
            case Constants.CODE_ACTION_NEXT:
                performEditorAction(EditorInfo.IME_ACTION_NEXT);
                break;
            case Constants.CODE_ACTION_PREVIOUS:
                performEditorAction(EditorInfo.IME_ACTION_PREVIOUS);
                break;
            case Constants.CODE_LANGUAGE_SWITCH:
                handleLanguageSwitchKey();
                break;
            case Constants.CODE_SHIFT_ENTER:
                sendDownUpKeyEvent(KeyEvent.KEYCODE_ENTER, KeyEvent.META_SHIFT_ON);
                // Shift + Enter is not supported in all devices
                break;
            default:
                throw new RuntimeException("Unknown key code : " + event.mKeyCode);
        }
    }

    /**
     * Handle an event that is not a functional event.
     *
     * These events are generally events that cause input, but in some cases they may do other
     * things like trigger an editor action.
     *
     * @param event The event to handle.
     * @param inputTransaction The transaction in progress.
     */
    private void handleNonFunctionalEvent(final Event event,
            final InputTransaction inputTransaction) {
        switch (event.mCodePoint) {
            case Constants.CODE_ENTER:
                final EditorInfo editorInfo = getCurrentInputEditorInfo();
                final int imeOptionsActionId =
                        InputTypeUtils.getImeOptionsActionIdFromEditorInfo(editorInfo);
                if (InputTypeUtils.IME_ACTION_CUSTOM_LABEL == imeOptionsActionId) {
                    // Either we have an actionLabel and we should performEditorAction with
                    // actionId regardless of its value.
                    performEditorAction(editorInfo.actionId);
                } else if (EditorInfo.IME_ACTION_NONE != imeOptionsActionId) {
                    // We didn't have an actionLabel, but we had another action to execute.
                    // EditorInfo.IME_ACTION_NONE explicitly means no action. In contrast,
                    // EditorInfo.IME_ACTION_UNSPECIFIED is the default value for an action, so it
                    // means there should be an action and the app didn't bother to set a specific
                    // code for it - presumably it only handles one. It does not have to be treated
                    // in any specific way: anything that is not IME_ACTION_NONE should be sent to
                    // performEditorAction.
                    performEditorAction(imeOptionsActionId);
                } else {
                    // No action label, and the action from imeOptions is NONE: this is a regular
                    // enter key that should input a carriage return.
                    
                    // Learn from the current sentence before handling enter
                    learnFromCurrentSentence();
                    
                    handleNonSpecialCharacterEvent(event, inputTransaction);
                }
                break;
            default:
                handleNonSpecialCharacterEvent(event, inputTransaction);
                break;
        }
    }

    /**
     * Handle inputting a code point to the editor.
     *
     * Non-special keys are those that generate a single code point.
     * This includes all letters, digits, punctuation, separators, emoji. It excludes keys that
     * manage keyboard-related stuff like shift, language switch, settings, layout switch, or
     * any key that results in multiple code points like the ".com" key.
     *
     * @param event The event to handle.
     * @param inputTransaction The transaction in progress.
     */
    private void handleNonSpecialCharacterEvent(final Event event,
            final InputTransaction inputTransaction) {
        final int codePoint = event.mCodePoint;
        if (inputTransaction.mSettingsValues.isWordSeparator(codePoint)
                || Character.getType(codePoint) == Character.OTHER_SYMBOL) {
            handleSeparatorEvent(event, inputTransaction);
        } else {
            handleNonSeparatorEvent(event);
        }
    }

    /**
     * Handle a non-separator.
     * @param event The event to handle.
     */
    private void handleNonSeparatorEvent(final Event event) {
        final char character = (char) event.mCodePoint;
        
        // Commit first so suggestions read a consistent editor state.
        sendKeyCodePoint(event.mCodePoint);
        if (Character.isLetterOrDigit(event.mCodePoint) || character == '\'' || character == '-') {
            mCurrentWord.appendCodePoint(event.mCodePoint);
            updateSuggestions();
        }
    }

    /**
     * Handle input of a separator code point.
     * @param event The event to handle.
     * @param inputTransaction The transaction in progress.
     */
    private void handleSeparatorEvent(final Event event, final InputTransaction inputTransaction) {
        // Learn from completed word before handling separator
        if (mCurrentWord.length() > 0 && shouldLearn()) {
            String completedWord = mCurrentWord.toString();
            String previousContext = getPreviousContext();
            learnOnWorker(engine -> {
                engine.learnWord(completedWord);
                if (!TextUtils.isEmpty(previousContext)) {
                    engine.learnFromInput(previousContext + " " + completedWord);
                }
            });
            
            mCurrentWord.setLength(0); // Clear current word
        }
        
        // Check if this separator indicates sentence completion
        int codePoint = event.mCodePoint;
        if (codePoint == '.' || codePoint == '!' || codePoint == '?') {
            learnFromCurrentSentence(); // Learn from the completed sentence
        }
        
        mCurrentWord.setLength(0);
        sendKeyCodePoint(event.mCodePoint);
        
        // Update suggestions after separator
        updateSuggestions();

        inputTransaction.requireShiftUpdate(InputTransaction.SHIFT_UPDATE_NOW);
    }

    /**
     * Handle a press on the backspace key.
     * @param event The event to handle.
     * @param inputTransaction The transaction in progress.
     */
    private void handleBackspaceEvent(final Event event, final InputTransaction inputTransaction) {
        // Update current word tracking
        if (mCurrentWord.length() > 0) {
            int start = Character.offsetByCodePoints(mCurrentWord, mCurrentWord.length(), -1);
            mCurrentWord.delete(start, mCurrentWord.length());
            updateSuggestions();
        }
        
        // In many cases after backspace, we need to update the shift state. Normally we need
        // to do this right away to avoid the shift state being out of date in case the user types
        // backspace then some other character very fast. However, in the case of backspace key
        // repeat, this can lead to flashiness when the cursor flies over positions where the
        // shift state should be updated, so if this is a key repeat, we update after a small delay.
        // Then again, even in the case of a key repeat, if the cursor is at start of text, it
        // can't go any further back, so we can update right away even if it's a key repeat.
        final int shiftUpdateKind =
                event.isKeyRepeat() && mConnection.getExpectedSelectionStart() > 0
                ? InputTransaction.SHIFT_UPDATE_LATER : InputTransaction.SHIFT_UPDATE_NOW;
        inputTransaction.requireShiftUpdate(shiftUpdateKind);

        if (mConnection.hasSelection()) {
            mConnection.deleteSelectedText();
        } else {
            final int codePointBeforeCursor = mConnection.getCodePointBeforeCursor();
            if (codePointBeforeCursor == Constants.NOT_A_CODE) {
                sendDownUpKeyEvent(KeyEvent.KEYCODE_DEL);
            } else {
                int numChars = Character.isSupplementaryCodePoint(codePointBeforeCursor) ? 2 : 1;
                if (android.os.Build.VERSION.SDK_INT >= 24) {
                    String before = mConnection.getTextBeforeCursor();
                    if (before != null && !before.isEmpty()) {
                        android.icu.text.BreakIterator boundaries =
                                android.icu.text.BreakIterator.getCharacterInstance();
                        boundaries.setText(before);
                        int start = boundaries.preceding(before.length());
                        if (start != android.icu.text.BreakIterator.DONE) {
                            numChars = before.length() - start;
                        }
                    }
                }
                mConnection.deleteTextBeforeCursor(numChars);
            }
        }
    }

    /**
     * Handle a press on the language switch key (the "globe key")
     */
    private void handleLanguageSwitchKey() {
        mLatinIME.switchToNextSubtype();
    }

    /**
     * Performs a recapitalization event.
     */
    private void performRecapitalization() {
        if (!mConnection.hasSelection() || !mRecapitalizeStatus.mIsEnabled()) {
            return; // No selection or recapitalize is disabled for now
        }
        final int selectionStart = mConnection.getExpectedSelectionStart();
        final int selectionEnd = mConnection.getExpectedSelectionEnd();
        final int numCharsSelected = selectionEnd - selectionStart;
        if (numCharsSelected > Constants.MAX_CHARACTERS_FOR_RECAPITALIZATION) {
            // We bail out if we have too many characters for performance reasons. We don't want
            // to suck possibly multiple-megabyte data.
            return;
        }
        // If we have a recapitalize in progress, use it; otherwise, start a new one.
        if (!mRecapitalizeStatus.isStarted()
                || !mRecapitalizeStatus.isSetAt(selectionStart, selectionEnd)) {
            final CharSequence selectedText = mConnection.getSelectedText();
            if (TextUtils.isEmpty(selectedText)) return; // Race condition with the input connection
            mRecapitalizeStatus.start(selectionStart, selectionEnd, selectedText.toString(), mLatinIME.getCurrentLayoutLocale());
            // We trim leading and trailing whitespace.
            mRecapitalizeStatus.trim();
        }
        mConnection.beginBatchEdit();
        mConnection.setSelection(selectionStart, selectionStart);
        mRecapitalizeStatus.rotate();
        mConnection.replaceText(selectionStart, selectionEnd, mRecapitalizeStatus.getRecapitalizedString());
        mConnection.setSelection(mRecapitalizeStatus.getNewCursorStart(), mRecapitalizeStatus.getNewCursorEnd());
        mConnection.endBatchEdit();
    }

    /**
     * Gets the current auto-caps state, factoring in the space state.
     *
     * This method tries its best to do this in the most efficient possible manner. It avoids
     * getting text from the editor if possible at all.
     * This is called from the KeyboardSwitcher (through a trampoline in LatinIME) because it
     * needs to know auto caps state to display the right layout.
     *
     * @param settingsValues the relevant settings values
     * @param layoutSetName the name of the current keyboard layout set
     * @return a caps mode from TextUtils.CAP_MODE_* or Constants.TextUtils.CAP_MODE_OFF.
     */
    public int getCurrentAutoCapsState(final SettingsValues settingsValues,
                                       final String layoutSetName) {
        if (!settingsValues.mAutoCap || !layoutUsesAutoCaps(layoutSetName)) {
            return Constants.TextUtils.CAP_MODE_OFF;
        }

        final EditorInfo ei = getCurrentInputEditorInfo();
        if (ei == null) return Constants.TextUtils.CAP_MODE_OFF;
        final int inputType = ei.inputType;
        // Warning: this depends on mSpaceState, which may not be the most current value. If
        // mSpaceState gets updated later, whoever called this may need to be told about it.
        return mConnection.getCursorCapsMode(inputType, settingsValues.mSpacingAndPunctuations);
    }

    private boolean layoutUsesAutoCaps(final String layoutSetName) {
        switch (layoutSetName) {
            case SubtypeLocaleUtils.LAYOUT_ARABIC:
            case SubtypeLocaleUtils.LAYOUT_BENGALI:
            case SubtypeLocaleUtils.LAYOUT_BENGALI_AKKHOR:
            case SubtypeLocaleUtils.LAYOUT_BENGALI_UNIJOY:
            case SubtypeLocaleUtils.LAYOUT_FARSI:
            case SubtypeLocaleUtils.LAYOUT_GEORGIAN:
            case SubtypeLocaleUtils.LAYOUT_HEBREW:
            case SubtypeLocaleUtils.LAYOUT_HINDI:
            case SubtypeLocaleUtils.LAYOUT_HINDI_COMPACT:
            case SubtypeLocaleUtils.LAYOUT_KANNADA:
            case SubtypeLocaleUtils.LAYOUT_KHMER:
            case SubtypeLocaleUtils.LAYOUT_LAO:
            case SubtypeLocaleUtils.LAYOUT_MALAYALAM:
            case SubtypeLocaleUtils.LAYOUT_MARATHI:
            case SubtypeLocaleUtils.LAYOUT_NEPALI_ROMANIZED:
            case SubtypeLocaleUtils.LAYOUT_NEPALI_TRADITIONAL:
            case SubtypeLocaleUtils.LAYOUT_TAMIL:
            case SubtypeLocaleUtils.LAYOUT_TELUGU:
            case SubtypeLocaleUtils.LAYOUT_THAI:
            case SubtypeLocaleUtils.LAYOUT_URDU:
                return false;
            default:
                return true;
        }
    }

    public int getCurrentRecapitalizeState() {
        if (!mRecapitalizeStatus.isStarted()
                || !mRecapitalizeStatus.isSetAt(mConnection.getExpectedSelectionStart(),
                        mConnection.getExpectedSelectionEnd())) {
            // Not recapitalizing at the moment
            return RecapitalizeStatus.NOT_A_RECAPITALIZE_MODE;
        }
        return mRecapitalizeStatus.getCurrentMode();
    }

    /**
     * @return the editor info for the current editor
     */
    private EditorInfo getCurrentInputEditorInfo() {
        return mLatinIME.getCurrentInputEditorInfo();
    }

    /**
     * @param actionId the action to perform
     */
    private void performEditorAction(final int actionId) {
        mConnection.performEditorAction(actionId);
    }

    /**
     * Perform the processing specific to inputting TLDs.
     *
     * Some keys input a TLD (specifically, the ".com" key) and this warrants some specific
     * processing. First, if this is a TLD, we ignore PHANTOM spaces -- this is done by type
     * of character in onCodeInput, but since this gets inputted as a whole string we need to
     * do it here specifically. Then, if the last character before the cursor is a period, then
     * we cut the dot at the start of ".com". This is because humans tend to type "www.google."
     * and then press the ".com" key and instinctively don't expect to get "www.google..com".
     *
     * @param text the raw text supplied to onTextInput
     * @return the text to actually send to the editor
     */
    private String performSpecificTldProcessingOnTextInput(final String text) {
        if (text.length() <= 1 || text.charAt(0) != Constants.CODE_PERIOD
                || !Character.isLetter(text.charAt(1))) {
            // Not a tld: do nothing.
            return text;
        }
        final int codePointBeforeCursor = mConnection.getCodePointBeforeCursor();
        // If no code point, #getCodePointBeforeCursor returns NOT_A_CODE_POINT.
        if (Constants.CODE_PERIOD == codePointBeforeCursor) {
            return text.substring(1);
        }
        return text;
    }

    /**
     * Handle a press on the settings key.
     */
    private void onSettingsKeyPressed() {
        mLatinIME.launchSettings();
    }

    /**
     * Sends a DOWN key event followed by an UP key event to the editor.
     *
     * If possible at all, avoid using this method. It causes all sorts of race conditions with
     * the text view because it goes through a different, asynchronous binder. Also, batch edits
     * are ignored for key events. Use the normal software input methods instead.
     *
     * @param keyCode the key code to send inside the key event.
     */
    public void sendDownUpKeyEvent(final int keyCode) {
        sendDownUpKeyEvent(keyCode, 0);
    }

    public void sendDownUpKeyEvent(final int keyCode, final int metaState) {
        final long eventTime = SystemClock.uptimeMillis();
        mConnection.sendKeyEvent(new KeyEvent(eventTime, eventTime,
                KeyEvent.ACTION_DOWN, keyCode, 0, metaState, KeyCharacterMap.VIRTUAL_KEYBOARD, 0,
                KeyEvent.FLAG_SOFT_KEYBOARD | KeyEvent.FLAG_KEEP_TOUCH_MODE));
        mConnection.sendKeyEvent(new KeyEvent(SystemClock.uptimeMillis(), eventTime,
                KeyEvent.ACTION_UP, keyCode, 0, metaState, KeyCharacterMap.VIRTUAL_KEYBOARD, 0,
                KeyEvent.FLAG_SOFT_KEYBOARD | KeyEvent.FLAG_KEEP_TOUCH_MODE));
    }

    /**
     * Sends a code point to the editor, using the most appropriate method.
     *
     * Normally we send code points with commitText, but there are some cases (where backward
     * compatibility is a concern for example) where we want to use deprecated methods.
     *
     * @param codePoint the code point to send.
     */
    // TODO: replace these two parameters with an InputTransaction
    private void sendKeyCodePoint(final int codePoint) {
        // TODO: Remove this special handling of digit letters.
        // For backward compatibility. See {@link InputMethodService#sendKeyChar(char)}.
        if (codePoint >= '0' && codePoint <= '9') {
            sendDownUpKeyEvent(codePoint - '0' + KeyEvent.KEYCODE_0);
            return;
        }

        mConnection.commitText(StringUtils.newSingleCodePointString(codePoint), 1);
    }
    
    /**
     * Find the word at the current cursor position.
     * Uses surrounding text to identify the complete word the cursor is currently on.
     * @return WordAtCursorInfo containing the word and its position, or null if no word found
     */
    private WordAtCursorInfo findWordAtCursor() {
        if (!mConnection.hasCursorPosition()) {
            return null;
        }
        
        // Get surrounding text
        String textBefore = mConnection.getTextBeforeCursor();
        String textAfter = mConnection.getTextAfterCursor();
        
        if (textBefore == null) textBefore = "";
        if (textAfter == null) textAfter = "";
        
        // Check if cursor is immediately after or before whitespace (between words)
        if (textBefore.length() > 0 && Character.isWhitespace(textBefore.charAt(textBefore.length() - 1))) {
            return null; // Cursor is after whitespace, between words
        }
        if (textAfter.length() > 0 && Character.isWhitespace(textAfter.charAt(0))) {
            // Check if there's a word character immediately before cursor
            if (textBefore.length() == 0 || !Character.isLetterOrDigit(textBefore.charAt(textBefore.length() - 1))) {
                return null; // Cursor is before whitespace and not touching a word
            }
        }
        
        // Find word boundaries
        int wordStart = textBefore.length();
        int wordEnd = 0;
        
        // Search backwards from cursor to find start of word
        for (int i = textBefore.length() - 1; i >= 0; i--) {
            char c = textBefore.charAt(i);
            if (Character.isWhitespace(c) || isPunctuation(c)) {
                wordStart = i + 1;
                break;
            }
            if (i == 0) {
                wordStart = 0;
            }
        }
        
        // Search forwards from cursor to find end of word
        for (int i = 0; i < textAfter.length(); i++) {
            char c = textAfter.charAt(i);
            if (Character.isWhitespace(c) || isPunctuation(c)) {
                wordEnd = i;
                break;
            }
            if (i == textAfter.length() - 1) {
                wordEnd = textAfter.length();
            }
        }
        
        // Extract the complete word
        String wordBeforeCursor = wordStart < textBefore.length() ? textBefore.substring(wordStart) : "";
        String wordAfterCursor = wordEnd > 0 ? textAfter.substring(0, wordEnd) : "";
        String completeWord = wordBeforeCursor + wordAfterCursor;
        
        // Return null if no valid word found
        if (completeWord.trim().isEmpty() || !completeWord.matches(".*\\w.*")) {
            return null;
        }
        
        return new WordAtCursorInfo(completeWord.trim(), wordStart, wordEnd + textBefore.length());
    }
    
    /**
     * Helper method to check if a character is punctuation
     */
    private boolean isPunctuation(char c) {
        return !Character.isLetterOrDigit(c) && !Character.isWhitespace(c);
    }
    
    /**
     * Updates suggestions based on cursor position context.
     * Provides contextual corrections/completions for word at cursor,
     * or falls back to next-word predictions.
     */
    private void updateContextualSuggestions() {
        suggestionGeneration++;
        suggestionHandler.removeCallbacks(suggestionUpdate);
        if (isEmailSuggestionField()) {
            updateSuggestions();
            return;
        }
        // Respect the editor's request before reading surrounding text or the clipboard.
        if (isPrivateField()) {
            mLatinIME.updateSuggestionStrip(java.util.Collections.emptyList());
            return;
        }
        // First check if cursor is positioned on a word
        WordAtCursorInfo wordAtCursor = findWordAtCursor();
        
        if (wordAtCursor != null) {
            final int generation = suggestionGeneration;
            final EditorInfo editor = getCurrentInputEditorInfo();
            final String word = wordAtCursor.word;
            learningWorker.execute(() -> {
                if (learningClosed || generation != suggestionGeneration) return;
                LocalLearningEngine engine = getLearningEngine();
                List<String> suggestions = engine == null ? java.util.Collections.emptyList()
                        : engine.getCorrectionsAndCompletions(word);
                deliverSuggestions(generation, editor, suggestions);
            });
        } else {
            // Cursor is not on a word (e.g., on space) - fall back to regular next-word suggestions
            updateSuggestions();
        }
    }
    
    /**
     * Simple data class to hold word-at-cursor information
     */
    private static class WordAtCursorInfo {
        public final String word;
        public final int startOffset;
        public final int endOffset;
        
        public WordAtCursorInfo(String word, int startOffset, int endOffset) {
            this.word = word;
            this.startOffset = startOffset;
            this.endOffset = endOffset;
        }
    }

    /**
     * Updates suggestions based on current input context.
     */
    private void updateSuggestions() {
        suggestionHandler.removeCallbacks(suggestionUpdate);
        suggestionGeneration++;
        if (isPrivateField() && !isEmailSuggestionField()) {
            mLatinIME.updateSuggestionStrip(java.util.Collections.emptyList());
            return;
        }
        suggestionHandler.postDelayed(suggestionUpdate, 24);
    }

    private void computeSuggestions() {
        final int generation = suggestionGeneration;
        final EditorInfo editor = getCurrentInputEditorInfo();
        if (isEmailSuggestionField()) {
            String text = mConnection.getTextBeforeCursor();
            boolean includeAccounts = rkr.simplekeyboard.inputmethod.compat.PreferenceManagerCompat
                    .getDeviceSharedPreferences(mLatinIME)
                    .getBoolean(rkr.simplekeyboard.inputmethod.latin.settings.Settings
                            .PREF_ACCOUNT_EMAIL_SUGGESTIONS, false);
            learningWorker.execute(() -> {
                if (learningClosed || generation != suggestionGeneration) return;
                deliverSuggestions(generation, editor, getEmailSuggestions(text, includeAccounts));
            });
            return;
        }
        if (isPrivateField()) {
            mLatinIME.updateSuggestionStrip(java.util.Collections.emptyList());
            return;
        }
        final String currentWord = mCurrentWord.toString();
        final String previousContext = getPreviousContext();
        
        // Fall back to regular learning-based suggestions
        learningWorker.execute(() -> {
            if (learningClosed || generation != suggestionGeneration) return;
            LocalLearningEngine engine = getLearningEngine();
            List<String> suggestions = engine == null ? java.util.Collections.emptyList()
                    : engine.getSuggestions(currentWord, previousContext);
            deliverSuggestions(generation, editor, suggestions);
        });
    }

    private void deliverSuggestions(int generation, EditorInfo editor, List<String> suggestions) {
        suggestionHandler.post(() -> {
            if (!learningClosed && generation == suggestionGeneration
                    && editor == getCurrentInputEditorInfo()
                    && (!isPrivateField() || isEmailSuggestionField())) {
                mLatinIME.updateSuggestionStrip(suggestions);
            }
        });
    }

    private interface LearningOperation {
        void apply(LocalLearningEngine engine);
    }

    private void learnOnWorker(LearningOperation operation) {
        if (!shouldLearn() || learningClosed) return;
        learningWorker.execute(() -> {
            LocalLearningEngine engine = getLearningEngine();
            if (engine != null) operation.apply(engine);
        });
    }

    public void closeLearningWorker() {
        learningClosed = true;
        suggestionGeneration++;
        suggestionHandler.removeCallbacks(suggestionUpdate);
        learningWorker.shutdown();
    }

    /**
     * Checks if email suggestions are enabled in settings.
     */
    private boolean isEmailSuggestionsEnabled() {
        try {
            return mLatinIME.getSettingsValues().mEmailSuggestionsEnabled;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Checks if the current input field is an email field.
     */
    private boolean isEmailInputField(EditorInfo editorInfo) {
        if (editorInfo == null) return false;
        int inputType = editorInfo.inputType;
        int variation = inputType & android.text.InputType.TYPE_MASK_VARIATION;
        return InputTypeUtils.isEmailVariation(variation);
    }

    private boolean isEmailSuggestionField() {
        EditorInfo info = getCurrentInputEditorInfo();
        return info != null && isEmailInputField(info) && isEmailSuggestionsEnabled()
                && (info.inputType & android.text.InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS) == 0
                && (info.imeOptions & EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING) == 0;
    }

    /**
     * Gets email-specific suggestions based on current input.
     */
    private java.util.List<String> getEmailSuggestions(String textBeforeCursor,
            boolean includeAccounts) {
        java.util.List<String> suggestions = new java.util.ArrayList<>();
        EmailSuggestionProvider emailProvider = getEmailSuggestionProvider();
        
        try {
            if (textBeforeCursor != null) {
                // Check for domain completion pattern: (text)@
                int atIndex = textBeforeCursor.lastIndexOf('@');
                if (atIndex >= 0) {
                    // We found @ - check if we should suggest domains
                    String textAfterAt = textBeforeCursor.substring(atIndex + 1);
                    // Only suggest domains if there's no space after @ (immediate domain completion)
                    if (!textAfterAt.contains(" ") && !textAfterAt.contains("\n")) {
                        String beforeAt = textBeforeCursor.substring(0, atIndex);
                        // Find the start of the email address (last space or beginning)
                        int emailStart = Math.max(beforeAt.lastIndexOf(' '), beforeAt.lastIndexOf('\n')) + 1;
                        String emailPrefix = beforeAt.substring(emailStart);
                        
                        if (!emailPrefix.isEmpty() && isValidEmailPrefix(emailPrefix)) {
                            java.util.List<String> domainSuggestions = emailProvider
                                    .getDomainCompletions(emailPrefix, textAfterAt);
                            suggestions.addAll(domainSuggestions);
                        }
                    }
                } else {
                    // We're typing an email address from the beginning
                    if (includeAccounts) {
                        String prefix = textBeforeCursor.substring(Math.max(
                                textBeforeCursor.lastIndexOf(' '), textBeforeCursor.lastIndexOf('\n')) + 1);
                        suggestions.addAll(emailProvider.getFilteredContactEmails(prefix));
                    }
                }
            }
        } catch (Exception e) {
            // Return empty suggestions on error
        }
        
        return suggestions;
    }

    /**
     * Validates that the text before @ is a valid email prefix.
     */
    private boolean isValidEmailPrefix(String prefix) {
        if (prefix == null || prefix.trim().isEmpty()) return false;
        // Basic validation: no spaces, no special chars except dots, underscores, hyphens
        return prefix.matches("[a-zA-Z0-9._-]+");
    }
    
    /**
     * Gets the previous context for contextual suggestions.
     */
    private String getPreviousContext() {
        try {
            // Get the text before cursor for context
            String textBeforeCursor = mConnection.getTextBeforeCursor();
            if (textBeforeCursor != null && !textBeforeCursor.isEmpty()) {
                String context = textBeforeCursor;
                
                // Remove the current word from context if it's at the end
                if (mCurrentWord.length() > 0) {
                    String currentWord = mCurrentWord.toString();
                    if (context.endsWith(currentWord)) {
                        context = context.substring(0, context.length() - currentWord.length()).trim();
                    }
                }
                
                // Clean the context: get the last few words for better relevance
                context = cleanContextForPrediction(context);
                
                // Limit context to last 100 characters for performance but keep word boundaries
                if (context.length() > 100) {
                    int lastSpaceIndex = context.lastIndexOf(' ', 100);
                    if (lastSpaceIndex > 50) {
                        context = context.substring(lastSpaceIndex + 1);
                    } else {
                        context = context.substring(context.length() - 100);
                    }
                }
                return context;
            }
        } catch (Exception e) {
            // Log error but continue with empty context to prevent crash
            android.util.Log.w("InputLogic", "Error extracting context: " + e.getMessage());
        }
        return "";
    }

    /**
     * Cleans and optimizes context for better n-gram predictions.
     */
    private String cleanContextForPrediction(String context) {
        if (TextUtils.isEmpty(context)) return "";
        
        // Remove extra whitespace and normalize
        context = context.trim().replaceAll("\\s+", " ");
        
        // For n-gram predictions, we want the last 2-3 words for trigram context
        String[] words = context.split("\\s+");
        if (words.length > 3) {
            // Take the last 3 words for optimal trigram prediction
            StringBuilder sb = new StringBuilder();
            for (int i = Math.max(0, words.length - 3); i < words.length; i++) {
                if (sb.length() > 0) sb.append(" ");
                sb.append(words[i]);
            }
            return sb.toString();
        }
        
        return context;
    }
    
    /**
     * Handles suggestion selection from the suggestion strip.
     */
    public void onSuggestionSelected(String suggestion) {
        if (isEmailSuggestionField() && suggestion != null && suggestion.contains("@")) {
            String before = mConnection.getTextBeforeCursor();
            if (before != null) {
                String token = before.substring(Math.max(before.lastIndexOf(' '),
                        before.lastIndexOf('\n')) + 1);
                if (suggestion.regionMatches(true, 0, token, 0, token.length())
                        && !suggestion.equalsIgnoreCase(token)) {
                    mConnection.deleteTextBeforeCursor(token.length());
                    mConnection.commitText(suggestion, 1);
                    mCurrentWord.setLength(0);
                    updateSuggestions();
                }
            }
            return;
        }
        // Check if this is a special suggestion type (clipboard, calculator, emoji)
        boolean isSpecialSuggestion = isSpecialSuggestion(suggestion);
        
        // For clipboard suggestions, get the full clipboard content instead of the truncated display text
        String actualText;
        if (suggestion != null && suggestion.startsWith("📋 ")) {
            // Get full clipboard content for pasting (not the truncated preview)
            actualText = getFullClipboardText();
            if (actualText == null) {
                // Fallback to stripped suggestion if clipboard is no longer available
                actualText = stripSuggestionPrefix(suggestion);
            }
        } else {
            // Strip special prefixes from suggestions (calculator, etc.)
            actualText = stripSuggestionPrefix(suggestion);
        }
        
        // First check if we're replacing a word at cursor position
        WordAtCursorInfo wordAtCursor = findWordAtCursor();
        
        if (wordAtCursor != null) {
            // Replace word at cursor position
            replaceWordAtCursor(wordAtCursor, actualText);
        } else if (mCurrentWord.length() > 0) {
            // Replace current word with suggestion (existing behavior)
            mConnection.deleteTextBeforeCursor(mCurrentWord.length());
            mConnection.commitText(actualText, 1);
            
            // Learn from the selected suggestion (only if not special)
            if (!isSpecialSuggestion) {
                learnCompletedWord(actualText);
            }
            
            mCurrentWord.setLength(0);
        } else {
            // Just insert the suggestion
            mConnection.commitText(actualText, 1);
            
            // Learn from the selected suggestion (only if not special)
            if (!isSpecialSuggestion) {
                final String selectedWord = actualText;
                learnOnWorker(engine -> engine.learnWord(selectedWord));
            }
        }
        
        // Add space after suggestion only if it's a regular word (not special suggestions)
        // Special suggestions (emoji, calculator, clipboard) should not auto-add space
        if (!isSpecialSuggestion && shouldAddSpaceAfter(actualText)) {
            mConnection.commitText(" ", 1);
        }
        
        // Update suggestions after selection
        updateContextualSuggestions();
    }
    
    /**
     * Gets the full clipboard text without any truncation.
     * This is used when pasting clipboard suggestions to ensure the complete content is inserted.
     */
    private String getFullClipboardText() {
        if (mLatinIME == null) {
            return null;
        }
        
        android.content.ClipboardManager clipboardManager = 
            (android.content.ClipboardManager) mLatinIME.getSystemService(android.content.Context.CLIPBOARD_SERVICE);
        
        if (clipboardManager == null || !clipboardManager.hasPrimaryClip()) {
            return null;
        }
        
        android.content.ClipData clipData = clipboardManager.getPrimaryClip();
        if (clipData == null || clipData.getItemCount() == 0) {
            return null;
        }
        
        android.content.ClipData.Item item = clipData.getItemAt(0);
        CharSequence text = item.getText();
        
        if (TextUtils.isEmpty(text)) {
            return null;
        }
        
        // Return the FULL text without any truncation
        return text.toString();
    }
    
    /**
     * Checks if a suggestion is a special type (clipboard, calculator, emoji).
     * Special suggestions should not trigger learning or auto-spacing.
     */
    private boolean isSpecialSuggestion(String suggestion) {
        if (suggestion == null) {
            return false;
        }
        
        // Check for clipboard prefix
        if (suggestion.startsWith("📋 ")) {
            return true;
        }
        
        // Check for calculator prefix
        if (suggestion.startsWith("= ")) {
            return true;
        }
        
        // Check if it's an emoji
        if (rkr.simplekeyboard.inputmethod.latin.utils.EmojiUtils.isEmoji(suggestion)) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Determines if a space should be added after the inserted text.
     * Only adds space after regular words, not after numbers, URLs, or punctuation.
     */
    private boolean shouldAddSpaceAfter(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        
        // Don't add space after emojis
        if (rkr.simplekeyboard.inputmethod.latin.utils.EmojiUtils.containsEmoji(text)) {
            return false;
        }
        
        // Don't add space after pure numbers (including decimals)
        if (text.matches("^[0-9]+(\\.[0-9]+)?$")) {
            return false;
        }
        
        // Don't add space after punctuation
        if (text.matches("^[.,!?;:]+$")) {
            return false;
        }
        
        // Don't add space if text ends with punctuation
        if (text.matches(".*[.,!?;:]$")) {
            return false;
        }
        
        // Add space after regular words (letters only or mixed with numbers)
        // But text should contain at least one letter
        return text.matches(".*[a-zA-Z\\u0600-\\u06FF]+.*");
    }
    
    /**
     * Strips special prefixes from suggestions (e.g., clipboard icon, calculator equals sign).
     * This ensures that when users select these suggestions, only the actual content is inserted.
     */
    private String stripSuggestionPrefix(String suggestion) {
        if (suggestion == null) {
            return "";
        }
        
        // Strip clipboard prefix "📋 "
        if (suggestion.startsWith("📋 ")) {
            return suggestion.substring(2).trim();
        }
        
        // Strip calculator prefix "= "
        if (suggestion.startsWith("= ")) {
            return suggestion.substring(2).trim();
        }
        
        // Return original if no prefix found
        return suggestion;
    }
    
    /**
     * Replaces a word at the cursor position with the selected suggestion.
     */
    private void replaceWordAtCursor(WordAtCursorInfo wordInfo, String replacement) {
        // Get current cursor position
        int cursorPos = mConnection.getExpectedSelectionStart();
        
        // Calculate word boundaries relative to current cursor position
        String textBefore = mConnection.getTextBeforeCursor();
        String textAfter = mConnection.getTextAfterCursor();
        
        if (textBefore == null) textBefore = "";
        if (textAfter == null) textAfter = "";
        
        // Find word start position (going backwards from cursor)
        int wordStartFromCursor = 0;
        for (int i = textBefore.length() - 1; i >= 0; i--) {
            char c = textBefore.charAt(i);
            if (Character.isWhitespace(c) || isPunctuation(c)) {
                wordStartFromCursor = textBefore.length() - i - 1;
                break;
            }
            if (i == 0) {
                wordStartFromCursor = textBefore.length();
            }
        }
        
        // Find word end position (going forwards from cursor)
        int wordEndFromCursor = 0;
        for (int i = 0; i < textAfter.length(); i++) {
            char c = textAfter.charAt(i);
            if (Character.isWhitespace(c) || isPunctuation(c)) {
                wordEndFromCursor = i;
                break;
            }
            if (i == textAfter.length() - 1) {
                wordEndFromCursor = textAfter.length();
            }
        }
        
        // Calculate absolute positions
        int wordStart = cursorPos - wordStartFromCursor;
        int wordEnd = cursorPos + wordEndFromCursor;
        
        // Select the word and replace it
        mConnection.setSelection(wordStart, wordEnd);
        mConnection.commitText(replacement, 1);
        
        // Learn from the replacement
        learnCompletedWord(replacement);
    }

    private void learnCompletedWord(String word) {
        if (!shouldLearn()) return;
        String previousContext = getPreviousContext();
        learnOnWorker(engine -> {
            engine.learnWord(word);
            if (!TextUtils.isEmpty(previousContext)) {
                engine.learnFromInput(previousContext + " " + word);
            }
        });
    }
    
    /**
     * Learns from the current sentence when user presses enter or finishes a sentence.
     */
    private void learnFromCurrentSentence() {
        if (!shouldLearn()) return;
        String textBeforeCursor = mConnection.getTextBeforeCursor();
        if (!TextUtils.isEmpty(textBeforeCursor)) {
            String[] sentences = textBeforeCursor.split("[.!?؟]");
            if (sentences.length > 0) {
                String currentSentence = sentences[sentences.length - 1].trim();
                if (!TextUtils.isEmpty(currentSentence) && currentSentence.split("\\s+").length > 1) {
                    learnOnWorker(engine -> engine.learnSentence(currentSentence));
                }
            }
        }
        
        // Clear current word after sentence completion
        mCurrentWord.setLength(0);
    }
    
    /**
     * Checks if the next character should be capitalized based on context.
     */
    public boolean shouldCapitalizeNext() {
        String textBeforeCursor = mConnection.getTextBeforeCursor();
        if (TextUtils.isEmpty(textBeforeCursor)) {
            return true; // Capitalize first character
        }
        
        String trimmed = textBeforeCursor.trim();
        if (trimmed.isEmpty()) {
            return true; // Capitalize after whitespace at beginning
        }
        
        // Capitalize after sentence ending punctuation
        char lastChar = trimmed.charAt(trimmed.length() - 1);
        return lastChar == '.' || lastChar == '!' || lastChar == '?' || lastChar == '؟';
    }
    
    /**
     * Commits text directly to the input connection.
     * Used for emoji insertion and clipboard content.
     */
    public void commitText(String text) {
        if (!TextUtils.isEmpty(text)) {
            mConnection.commitText(text, 1);
            
            // Learn from committed text if it's a word
            if (text.trim().matches("\\w+")) {
                final String word = text.trim();
                learnOnWorker(engine -> engine.learnWord(word));
            }
        }
    }
}
