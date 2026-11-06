package dev.asin.examples;

import dev.asin.ApiResponse;
import dev.asin.AsinClient;
import dev.asin.params.SearchAmazonParams;

/** Run with ASIN_API_KEY set. */
public final class JsonExample {
  private JsonExample() {}

  public static void main(String[] args) throws Exception {
    AsinClient client = AsinClient.fromEnv();
    ApiResponse response = client.searchAmazon(SearchAmazonParams.builder().query("wireless earbuds").build());
    System.out.println(response.json().toPrettyString());
  }
}
