package dev.asin;

import java.util.Map;

/** Parameters of one endpoint, in wire order. Implemented by every class in {@code dev.asin.params}. */
public interface AsinParams {
  /** Wire name to value; null values are omitted from requests. */
  Map<String, Object> toMap();
}
