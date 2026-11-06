package dev.asin.params;

import dev.asin.AsinParams;
import dev.asin.AsinSerializationException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Parameters for {@code GET /products}.
 *
 * <p>Generated from the Asin OpenAPI document. Do not edit by hand.
 */
public final class ListProductsParams implements AsinParams {
  private final Map<String, Object> values;

  private ListProductsParams(Map<String, Object> values) {
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

  /** Builder for {@link ListProductsParams}. */
  public static final class Builder {
    private final Map<String, Object> values = new LinkedHashMap<>();

    private Builder() {}

    /** Only this ASIN */
    public Builder asin(String value) {
      values.put("asin", value);
      return this;
    }

    /** Only this Amazon store Allowed values: com, co.uk, de, fr, it, es, ca, co.jp, com.br, com.mx, in. */
    public Builder marketplace(String value) {
      values.put("marketplace", value);
      return this;
    }

    /** Only the products of one of the app's watchlist filters. all: Every tracked product (default). drops: Cheaper now than when the product was added. target: At or below its target price. out_of_stock: No offer at the last check. attention: The last check didn't go through (last_error is set): no cred… Allowed values: all, drops, target, out_of_stock, attention, paused. Default: "all". */
    public Builder filter(String value) {
      values.put("filter", value);
      return this;
    }

    /** Page number Default: 1. */
    public Builder page(Long value) {
      values.put("page", value);
      return this;
    }

    /** Products per page Default: 20. */
    public Builder limit(Long value) {
      values.put("limit", value);
      return this;
    }

    /** Validates required parameters and returns the immutable {@link ListProductsParams}. */
    public ListProductsParams build() {
      return new ListProductsParams(java.util.Collections.unmodifiableMap(new LinkedHashMap<>(values)));
    }
  }
}
