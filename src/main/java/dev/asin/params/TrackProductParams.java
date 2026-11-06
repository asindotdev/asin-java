package dev.asin.params;

import dev.asin.AsinParams;
import dev.asin.AsinSerializationException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Parameters for {@code POST /products}.
 *
 * <p>Generated from the Asin OpenAPI document. Do not edit by hand.
 */
public final class TrackProductParams implements AsinParams {
  private final Map<String, Object> values;

  private TrackProductParams(Map<String, Object> values) {
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

  /** Builder for {@link TrackProductParams}. */
  public static final class Builder {
    private final Map<String, Object> values = new LinkedHashMap<>();

    private Builder() {}

    /** Required. ASIN (B09DFCB66S) or Amazon product link */
    public Builder product(String value) {
      values.put("product", value);
      return this;
    }

    /** Amazon store, e.g. com for amazon.com. A product link sets it on its own. Allowed values: com, co.uk, de, fr, it, es, ca, co.jp, com.br, com.mx, in. Default: "com". */
    public Builder marketplace(String value) {
      values.put("marketplace", value);
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

    /** Answer once the first check is done (up to 45 seconds) Default: false. */
    public Builder wait(Boolean value) {
      values.put("wait", value);
      return this;
    }

    /** Validates required parameters and returns the immutable {@link TrackProductParams}. */
    public TrackProductParams build() {
      if (values.get("product") == null) {
        throw new AsinSerializationException("The \"product\" parameter is required.", "product");
      }
      return new TrackProductParams(java.util.Collections.unmodifiableMap(new LinkedHashMap<>(values)));
    }
  }
}
