package dev.asin.params;

import dev.asin.AsinParams;
import dev.asin.AsinSerializationException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Parameters for {@code POST /offers}.
 *
 * <p>Generated from the Asin OpenAPI document. Do not edit by hand.
 */
public final class FetchOffersParams implements AsinParams {
  private final Map<String, Object> values;

  private FetchOffersParams(Map<String, Object> values) {
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

  /** Builder for {@link FetchOffersParams}. */
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

    /** Read Amazon again unless the last read is under 5 minutes old (1 credit) Default: false. */
    public Builder refresh(Boolean value) {
      values.put("refresh", value);
      return this;
    }

    /** Validates required parameters and returns the immutable {@link FetchOffersParams}. */
    public FetchOffersParams build() {
      if (values.get("asin") == null) {
        throw new AsinSerializationException("The \"asin\" parameter is required.", "asin");
      }
      return new FetchOffersParams(java.util.Collections.unmodifiableMap(new LinkedHashMap<>(values)));
    }
  }
}
