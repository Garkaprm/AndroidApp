package org.speechrecognizer;

/**
 * Update's user's activity during speech recognizing.
 */
public interface IActivityUpdater {

  /**
   * Adds recognized word.
   *
   * @param word the recognized word, cannot be {@code null}
   */
  void addRecognizedWord(String word);

  /**
   * Notifies about recognizing process start.
   */
  void onBeginRecognizing();

  /**
   * Notifies about recognizing process end.
   */
  void onEndRecognizing();

  /**
   * Shows error occurred.
   *
   * @param error the error message, can be {@code null}
   */
  void showError(String error);
}
