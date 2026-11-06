package dev.asin;

/** The client configuration is missing or unusable. */
public final class AsinConfigurationException extends AsinException {
  private static final long serialVersionUID = 1L;

  /** Creates the exception. */
  public AsinConfigurationException(String message) {
    super(message, null);
  }

  /** Creates the exception with a cause. */
  public AsinConfigurationException(String message, Throwable cause) {
    super(message, cause);
  }
}
