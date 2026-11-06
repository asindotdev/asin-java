package dev.asin;

/** A request value cannot be represented safely on the wire, or a required parameter is missing. */
public final class AsinSerializationException extends AsinException {
  private static final long serialVersionUID = 1L;
  private final String param;

  /** Creates the exception. */
  public AsinSerializationException(String message, String param) {
    this(message, param, null);
  }

  /** Creates the exception with a cause. */
  public AsinSerializationException(String message, String param, Throwable cause) {
    super(message, cause);
    this.param = param;
  }

  /** The parameter at fault, or null. */
  public String param() {
    return param;
  }
}
