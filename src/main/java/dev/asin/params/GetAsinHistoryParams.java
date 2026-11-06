package dev.asin.params;

import dev.asin.AsinParams;
import dev.asin.AsinSerializationException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Parameters for {@code GET /history}.
 *
 * <p>Generated from the Asin OpenAPI document. Do not edit by hand.
 */
public final class GetAsinHistoryParams implements AsinParams {
  private final Map<String, Object> values;

  private GetAsinHistoryParams(Map<String, Object> values) {
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

  /** Builder for {@link GetAsinHistoryParams}. */
  public static final class Builder {
    private final Map<String, Object> values = new LinkedHashMap<>();

    private Builder() {}

    /** Required. 10-character ASIN */
    public Builder asin(String value) {
      values.put("asin", value);
      return this;
    }

    /** Amazon store, e.g. com for amazon.com Allowed values: com, co.uk, de, fr, it, es, ca, co.jp, com.br, com.mx, in. Default: "com". */
    public Builder marketplace(String value) {
      values.put("marketplace", value);
      return this;
    }

    /** History period in days Default: 90. */
    public Builder days(Long value) {
      values.put("days", value);
      return this;
    }

    /** Validates required parameters and returns the immutable {@link GetAsinHistoryParams}. */
    public GetAsinHistoryParams build() {
      if (values.get("asin") == null) {
        throw new AsinSerializationException("The \"asin\" parameter is required.", "asin");
      }
      return new GetAsinHistoryParams(java.util.Collections.unmodifiableMap(new LinkedHashMap<>(values)));
    }
  }
}
