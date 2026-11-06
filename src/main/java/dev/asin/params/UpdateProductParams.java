package dev.asin.params;

import dev.asin.AsinParams;
import dev.asin.AsinSerializationException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Parameters for {@code PATCH /products/{id}}.
 *
 * <p>Generated from the Asin OpenAPI document. Do not edit by hand.
 */
public final class UpdateProductParams implements AsinParams {
  private final Map<String, Object> values;

  private UpdateProductParams(Map<String, Object> values) {
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

  /** Builder for {@link UpdateProductParams}. */
  public static final class Builder {
    private final Map<String, Object> values = new LinkedHashMap<>();

    private Builder() {}

    /** Required. product_id returned when the product was added */
    public Builder id(String value) {
      values.put("id", value);
      return this;
    }

    /** Email the owner when the price goes at or below this amount. null removes it. */
    public Builder targetPrice(Number value) {
      values.put("target_price", value);
      return this;
    }

    /** Minutes between two checks (1440 = daily). Checks faster than daily need a plan or a credit pack: every 60 minutes with a pack or Starter, 30 with Pro, 5 with Business. Default: 1440. */
    public Builder intervalMinutes(Long value) {
      values.put("interval_minutes", value);
      return this;
    }

    /** Email when the price drops or the product is back in stock */
    public Builder alertOnDrop(Boolean value) {
      values.put("alert_on_drop", value);
      return this;
    }

    /** Stop checking (and charging) for now; false resumes */
    public Builder paused(Boolean value) {
      values.put("paused", value);
      return this;
    }

    /** Validates required parameters and returns the immutable {@link UpdateProductParams}. */
    public UpdateProductParams build() {
      if (values.get("id") == null) {
        throw new AsinSerializationException("The \"id\" parameter is required.", "id");
      }
      return new UpdateProductParams(java.util.Collections.unmodifiableMap(new LinkedHashMap<>(values)));
    }
  }
}
