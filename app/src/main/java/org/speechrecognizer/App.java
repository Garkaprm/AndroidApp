package org.speechrecognizer;

import android.app.Application;
import org.speechrecognizer.di.RecognizeSpeechModule;
import toothpick.Scope;
import toothpick.Toothpick;

/**
 * Maintains application state.
 */
public final class App extends Application {

  /**
   * Specifies application scope name.
   */
  public static final String APP_SCOPE_NAME = "APP";

  @Override
  public void onCreate() {
    super.onCreate();
    Scope appScope = Toothpick.openScope(APP_SCOPE_NAME);
    appScope.installModules(new RecognizeSpeechModule());
  }
}
