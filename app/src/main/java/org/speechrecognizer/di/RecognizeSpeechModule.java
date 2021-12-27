package org.speechrecognizer.di;

import org.speechrecognizer.RecognizeSpeechService;
import toothpick.config.Module;

/**
 * Module to bind service implementations.
 */
public class RecognizeSpeechModule extends Module {

  /**
   * Creates module instance.
   */
  public RecognizeSpeechModule() {
    bind(RecognizeSpeechService.class).singleton();
  }
}
