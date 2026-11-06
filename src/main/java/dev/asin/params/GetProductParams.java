package dev.asin.params;

import dev.asin.AsinParams;
import dev.asin.AsinSerializationException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Parameters for {@code GET /products/{id}}.
 *
 * <p>Generated from the Asin OpenAPI document. Do not edit by hand.
 */
public final class GetProductParams implements AsinParams {
  private final Map<String, Object> values;

  private GetProductParams(Map<String, Object> values) {
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

  /** Builder for {@link GetProductParams}. */
  public static final class Builder {
    private final Map<String, Object> values = new LinkedHashMap<>();

    private Builder() {}

    /** Required. product_id returned when the product was added */
    public Builder id(String value) {
      values.put("id", value);
      return this;
    }

    /** History period in days Default: 90. */
    public Builder days(Long value) {
      values.put("days", value);
      return this;
    }

    /** Validates required parameters and returns the immutable {@link GetProductParams}. */
    public GetProductParams build() {
      if (values.get("id") == null) {
        throw new AsinSerializationException("The \"id\" parameter is required.", "id");
      }
      return new GetProductParams(java.util.Collections.unmodifiableMap(new LinkedHashMap<>(values)));
    }
  }
}
