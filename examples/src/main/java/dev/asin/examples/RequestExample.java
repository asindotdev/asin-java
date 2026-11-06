package dev.asin.examples;

import dev.asin.ApiResponse;
import dev.asin.AsinApiException;
import dev.asin.AsinClient;
import java.util.Map;

/** Run with ASIN_API_KEY set. */
public final class RequestExample {
  private RequestExample() {}

  public static void main(String[] args) throws Exception {
    AsinClient client = AsinClient.fromEnv();
    try {
      ApiResponse response = client.request("GET", "/account", Map.of());
      System.out.println(response.text());
    } catch (AsinApiException e) {
      System.err.println("HTTP " + e.status() + ": " + e.getMessage());
    }
  }
}
