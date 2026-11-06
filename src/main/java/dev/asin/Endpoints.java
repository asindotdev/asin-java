package dev.asin;

import java.util.List;
import java.util.Map;

/**
 * Path, methods, response kind and parameter names of every documented operation.
 *
 * <p>Generated from the Asin OpenAPI document. Do not edit by hand.
 */
public final class Endpoints {
  private Endpoints() {}

  /** One documented operation. */
  public record Endpoint(String path, List<String> methods, boolean json, List<String> required) {}

  /** Every documented operation keyed by its snake_case name. */
  public static final Map<String, Endpoint> ALL = Map.ofEntries(
      Map.entry("check_product", new Endpoint("/products/{id}/check", List.of("POST"), true, List.of("id"))),
      Map.entry("delete_product", new Endpoint("/products/{id}", List.of("DELETE"), true, List.of("id"))),
      Map.entry("fetch_offers", new Endpoint("/offers", List.of("POST"), true, List.of("asin"))),
      Map.entry("get_account", new Endpoint("/account", List.of("GET"), true, List.of())),
      Map.entry("get_asin_history", new Endpoint("/history", List.of("GET"), true, List.of("asin"))),
      Map.entry("get_offers", new Endpoint("/offers", List.of("GET"), true, List.of("asin"))),
      Map.entry("get_product", new Endpoint("/products/{id}", List.of("GET"), true, List.of("id"))),
      Map.entry("list_products", new Endpoint("/products", List.of("GET"), true, List.of())),
      Map.entry("search_amazon", new Endpoint("/search", List.of("GET"), true, List.of("query"))),
      Map.entry("track_product", new Endpoint("/products", List.of("POST"), true, List.of("product"))),
      Map.entry("update_product", new Endpoint("/products/{id}", List.of("PATCH"), true, List.of("id")))
  );
}
