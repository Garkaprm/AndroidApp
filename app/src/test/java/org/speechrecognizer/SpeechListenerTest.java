package org.speechrecognizer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.annotations.Nullable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Cancellable;
import org.junit.Before;
import org.junit.Test;
import org.speechrecognizer.impl.SpeechListener;

/**
 * Unit tests for {@link SpeechListener} class. Tests simulate listener's work and check that
 * expected subscriber's methods are called.
 */
public class SpeechListenerTest {

  private static final String TEST_BEGIN_RECOGNITION_WORD = "TEST_WORD";

  private SpeechListener speechListener;
  private SpeechListenerSubscriber speechListenerSubscriber;

  @Before
  public void init() {
    speechListener = new SpeechListener(TEST_BEGIN_RECOGNITION_WORD);
    speechListenerSubscriber = new SpeechListenerSubscriber();
    speechListener.subscribe(speechListenerSubscriber);
  }

  @Test
  public void testOnPartialResult() {
    speechListener.onPartialResult("hypothesis");
    checkNothingCalled();
  }

  @Test
  public void testOnResultBadWord() {
    callOnResult("hypothesis");
    checkNothingCalled();
  }

  @Test
  public void testOnResultBeginRecognitionWord() {
    callOnResult(TEST_BEGIN_RECOGNITION_WORD);
    checkOnNextCalled(ISpeechListener.START_RECOGNITION_EVENT);
  }

  @Test
  public void testOnResultRealPhrase() {
    startRecognition();

    String realPhrase = "The recognized phrase";
    callOnResult(realPhrase);
    checkOnNextCalled(realPhrase);
  }

  @Test
  public void testOnResultTwoPhrases() {
    startRecognition();

    String phrase = "Phrase1";
    callOnResult(phrase);
    checkOnNextCalled(phrase);
    speechListenerSubscriber.reset();

    phrase = "Phrase2";
    callOnResult(phrase);
    checkOnNextCalled(phrase);
  }

  @Test
  public void testOnResultNullPassed() {
    callOnResult(null);
    checkNothingCalled();
  }

  @Test(expected = NullPointerException.class)
  public void testOnResultNullDirectlyPassedNotSupported() {
    speechListener.onResult(null);
    checkNothingCalled();
  }

  @Test
  public void testOnResultEmptyPassed() {
    callOnResult("");
    checkNothingCalled();
  }

  @Test
  public void testRecognitionCompleted() {
    startRecognition();
    callOnResult("word");
    checkOnNextCalled("word");
    speechListenerSubscriber.reset();
    callOnResult(null);
    checkOnCompleteCalled();
  }

  @Test
  public void testOnError() {
    Exception ex = new Exception("test exception");
    speechListener.onError(ex);
    checkOnErrorCalled(ex);
  }

  @Test
  public void testOnTimeout() {
    speechListener.onTimeout();
    checkOnCompleteCalled();
  }

  @Test
  public void testOnFinalResult() {
    speechListener.onFinalResult("hypothesis");
    checkOnCompleteCalled();
  }

  private void startRecognition() {
    callOnResult(TEST_BEGIN_RECOGNITION_WORD);
    // reset the subscriber to correctly check later events
    speechListenerSubscriber.reset();
  }

  private void callOnResult(String textValue) {
    speechListener.onResult(createJson(textValue));
  }

  private String createJson(String textValue) {
    StringBuilder sb = new StringBuilder("{\"text\":");
    if (textValue == null) {
      sb.append("null");
    } else {
      sb.append('\"');
      sb.append(textValue);
      sb.append('\"');
    }
    sb.append('}');
    return sb.toString();
  }

  private void checkNothingCalled() {
    speechListenerSubscriber.checkNothingCalled();
  }

  private void checkOnNextCalled(String expectedValue) {
    speechListenerSubscriber.checkOnNextCalled(expectedValue);
  }

  private void checkOnCompleteCalled() {
    speechListenerSubscriber.checkOnCompleteCalled();
  }

  private void checkOnErrorCalled(Throwable expectedException) {
    speechListenerSubscriber.checkOnErrorCalled(expectedException);
  }

  private static class SpeechListenerSubscriber implements ObservableEmitter<String> {

    private boolean onNextCalled;
    private boolean onCompleteCalled;
    private boolean onErrorCalled;

    private String onNextValue;
    private Throwable onErrorException;

    public void reset() {
      onNextValue = null;
      onNextCalled = false;
      onCompleteCalled = false;
      onErrorCalled = false;
      onErrorException = null;
    }

    public void checkNothingCalled() {
      assertFalse("Unexpected call of onNext()", onNextCalled);
      assertNull("Unexpected value in onNext() call", onNextValue);
      assertFalse("Unexpected call of onComplete()", onCompleteCalled);
      assertFalse("Unexpected call of onError()", onErrorCalled);
      assertNull("Unexpected value in onError() call", onErrorException);
    }

    public void checkOnNextCalled(String expectedValue) {
      assertTrue("Expected to call onNext(), but it wasn't", onNextCalled);
      assertEquals("The value in onNext() is not equal to the expected one", expectedValue,
          onNextValue);
      assertFalse("Unexpected call of onComplete()", onCompleteCalled);
      assertFalse("Unexpected call of onError()", onErrorCalled);
      assertNull("Unexpected value in onError() call", onErrorException);
    }

    public void checkOnCompleteCalled() {
      assertTrue("Expected to call onComplete(), but it wasn't", onCompleteCalled);
      assertFalse("Unexpected call of onNext()", onNextCalled);
      assertNull("Unexpected value in onNext() call", onNextValue);
      assertFalse("Unexpected call of onError()", onErrorCalled);
      assertNull("Unexpected value in onError() call", onErrorException);
    }

    private void checkOnErrorCalled(Throwable expectedException) {
      assertTrue("Expected to call onError(), but it wasn't", onErrorCalled);
      assertEquals("The exception in onError() is not equal to the expected one", expectedException,
          onErrorException);
      assertFalse("Unexpected call of onNext()", onNextCalled);
      assertNull("Unexpected value in onNext() call", onNextValue);
      assertFalse("Unexpected call of onComplete()", onCompleteCalled);
    }

    @Override
    public void setDisposable(@Nullable Disposable d) {
    }

    @Override
    public void setCancellable(@Nullable Cancellable c) {
    }

    @Override
    public boolean isDisposed() {
      return false;
    }

    @Override
    public @NonNull ObservableEmitter<String> serialize() {
      return null;
    }

    @Override
    public boolean tryOnError(@NonNull Throwable t) {
      return false;
    }

    @Override
    public void onNext(@NonNull String value) {
      onNextCalled = true;
      onNextValue = value;
    }

    @Override
    public void onError(@NonNull Throwable error) {
      onErrorCalled = true;
      onErrorException = error;
    }

    @Override
    public void onComplete() {
      onCompleteCalled = true;
    }
  }
}