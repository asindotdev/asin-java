package dev.asin;

/** The request failed before a complete HTTP response was received (network error, timeout). */
public final class AsinTransportException extends AsinException {
  private static final long serialVersionUID = 1L;

  /** Creates the exception. */
  public AsinTransportException(String message, Throwable cause) {
    super(message, cause);
  }
}
