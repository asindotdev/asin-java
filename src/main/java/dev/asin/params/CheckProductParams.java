package dev.asin.params;

import dev.asin.AsinParams;
import dev.asin.AsinSerializationException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Parameters for {@code POST /products/{id}/check}.
 *
 * <p>Generated from the Asin OpenAPI document. Do not edit by hand.
 */
public final class CheckProductParams implements AsinParams {
  private final Map<String, Object> values;

  private CheckProductParams(Map<String, Object> values) {
    this.values = values;
  }

  /** Returns a new builder. */
  public static Builder builder() {
    return new Builder();
  }

  @Override
  public Map<String, Object> toMap() {
    return values;
  }

  /** Builder for {@link CheckProductParams}. */
  public static final class Builder {
    private final Map<String, Object> values = new LinkedHashMap<>();

    private Builder() {}

    /** Required. product_id returned when the product was added */
    public Builder id(String value) {
      values.put("id", value);
      return this;
    }

    /** Answer once the check is done (up to 45 seconds) Default: false. */
    public Builder wait(Boolean value) {
      values.put("wait", value);
      return this;
    }

    /** Validates required parameters and returns the immutable {@link CheckProductParams}. */
    public CheckProductParams build() {
      if (values.get("id") == null) {
        throw new AsinSerializationException("The \"id\" parameter is required.", "id");
      }
      return new CheckProductParams(java.util.Collections.unmodifiableMap(new LinkedHashMap<>(values)));
    }
  }
}
