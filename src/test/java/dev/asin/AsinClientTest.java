package dev.asin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.asin.params.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.junit.jupiter.api.Test;

class AsinClientTest {
  private static final ObjectMapper MAPPER = new ObjectMapper();

  static final class Recording implements AsinTransport {
    final List<Request> requests = new ArrayList<>();
    Function<Request, RawResponse> responder = request -> json(200, "{\"ok\":true}");

    @Override
    public RawResponse send(Request request) throws IOException {
      requests.add(request);
      return responder.apply(request);
    }
  }

  static RawResponse json(int status, String body) {
    return new RawResponse(
        status, Map.of("content-type", List.of("application/json; charset=utf-8")), body.getBytes(StandardCharsets.UTF_8));
  }

  private final Recording transport = new Recording();
  private final AsinClient client =
      AsinClient.builder().apiKey("test-key").transport(transport).retryDelay(java.time.Duration.ofMillis(1)).build();

  @Test
  void requiresApiKey() {
    assertThrows(AsinConfigurationException.class, () -> AsinClient.builder().apiKey(null).build());
    assertThrows(AsinConfigurationException.class, () -> AsinClient.builder().apiKey("   ").build());
    assertThrows(
        AsinConfigurationException.class,
        () -> AsinClient.builder().apiKey("k").baseUrl("ftp://example.test").build());
  }

  @Test
  void featuredOperation() throws IOException {
    transport.responder = request -> json(200, "{\"hello\":\"world\"}");
    ApiResponse response = client.searchAmazon(SearchAmazonParams.builder().query("wireless earbuds").build());
    assertEquals("world", response.json().get("hello").asText());
    AsinTransport.Request request = transport.requests.get(0);
    assertEquals("GET", request.method());
    assertEquals("https://www.asin.dev/api/v1/search", request.uri().toString().split("\\?")[0]);
    assertEquals("Bearer test-key", request.headers().get("Authorization"));
    assertTrue(request.headers().get("User-Agent").startsWith("asin-java/"));
  }

  @Test
  void querySerialization() {
    AsinClient local = AsinClient.builder().apiKey("k").baseUrl("https://example.test/").transport(transport).build();
    Map<String, Object> params = new LinkedHashMap<>();
    params.put("text", "a b&c");
    params.put("flag", false);
    params.put("count", 0);
    params.put("ratio", 1.5);
    params.put("whole", 2.0);
    params.put("list", List.of("x", "y"));
    params.put("nothing", null);
    local.request("GET", "/anything", params);
    assertEquals(
        "https://example.test/anything?text=a%20b%26c&flag=false&count=0&ratio=1.5&whole=2&list=x&list=y",
        transport.requests.get(0).uri().toString());
  }

  @Test
  void postSendsJsonBody() throws IOException {
    Map<String, Object> params = new LinkedHashMap<>();
    params.put("query", "x");
    params.put("page", 2);
    params.put("gone", null);
    client.request("POST", "/anything", params);
    AsinTransport.Request request = transport.requests.get(0);
    assertEquals("POST", request.method());
    assertEquals("application/json", request.headers().get("Content-Type"));
    assertEquals(MAPPER.readTree("{\"query\":\"x\",\"page\":2}"), MAPPER.readTree(request.body()));
    assertEquals(null, request.uri().getRawQuery());
  }

  @Test
  void waitLocationDependsOnTheEndpoint() throws IOException {
    client.request("POST", "/products/{id}/check", Map.of("id", "p1", "wait", true));
    assertEquals("wait=true", transport.requests.get(0).uri().getRawQuery());
    assertEquals(MAPPER.readTree("{}"), MAPPER.readTree(transport.requests.get(0).body()));
    client.request("POST", "/products", Map.of("wait", true));
    assertEquals(null, transport.requests.get(1).uri().getRawQuery());
    assertEquals(MAPPER.readTree("{\"wait\":true}"), MAPPER.readTree(transport.requests.get(1).body()));
  }

  @Test
  void patchAndDelete() throws IOException {
    client.request("PATCH", "/products/{id}", Map.of("id", "p1", "paused", true));
    assertEquals("PATCH", transport.requests.get(0).method());
    assertEquals(MAPPER.readTree("{\"paused\":true}"), MAPPER.readTree(transport.requests.get(0).body()));
    client.request("DELETE", "/products/{id}", Map.of("id", "p1"));
    assertEquals("DELETE", transport.requests.get(1).method());
    assertEquals(null, transport.requests.get(1).body());
  }

  @Test
  void pathParameters() {
    client.request("GET", "/items/{id}", Map.of("id", "a/b c"));
    assertEquals("https://www.asin.dev/api/v1/items/a%2Fb%20c", transport.requests.get(0).uri().toString());
    assertThrows(AsinSerializationException.class, () -> client.request("GET", "/items/{id}", Map.of()));
  }

  @Test
  void requiredParameters() {
    assertThrows(AsinSerializationException.class, () -> SearchAmazonParams.builder().build());
    assertTrue(transport.requests.isEmpty());
  }

  @Test
  void unsupportedMethod() {
    assertThrows(
        AsinConfigurationException.class,
        () -> client.getAccount(RequestOptions.method("POST")));
  }

  @Test
  void everyEndpoint() throws Exception {
    Map<String, Runnable> calls = new LinkedHashMap<>();
    calls.put("check_product", () -> client.checkProduct(CheckProductParams.builder().id("test").build()));
    calls.put("delete_product", () -> client.deleteProduct(DeleteProductParams.builder().id("test").build()));
    calls.put("fetch_offers", () -> client.fetchOffers(FetchOffersParams.builder().asin("test").build()));
    calls.put("get_account", () -> client.getAccount());
    calls.put("get_asin_history", () -> client.getAsinHistory(GetAsinHistoryParams.builder().asin("test").build()));
    calls.put("get_offers", () -> client.getOffers(GetOffersParams.builder().asin("test").build()));
    calls.put("get_product", () -> client.getProduct(GetProductParams.builder().id("test").build()));
    calls.put("list_products", () -> client.listProducts(ListProductsParams.builder().build()));
    calls.put("search_amazon", () -> client.searchAmazon(SearchAmazonParams.builder().query("test").build()));
    calls.put("track_product", () -> client.trackProduct(TrackProductParams.builder().product("test").build()));
    calls.put("update_product", () -> client.updateProduct(UpdateProductParams.builder().id("test").build()));
    for (Map.Entry<String, Endpoints.Endpoint> entry : Endpoints.ALL.entrySet()) {
      Runnable call = calls.get(entry.getKey());
      assertNotNull(call, entry.getKey());
      call.run();
      AsinTransport.Request request = transport.requests.get(transport.requests.size() - 1);
      assertEquals(
          "https://www.asin.dev/api/v1" + entry.getValue().path().replace("{id}", "test"),
          request.uri().toString().split("\\?")[0],
          entry.getKey());
      assertEquals(entry.getValue().methods().get(0), request.method(), entry.getKey());
    }
    assertEquals(Endpoints.ALL.size(), calls.size());
  }

  @Test
  void binaryResponse() {
    transport.responder = request -> new RawResponse(200, Map.of("content-type", List.of("image/png")), new byte[] {(byte) 0x89, 'P', 'N', 'G'});
    ApiResponse response = client.request("GET", "/anything", Map.of());
    assertFalse(response.isJson());
    assertEquals(4, response.bytes().length);
    assertEquals("image/png", response.contentType());
  }

  @Test
  void nonJsonSuccessForJsonOperation() {
    transport.responder = request -> new RawResponse(200, Map.of("content-type", List.of("text/html")), "<html>".getBytes(StandardCharsets.UTF_8));
    assertThrows(AsinResponseDecodingException.class, () -> client.searchAmazon(SearchAmazonParams.builder().query("wireless earbuds").build()));
  }

  @Test
  void apiError() {
    transport.responder = request -> json(401, "{\"error\":\"Invalid API Key\"}");
    AsinApiException error = assertThrows(AsinApiException.class, () -> client.request("GET", "/account", Map.of()));
    assertEquals(401, error.status());
    assertEquals("Invalid API Key", error.getMessage());
    assertEquals("Invalid API Key", error.responseBody().get("error").asText());
  }

  @Test
  void nonJsonErrorBody() {
    transport.responder = request -> new RawResponse(502, Map.of(), "Bad gateway".getBytes(StandardCharsets.UTF_8));
    AsinApiException error = assertThrows(AsinApiException.class, () -> client.request("GET", "/account", Map.of()));
    assertEquals(502, error.status());
    assertEquals("Bad gateway", new String(error.rawResponse().body(), StandardCharsets.UTF_8));
  }

  @Test
  void transportError() {
    AsinClient failing = AsinClient.builder().apiKey("k").maxRetries(0).transport(request -> {
      throw new IOException("network down");
    }).build();
    assertThrows(AsinTransportException.class, () -> failing.request("GET", "/account", Map.of()));
  }

  @Test
  void decodesIntoTypes() {
    transport.responder = request -> json(200, "{\"value\":42}");
    record Payload(int value) {}
    assertEquals(42, client.request("GET", "/account", Map.of()).as(Payload.class).value());
  }

  @Test
  void retriesThenSucceeds() {
    int[] calls = {0};
    AsinClient flaky = AsinClient.builder().apiKey("k").retryDelay(java.time.Duration.ofMillis(1)).transport(request -> {
      calls[0]++;
      if (calls[0] == 1) {
        throw new IOException("reset");
      }
      if (calls[0] == 2) {
        return new RawResponse(429, Map.of("retry-after", List.of("0"), "content-type", List.of("application/json")), "{}".getBytes(StandardCharsets.UTF_8));
      }
      return json(200, "{}");
    }).build();
    assertTrue(flaky.request("GET", "/account", Map.of()).isJson());
    assertEquals(3, calls[0]);
  }

  @Test
  void retryLimitsAndClientErrors() {
    AsinClient limited = AsinClient.builder().apiKey("k").transport(transport).maxRetries(2).retryDelay(java.time.Duration.ofMillis(1)).build();
    transport.responder = request -> json(503, "{}");
    assertThrows(AsinApiException.class, () -> limited.request("GET", "/account", Map.of()));
    assertEquals(3, transport.requests.size());
    transport.responder = request -> json(402, "{}");
    assertThrows(AsinApiException.class, () -> limited.request("GET", "/account", Map.of()));
    assertEquals(4, transport.requests.size());
    transport.responder = request -> json(500, "{}");
    assertThrows(AsinApiException.class, () -> limited.request("GET", "/account", Map.of(), RequestOptions.builder().maxRetries(0).build()));
    assertEquals(5, transport.requests.size());
  }

  @Test
  void unauthorizedIsNeverRetried() {
    Map<String, Integer> cases = new LinkedHashMap<>();
    cases.put("Rate limit exceeded: You've exceeded the rate limit on your subscription", 1);
    cases.put("Rate limit exceeded for the API key: quota daily", 1);
    cases.put("Invalid API Key", 1);
    for (Map.Entry<String, Integer> c : cases.entrySet()) {
      int[] calls = {0};
      AsinClient limited = AsinClient.builder().apiKey("k").retryDelay(java.time.Duration.ofMillis(1)).transport(request -> {
        calls[0]++;
        return calls[0] == 1 ? json(401, "{\"error\":\"" + c.getKey() + "\"}") : json(200, "{}");
      }).build();
      try {
        limited.request("GET", "/account", Map.of());
      } catch (AsinApiException e) {
        // expected for the non-retried cases
      }
      assertEquals(c.getValue(), calls[0], c.getKey());
    }
  }

  @Test
  void readsNestedErrorMessage() {
    AsinClient failing = AsinClient.builder().apiKey("k").maxRetries(0).transport(request ->
        json(402, "{\"error\":{\"code\":\"insufficient_credits\",\"message\":\"No credits left\"}}")).build();
    AsinApiException error = assertThrows(AsinApiException.class, () -> failing.request("GET", "/account", Map.of()));
    assertEquals("No credits left", error.errorMessage());
    client.searchAmazon(SearchAmazonParams.builder().query("wireless earbuds").build());
  }

  @Test
  void batchKeepsOrderAndIsolatesFailures() {
    java.util.concurrent.atomic.AtomicInteger running = new java.util.concurrent.atomic.AtomicInteger();
    java.util.concurrent.atomic.AtomicInteger peak = new java.util.concurrent.atomic.AtomicInteger();
    List<BatchResult<Integer>> results = client.batch(List.of(1, 2, 3, 4, 5, 6), 2, n -> {
      peak.accumulateAndGet(running.incrementAndGet(), Math::max);
      try {
        Thread.sleep(5);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
      running.decrementAndGet();
      if (n == 4) {
        throw new IllegalStateException("boom");
      }
      return n * 10;
    });
    assertEquals(2, peak.get());
    assertEquals(6, results.size());
    assertFalse(results.get(3).ok());
    assertEquals(60, results.get(5).value());
    assertEquals(3, results.get(3).index());
  }
}
