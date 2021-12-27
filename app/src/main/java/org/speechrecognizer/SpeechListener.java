package org.speechrecognizer;

import android.util.Log;
import androidx.annotation.NonNull;
import org.json.JSONException;
import org.json.JSONObject;
import org.vosk.android.RecognitionListener;

/**
 * A listener of user's speech.
 */
public class SpeechListener implements RecognitionListener {

  private final IActivityUpdater activityUpdater;

  private boolean isInRecognizingMode;

  /**
   * Creates listener instance.
   *
   * @param activityUpdater the activity updater, cannot be {@code null}
   */
  public SpeechListener(@NonNull IActivityUpdater activityUpdater) {
    this.activityUpdater = activityUpdater;
  }

  @Override
  public void onPartialResult(String hypothesis) {
    // nothing to do
  }

  @Override
  public void onResult(String hypothesis) {
    String word = getRecognizedWord(hypothesis);
    if (word == null) {
      return;
    }

    if (isInRecognizingMode) {
      activityUpdater.addRecognizedWord(word);
    } else if (word.equals(R.string.begin_recognize_word)) {
      isInRecognizingMode = true;
      activityUpdater.onBeginRecognizing();
    }
    // else do nothing, not our control word
  }

  @Override
  public void onFinalResult(String hypothesis) {
    stopRecognizing();
  }

  @Override
  public void onError(Exception exception) {
    activityUpdater.showError("Ошибка при распознавании:" + exception.getMessage());
  }

  @Override
  public void onTimeout() {
    stopRecognizing();
  }

  private void stopRecognizing() {
    isInRecognizingMode = false;
    activityUpdater.onEndRecognizing();
  }

  /*
   * Returns recognized word or {@code null} if nothing recognized.
   */
  private String getRecognizedWord(String hypothesis) {
    try {
      JSONObject jObject = new JSONObject(hypothesis);
      String text = jObject.getString("text");
      if (text != null && !text.isEmpty()) {
        return text.trim();
      }
    } catch (JSONException e) {
      // Shouldn't be here in normal work. If happened, log error and continue to work.
      Log.e(SpeechListener.class.getName(), "Error parsing Vosk's JSON", e);
    }
    return null;
  }
}
