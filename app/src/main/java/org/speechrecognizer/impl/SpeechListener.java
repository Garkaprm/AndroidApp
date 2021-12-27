package org.speechrecognizer.impl;

import android.util.Log;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.ObservableEmitter;
import org.json.JSONException;
import org.json.JSONObject;
import org.speechrecognizer.ISpeechListener;
import org.vosk.android.RecognitionListener;

/**
 * A listener of user's speech.
 */
public class SpeechListener implements ISpeechListener, RecognitionListener {

  private final String beginRecognizingWord;

  private ObservableEmitter<String> dataSubscriber;
  private boolean isInRecognizingMode;

  /**
   * Creates listener instance.
   *
   * @param beginRecognizingWord the word indicating start recognizing mode, cannot be {@code null}
   *                             or empty
   */
  public SpeechListener(@NonNull String beginRecognizingWord) {
    if (beginRecognizingWord.isEmpty()) {
      throw new IllegalArgumentException("Bad argument given: beginRecognizingWord cannot be empty");
    }
    this.beginRecognizingWord = beginRecognizingWord;
  }

  @Override
  public void subscribe(@NonNull ObservableEmitter<String> emitter) {
    this.dataSubscriber = emitter;
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
        // don't have new words from user, stop recognizing
        stopRecognizing();
      }
      return;
    }

    if (isInRecognizingMode) {
      dataSubscriber.onNext(word);
    } else if (beginRecognizingWord.equalsIgnoreCase(word)) {
      isInRecognizingMode = true;
      dataSubscriber.onNext(START_RECOGNITION_EVENT);
    }
    // else do nothing, not our control word
  }

  @Override
  public void onFinalResult(String hypothesis) {
    stopRecognizing();
  }

  @Override
  public void onError(Exception exception) {
    dataSubscriber.onError(exception);
  }

  @Override
  public void onTimeout() {
    stopRecognizing();
  }

  private void stopRecognizing() {
    isInRecognizingMode = false;
    dataSubscriber.onComplete();
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
