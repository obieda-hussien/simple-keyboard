package rkr.simplekeyboard.inputmethod.latin;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.TextUtils;
import android.widget.Toast;

import java.util.ArrayList;

import rkr.simplekeyboard.inputmethod.R;

/**
 * Tiny no-history bridge activity used by the IME for system-owned pickers that return a result.
 * It never stores media or speech text; results are immediately sent back to the running IME.
 */
public final class KeyboardActionActivity extends Activity {
    public static final String EXTRA_MODE = "mode";
    public static final String EXTRA_LANGUAGE = "language";
    public static final String EXTRA_TEXT = "text";
    public static final String EXTRA_URI = "uri";
    public static final String EXTRA_MIME = "mime";
    public static final String EXTRA_EDITOR_GENERATION = "editor_generation";

    public static final String MODE_VOICE = "voice";
    public static final String MODE_IMAGE = "image";
    public static final String MODE_TRANSLATE = "translate";

    public static final String ACTION_VOICE_RESULT =
            "rkr.simplekeyboard.inputmethod.action.VOICE_RESULT";
    public static final String ACTION_IMAGE_RESULT =
            "rkr.simplekeyboard.inputmethod.action.IMAGE_RESULT";
    public static final String ACTION_TRANSLATE_RESULT =
            "rkr.simplekeyboard.inputmethod.action.TRANSLATE_RESULT";

    private static final int REQUEST_VOICE = 1001;
    private static final int REQUEST_IMAGE = 1002;
    private static final int REQUEST_TRANSLATE = 1003;
    private boolean launched;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            launched = savedInstanceState.getBoolean("launched", false);
        }
        if (!launched) launchRequestedAction();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("launched", launched);
        super.onSaveInstanceState(outState);
    }

    private void launchRequestedAction() {
        launched = true;
        final String mode = getIntent().getStringExtra(EXTRA_MODE);
        if (MODE_VOICE.equals(mode)) {
            launchVoice();
        } else if (MODE_IMAGE.equals(mode)) {
            launchImage();
        } else if (MODE_TRANSLATE.equals(mode)) {
            launchTranslate();
        } else {
            finish();
        }
    }

    private void launchVoice() {
        final Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false);
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
        final String language = getIntent().getStringExtra(EXTRA_LANGUAGE);
        if (!TextUtils.isEmpty(language)) {
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, language);
        }
        if (intent.resolveActivity(getPackageManager()) == null) {
            Toast.makeText(this, R.string.voice_input_unavailable, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        try {
            startActivityForResult(intent, REQUEST_VOICE);
        } catch (RuntimeException e) {
            Toast.makeText(this, R.string.voice_input_unavailable, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void launchImage() {
        final Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        try {
            startActivityForResult(intent, REQUEST_IMAGE);
        } catch (RuntimeException e) {
            Toast.makeText(this, R.string.image_picker_unavailable, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void launchTranslate() {
        final String source = getIntent().getStringExtra(EXTRA_TEXT);
        if (TextUtils.isEmpty(source) || android.os.Build.VERSION.SDK_INT < 23) {
            Toast.makeText(this, R.string.translate_unavailable, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        final Intent process = new Intent(Intent.ACTION_PROCESS_TEXT);
        process.setType("text/plain");
        process.putExtra(Intent.EXTRA_PROCESS_TEXT, source);
        process.putExtra(Intent.EXTRA_PROCESS_TEXT_READONLY, false);
        if (process.resolveActivity(getPackageManager()) == null) {
            Toast.makeText(this, R.string.translate_unavailable, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        try {
            startActivityForResult(
                    Intent.createChooser(process, getString(R.string.translate_action)),
                    REQUEST_TRANSLATE);
        } catch (RuntimeException e) {
            Toast.makeText(this, R.string.translate_unavailable, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == REQUEST_VOICE) {
                final ArrayList<String> results =
                        data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                if (results != null) {
                    for (String result : results) {
                        if (!TextUtils.isEmpty(result)) {
                            sendResult(ACTION_VOICE_RESULT, EXTRA_TEXT, result, null, null);
                            break;
                        }
                    }
                }
            } else if (requestCode == REQUEST_TRANSLATE) {
                final CharSequence processed = data.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT);
                if (!TextUtils.isEmpty(processed)) {
                    sendResult(ACTION_TRANSLATE_RESULT, EXTRA_TEXT,
                            processed.toString(), null, null);
                }
            } else if (requestCode == REQUEST_IMAGE) {
                final Uri uri = data.getData();
                if (uri != null) {
                    final int takeFlags = data.getFlags() & Intent.FLAG_GRANT_READ_URI_PERMISSION;
                    if (takeFlags != 0) {
                        try {
                            getContentResolver().takePersistableUriPermission(uri, takeFlags);
                        } catch (SecurityException ignored) {
                            // Some providers grant only temporary access; that is enough for commitContent.
                        }
                    }
                    String mime = getContentResolver().getType(uri);
                    if (TextUtils.isEmpty(mime)) mime = "image/*";
                    sendResult(ACTION_IMAGE_RESULT, EXTRA_URI, uri.toString(), EXTRA_MIME, mime);
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
        finish();
    }

    private void sendResult(String action, String key, String value,
            String secondKey, String secondValue) {
        final Intent result = new Intent(action).setPackage(getPackageName());
        result.putExtra(EXTRA_EDITOR_GENERATION,
                getIntent().getIntExtra(EXTRA_EDITOR_GENERATION, -1));
        result.putExtra(key, value);
        if (secondKey != null) result.putExtra(secondKey, secondValue);
        sendBroadcast(result);
    }
}
