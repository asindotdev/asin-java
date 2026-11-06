package dev.asin.params;

import dev.asin.AsinParams;
import dev.asin.AsinSerializationException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Parameters for {@code GET /search}.
 *
 * <p>Generated from the Asin OpenAPI document. Do not edit by hand.
 */
public final class SearchAmazonParams implements AsinParams {
  private final Map<String, Object> values;

  private SearchAmazonParams(Map<String, Object> values) {
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

  /** Builder for {@link SearchAmazonParams}. */
  public static final class Builder {
    private final Map<String, Object> values = new LinkedHashMap<>();

    private Builder() {}

    /** Required. Keywords */
    public Builder query(String value) {
      values.put("query", value);
      return this;
    }

    /** Amazon store, e.g. com for amazon.com Allowed values: com, co.uk, de, fr, it, es, ca, co.jp, com.br, com.mx, in. Default: "com". */
    public Builder marketplace(String value) {
      values.put("marketplace", value);
      return this;
    }

    /** Validates required parameters and returns the immutable {@link SearchAmazonParams}. */
    public SearchAmazonParams build() {
      if (values.get("query") == null) {
        throw new AsinSerializationException("The \"query\" parameter is required.", "query");
      }
      return new SearchAmazonParams(java.util.Collections.unmodifiableMap(new LinkedHashMap<>(values)));
    }
  }
}
