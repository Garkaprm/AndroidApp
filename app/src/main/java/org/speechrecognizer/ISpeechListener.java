package org.speechrecognizer;

import io.reactivex.rxjava3.core.ObservableOnSubscribe;

/**
 * Represents speech listener which can be observed from outside. When new user words were
 * recognized, the listener emits events.
 */
public interface ISpeechListener extends ObservableOnSubscribe<String> {

  /**
   * Identifies moment of recognition start.
   */
  String START_RECOGNITION_EVENT = "START_RECOGNITION_EVENT";
}
