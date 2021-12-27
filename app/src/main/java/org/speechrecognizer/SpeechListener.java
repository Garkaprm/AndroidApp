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
  private final String beginRecognizingWord;

  private boolean isInRecognizingMode;

  /**
   * Creates listener instance.
   *
   * @param activityUpdater      the activity updater, cannot be {@code null}
   * @param beginRecognizingWord the word indicating start recognizing mode, cannot be {@code null}
   *                             or empty
   */
  public SpeechListener(@NonNull IActivityUpdater activityUpdater,
      @NonNull String beginRecognizingWord) {
    if (beginRecognizingWord.isEmpty()) {
      throw new IllegalArgumentException("Bad argument given: beginRecognizingWord");
    }
    this.activityUpdater = activityUpdater;
    this.beginRecognizingWord = beginRecognizingWord;
  }

  @Override
  public void onPartialResult(String hypothesis) {
    // nothing to do
  }

  @Override
  public void onResult(String hypothesis) {
    String word = getRecognizedWord(hypothesis);

    if (word == null) {
      if (isInRecognizingMode) {
        // don't have new words from user, stop recognizing and return to initial state
        stopRecognizing();
      }
      return;
    }

    if (isInRecognizingMode) {
      activityUpdater.addRecognizedWord(word);
    } else if (beginRecognizingWord.equalsIgnoreCase(word)) {
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
    Log.e(SpeechListener.class.getName(), exception.getMessage(), exception);
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
