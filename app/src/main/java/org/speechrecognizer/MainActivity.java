package org.speechrecognizer;

import androidx.annotation.NonNull;
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

import org.vosk.LibVosk;
import org.vosk.LogLevel;

public class MainActivity extends AppCompatActivity {

  private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;

  private TextView textArea;
  private ImageView activeRecognizingIcon;

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
    // TODO
  }
}
