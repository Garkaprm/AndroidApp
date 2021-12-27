package org.speechrecognizer;

import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import java.io.IOException;
import org.vosk.LibVosk;
import org.vosk.LogLevel;
import org.vosk.Model;
import org.vosk.Recognizer;
import org.vosk.android.SpeechService;
import org.vosk.android.StorageService;

/**
 * Main application activity.
 */
public class MainActivity extends AppCompatActivity {

  private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;

  private TextView textArea;
  private ImageView activeRecognizingIcon;

  private Model voskModel;
  private SpeechService speechService;
  private SpeechListener speechListener;

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
      @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    if (requestCode == PERMISSIONS_REQUEST_RECORD_AUDIO) {
      if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        // permissions obtained, can begin model initialization
        initModel();
      } else {
        // can't work in such case
        finish();
      }
    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    LibVosk.setLogLevel(LogLevel.INFO);
    prepareUi();

    if (hasRequiredPermissions()) {
      // can begin model initialization
      initModel();
    } else {
      requestPermissions();
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();

    if (speechService != null) {
      speechService.stop();
      speechService.shutdown();
      speechService = null;
    }
  }

  private void prepareUi() {
    textArea = findViewById(R.id.text_area);
    textArea.setText(R.string.wait_message);
    textArea.setMovementMethod(new ScrollingMovementMethod());

    activeRecognizingIcon = findViewById(R.id.mic_icon);
    activeRecognizingIcon.setVisibility(View.INVISIBLE);
  }

  private boolean hasRequiredPermissions() {
    int permissionCheck = ContextCompat
        .checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO);
    return permissionCheck == PackageManager.PERMISSION_GRANTED;
  }

  private void requestPermissions() {
    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},
        PERMISSIONS_REQUEST_RECORD_AUDIO);
  }

  private void initModel() {
    StorageService.unpack(this, "model-ru", "model",
        model -> {
          this.voskModel = model;
          showReadyMessage();
          startListening();
        },
        exception -> {
          Log.e(MainActivity.class.getName(), exception.getMessage(), exception);
          showError("Ошибка инициализации модели: " + exception.getMessage());
        });
  }

  private void startListening() {
    try {
      boolean isFirstTime = speechListener == null;

      if (isFirstTime) {
        Recognizer rec = new Recognizer(voskModel, 16000.0f);
        this.speechService = new SpeechService(rec, 16000.0f);
        this.speechListener = new SpeechListener(getBeginRecognizingWord());
      }

      Observable.create(speechListener)
          .subscribe(this::onNextWord, this::onError, this::onEndRecognizing);

      if (isFirstTime) {
        speechService.startListening(speechListener);
      }
    } catch (IOException e) {
      Log.e(MainActivity.class.getName(), e.getMessage(), e);
      showError("Ошибка: " + e.getMessage());
    }
  }

  private String getBeginRecognizingWord() {
    return getApplicationContext().getResources().getText(R.string.begin_recognize_word).toString();
  }

  private void onNextWord(@NonNull String word) {
    if (SpeechListener.START_RECOGNITION_EVENT.equals(word)) {
      onBeginRecognizing();
    } else {
      addRecognizedWord(word);
    }
  }

  private void addRecognizedWord(@NonNull String word) {
    textArea.append(word + " ");
  }

  private void onError(@NonNull Throwable e) {
    Log.e(MainActivity.class.getName(), e.getMessage(), e);
    showError("Ошибка распознавания: " + e.getMessage());
  }

  private void showError(String error) {
    textArea.setText(error);
  }

  private void onBeginRecognizing() {
    textArea.setText("");
    activeRecognizingIcon.setVisibility(View.VISIBLE);
  }

  private void onEndRecognizing() {
    showReadyMessage();

    // start listening again for the next phrase
    startListening();
  }

  private void showReadyMessage() {
    textArea.setText(R.string.ready_message);
    activeRecognizingIcon.setVisibility(View.INVISIBLE);
  }
}
