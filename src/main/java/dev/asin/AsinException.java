package dev.asin;

/** Base class for every SDK-defined exception. */
public class AsinException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  /** Creates the exception. */
  public AsinException(String message, Throwable cause) {
    super(message, cause);
  }
}
