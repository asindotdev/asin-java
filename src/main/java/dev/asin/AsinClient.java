package dev.asin;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Function;
import dev.asin.params.*;

/**
 * Official Asin API client. Create one instance and reuse it; it is thread-safe.
 *
 * <pre>{@code
 * AsinClient client = AsinClient.fromEnv(); // reads ASIN_API_KEY
 * ApiResponse response = client.searchAmazon(SearchAmazonParams.builder().query("wireless earbuds").build());
 * }</pre>
 */
public final class AsinClient {
  /** Production API origin. */
  public static final String DEFAULT_BASE_URL = "https://www.asin.dev/api/v1";

  /** Default client-side deadline per request. */
  public static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(120);

  /** Environment variable read by {@link #fromEnv()}. */
  public static final String API_KEY_ENVIRONMENT_VARIABLE = "ASIN_API_KEY";

  /** SDK version sent in the User-Agent header. */
  public static final String VERSION = "0.1.0";

  /** Tasks run at the same time by {@link #batch}. */
  public static final int DEFAULT_CONCURRENCY = 10;

  /** Retries after a network error or a 408, 429, 500, 502, 503 or 504 response. */
  public static final int MAX_RETRIES = 3;

  /** Each retry waits this many times longer than the previous one (a Retry-After header wins). */
  public static final double RETRY_BACKOFF_FACTOR = 1.5;

  /** Delay before the first retry. */
  public static final Duration DEFAULT_RETRY_DELAY = Duration.ofSeconds(1);

  /** Longest delay between two attempts. */
  public static final Duration MAX_RETRY_DELAY = Duration.ofSeconds(30);

  private static final Set<Integer> RETRYABLE_STATUSES = Set.of(408, 429, 500, 502, 503, 504);

  private static final List<String> ERROR_MESSAGE_KEYS = List.of("error", "message", "error_message", "detail");

  private final String apiKey;
  private final String baseUrl;
  private final Duration timeout;
  private final Map<String, String> headers;
  private final AsinTransport transport;
  private final int maxRetries;
  private final Duration retryDelay;

  private AsinClient(Builder builder) {
    String key = builder.apiKey;
    if (key == null || key.isBlank()) {
      throw new AsinConfigurationException(
          "A non-blank Asin API key is required. Pass apiKey(...) or set " + API_KEY_ENVIRONMENT_VARIABLE + ".");
    }
    if (key.indexOf('\r') >= 0 || key.indexOf('\n') >= 0) {
      throw new AsinConfigurationException("The Asin API key cannot be used in an HTTP header.");
    }
    String base = builder.baseUrl.replaceAll("/+$", "");
    URI uri;
    try {
      uri = URI.create(base);
    } catch (IllegalArgumentException e) {
      throw new AsinConfigurationException("baseUrl must be an absolute http or https URL.", e);
    }
    if (!("https".equals(uri.getScheme()) || "http".equals(uri.getScheme())) || uri.getHost() == null) {
      throw new AsinConfigurationException("baseUrl must be an absolute http or https URL.");
    }
    if (builder.timeout != null && (builder.timeout.isNegative() || builder.timeout.isZero())) {
      throw new AsinConfigurationException("timeout must be positive, or null to disable it.");
    }
    this.apiKey = key;
    this.baseUrl = base;
    this.timeout = builder.timeout;
    this.headers = Map.copyOf(builder.headers);
    if (builder.maxRetries < 0) {
      throw new AsinConfigurationException("maxRetries must be zero or more.");
    }
    if (builder.retryDelay == null || builder.retryDelay.isNegative()) {
      throw new AsinConfigurationException("retryDelay must be zero or more.");
    }
    this.maxRetries = builder.maxRetries;
    this.retryDelay = builder.retryDelay;
    this.transport = builder.transport != null ? builder.transport : AsinTransport.defaultTransport();
  }

  /** Creates a client that reads the API key from {@value #API_KEY_ENVIRONMENT_VARIABLE}. */
  public static AsinClient fromEnv() {
    return builder().build();
  }

  /** Creates a client with an explicit API key. */
  public static AsinClient of(String apiKey) {
    return builder().apiKey(apiKey).build();
  }

  /** Returns a new builder. The API key defaults to {@value #API_KEY_ENVIRONMENT_VARIABLE}. */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Sends any request, including endpoints released after this SDK version. GET parameters travel
   * in the query string; POST parameters travel in a JSON body.
   */
  public ApiResponse request(String method, String path, Map<String, ?> params) {
    return request(method, path, params, RequestOptions.defaults());
  }

  /** {@link #request(String, String, Map)} with per-request options. */
  public ApiResponse request(String method, String path, Map<String, ?> params, RequestOptions options) {
    String verb = method == null ? "" : method.toUpperCase(Locale.ROOT);
    return send(verb, path, params == null ? Map.of() : params, options, false);
  }

  /**
   * Check the price now. Asks for a check right away (1 credit), at most once per 5 minutes; earlier calls return the product unchanged. A paused product has to be resumed first. The check runs in the background; with wait=true the answer waits for it. Read last_e…
   *
   * <p>{@code POST /products/{id}/check}
   */
  public ApiResponse checkProduct(CheckProductParams params) {
    return checkProduct(params, RequestOptions.defaults());
  }

  /** {@code POST /products/{id}/check} with per-request options. */
  public ApiResponse checkProduct(CheckProductParams params, RequestOptions options) {
    return call("check_product", params == null ? Map.of() : params.toMap(), options);
  }

  /**
   * Stop tracking a product. Removes the product and its price history.
   *
   * <p>{@code DELETE /products/{id}}
   */
  public ApiResponse deleteProduct(DeleteProductParams params) {
    return deleteProduct(params, RequestOptions.defaults());
  }

  /** {@code DELETE /products/{id}} with per-request options. */
  public ApiResponse deleteProduct(DeleteProductParams params, RequestOptions options) {
    return call("delete_product", params == null ? Map.of() : params.toMap(), options);
  }

  /**
   * Read every offer from Amazon. Reads the offers on Amazon for 1 credit, refunded if it doesn't answer. A read made in the last 3 hours, by anyone, is returned instead, for free (charged: false). The answer is the same as GET /offers.
   *
   * <p>{@code POST /offers}
   */
  public ApiResponse fetchOffers(FetchOffersParams params) {
    return fetchOffers(params, RequestOptions.defaults());
  }

  /** {@code POST /offers} with per-request options. */
  public ApiResponse fetchOffers(FetchOffersParams params, RequestOptions options) {
    return call("fetch_offers", params == null ? Map.of() : params.toMap(), options);
  }

  /**
   * Plan and remaining credits. Your plan, the credits left (monthly allowance and packs), how many products you track and the credits they use per month.
   *
   * <p>{@code GET /account}
   */
  public ApiResponse getAccount() {
    return getAccount(RequestOptions.defaults());
  }

  /** {@code GET /account} with per-request options. */
  public ApiResponse getAccount(RequestOptions options) {
    return call("get_account", Map.of(), options);
  }

  /**
   * Price history of any ASIN. Every price Asin read for an ASIN on a marketplace over the period, whoever tracks it, oldest first, with the lowest, highest and average of those prices. A history of more than 5000 points is thinned evenly, keeping its first and last poi…
   *
   * <p>{@code GET /history}
   */
  public ApiResponse getAsinHistory(GetAsinHistoryParams params) {
    return getAsinHistory(params, RequestOptions.defaults());
  }

  /** {@code GET /history} with per-request options. */
  public ApiResponse getAsinHistory(GetAsinHistoryParams params, RequestOptions options) {
    return call("get_asin_history", params == null ? Map.of() : params.toMap(), options);
  }

  /**
   * Offers already known. Who sells the product and at what price, as last read, cheapest first, with Amazon's own price and the cheapest new and used offers (used is only known for Amazon's own outlet). Free: this never reads Amazon or spends a credit. When nobody…
   *
   * <p>{@code GET /offers}
   */
  public ApiResponse getOffers(GetOffersParams params) {
    return getOffers(params, RequestOptions.defaults());
  }

  /** {@code GET /offers} with per-request options. */
  public ApiResponse getOffers(GetOffersParams params, RequestOptions options) {
    return call("get_offers", params == null ? Map.of() : params.toMap(), options);
  }

  /**
   * Get a product and its price history. The product's latest state and every price read of the period, oldest first. The history covers every read of this ASIN on this marketplace, including those made before you added it, so a product added today can come with months of prices;…
   *
   * <p>{@code GET /products/{id}}
   */
  public ApiResponse getProduct(GetProductParams params) {
    return getProduct(params, RequestOptions.defaults());
  }

  /** {@code GET /products/{id}} with per-request options. */
  public ApiResponse getProduct(GetProductParams params, RequestOptions options) {
    return call("get_product", params == null ? Map.of() : params.toMap(), options);
  }

  /**
   * List tracked products. Your watchlist, newest first, with each product's latest state and its recent prices (trend). Filter with asin (and marketplace) to know if a product is tracked, or with filter to get the same lists as the app's tabs.
   *
   * <p>{@code GET /products}
   */
  public ApiResponse listProducts(ListProductsParams params) {
    return listProducts(params, RequestOptions.defaults());
  }

  /** {@code GET /products} with per-request options. */
  public ApiResponse listProducts(ListProductsParams params, RequestOptions options) {
    return call("list_products", params == null ? Map.of() : params.toMap(), options);
  }

  /**
   * Search Amazon. Amazon search results (up to 60, sponsored included), to find the ASIN of a product. Free, limited to 30 searches per hour.
   *
   * <p>{@code GET /search}
   */
  public ApiResponse searchAmazon(SearchAmazonParams params) {
    return searchAmazon(params, RequestOptions.defaults());
  }

  /** {@code GET /search} with per-request options. */
  public ApiResponse searchAmazon(SearchAmazonParams params, RequestOptions options) {
    return call("search_amazon", params == null ? Map.of() : params.toMap(), options);
  }

  /**
   * Track a product. Adds an ASIN, an Amazon link or a short link (amzn.to, a.co…) to your watchlist. The first check runs in the background within seconds (checking is true until then): poll GET /products/{id}, or send wait: true to get the answer once it's d…
   *
   * <p>{@code POST /products}
   */
  public ApiResponse trackProduct(TrackProductParams params) {
    return trackProduct(params, RequestOptions.defaults());
  }

  /** {@code POST /products} with per-request options. */
  public ApiResponse trackProduct(TrackProductParams params, RequestOptions options) {
    return call("track_product", params == null ? Map.of() : params.toMap(), options);
  }

  /**
   * Change alerts and checks. Target price, drop alerts, check interval, or pause. Send only what changes.
   *
   * <p>{@code PATCH /products/{id}}
   */
  public ApiResponse updateProduct(UpdateProductParams params) {
    return updateProduct(params, RequestOptions.defaults());
  }

  /** {@code PATCH /products/{id}} with per-request options. */
  public ApiResponse updateProduct(UpdateProductParams params, RequestOptions options) {
    return call("update_product", params == null ? Map.of() : params.toMap(), options);
  }

  private ApiResponse call(String name, Map<String, Object> params, RequestOptions options) {
    Endpoints.Endpoint endpoint = Endpoints.ALL.get(name);
    RequestOptions opts = options == null ? RequestOptions.defaults() : options;
    String method = opts.method() == null ? endpoint.methods().get(0) : opts.method();
    if (!endpoint.methods().contains(method)) {
      throw new AsinConfigurationException(
          endpoint.path() + " supports " + String.join(", ", endpoint.methods()) + ", not " + method + ".");
    }
    for (String required : endpoint.required()) {
      if (params.get(required) == null) {
        throw new AsinSerializationException("The \"" + required + "\" parameter is required.", required);
      }
    }
    return send(method, endpoint.path(), params, opts, endpoint.json());
  }

  private ApiResponse send(
      String method, String path, Map<String, ?> params, RequestOptions options, boolean expectJson) {
    if (!List.of("GET", "POST", "PATCH", "DELETE").contains(method)) {
      throw new AsinConfigurationException("Unsupported HTTP method: " + method + ".");
    }
    if (path == null || !path.startsWith("/")) {
      throw new AsinConfigurationException("The request path must start with \"/\".");
    }
    RequestOptions opts = options == null ? RequestOptions.defaults() : options;
    Serialization.Serialized serialized = Serialization.serialize(method, path, params);
    String url = baseUrl + serialized.path() + (serialized.query().isEmpty() ? "" : "?" + serialized.query());

    Map<String, String> requestHeaders = new LinkedHashMap<>();
    requestHeaders.put("Accept", "application/json, */*;q=0.8");
    requestHeaders.put("User-Agent", "asin-java/" + VERSION);
    requestHeaders.putAll(headers);
    requestHeaders.putAll(opts.headers());
    requestHeaders.put("Authorization", "Bearer " + apiKey);
    byte[] body = null;
    if (serialized.body() != null) {
      requestHeaders.put("Content-Type", "application/json");
      body = Serialization.toJson(serialized.body());
    }
    Duration requestTimeout = opts.timeout() != null ? opts.timeout() : timeout;

    int retries = opts.maxRetries() != null ? Math.max(opts.maxRetries(), 0) : maxRetries;
    RawResponse raw;
    for (int attempt = 0; ; attempt++) {
      String retryAfter = null;
      try {
        raw = transport.send(new AsinTransport.Request(method, URI.create(url), requestHeaders, body, requestTimeout));
        if (raw.status() >= 200 && raw.status() <= 299) {
          break;
        }
        if (attempt >= retries || !isRetryable(raw)) {
          throw apiException(raw);
        }
        List<String> values = raw.headers().get("retry-after");
        retryAfter = values == null || values.isEmpty() ? null : values.get(0);
      } catch (HttpTimeoutException e) {
        throw new AsinTransportException("Asin request timed out.", e);
      } catch (IOException e) {
        if (attempt >= retries) {
          throw new AsinTransportException("Asin request failed before an HTTP response was received.", e);
        }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new AsinTransportException("Asin request was interrupted.", e);
      }
      pause(retryDelay(attempt, retryAfter));
    }
    ApiResponse response = new ApiResponse(raw);
    if (response.isJson()) {
      try {
        response.json();
      } catch (AsinResponseDecodingException e) {
        throw new AsinResponseDecodingException(
            "Asin returned a successful JSON response that could not be decoded.", raw, e);
      }
    } else if (expectJson) {
      throw new AsinResponseDecodingException(
          "Asin returned a successful non-JSON response for a JSON operation.", raw, null);
    }
    return response;
  }

  private static boolean isRetryable(RawResponse raw) {
    return RETRYABLE_STATUSES.contains(raw.status());
  }

  private Duration retryDelay(int attempt, String retryAfter) {
    if (retryAfter != null) {
      try {
        double seconds = Double.parseDouble(retryAfter.trim());
        if (seconds >= 0) {
          return Duration.ofMillis((long) (Math.min(seconds, 60) * 1000));
        }
      } catch (NumberFormatException e) {
        // An HTTP date: fall back to exponential backoff.
      }
    }
    double millis = retryDelay.toMillis() * Math.pow(RETRY_BACKOFF_FACTOR, attempt);
    return Duration.ofMillis((long) Math.min(millis, MAX_RETRY_DELAY.toMillis()));
  }

  private static void pause(Duration delay) {
    try {
      Thread.sleep(delay.toMillis());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new AsinTransportException("Asin request was interrupted while waiting to retry.", e);
    }
  }

  /**
   * Runs {@code task} for every item on {@code concurrency} threads and returns one {@link BatchResult} per
   * item, in input order. A failing item never stops the others; the client is thread-safe.
   *
   * <pre>{@code
   * List<BatchResult<ApiResponse>> results = client.batch(ids, 5, id -> client.someEndpoint(...));
   * }</pre>
   */
  public <I, T> List<BatchResult<T>> batch(Collection<? extends I> items, int concurrency, Function<? super I, ? extends T> task) {
    if (concurrency < 1) {
      throw new AsinConfigurationException("concurrency must be a positive integer.");
    }
    List<I> list = new ArrayList<>(items);
    if (list.isEmpty()) {
      return List.of();
    }
    ExecutorService pool = Executors.newFixedThreadPool(Math.min(concurrency, list.size()), runnable -> {
      Thread thread = new Thread(runnable, "asin-batch");
      thread.setDaemon(true);
      return thread;
    });
    try {
      List<Future<T>> futures = new ArrayList<>(list.size());
      for (I item : list) {
        futures.add(pool.submit(() -> task.apply(item)));
      }
      List<BatchResult<T>> results = new ArrayList<>(list.size());
      for (int i = 0; i < futures.size(); i++) {
        try {
          results.add(new BatchResult<>(i, futures.get(i).get(), null));
        } catch (ExecutionException e) {
          results.add(new BatchResult<>(i, null, e.getCause()));
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          throw new AsinTransportException("batch was interrupted.", e);
        }
      }
      return results;
    } finally {
      pool.shutdownNow();
    }
  }

  /** {@link #batch(Collection, int, Function)} with {@link #DEFAULT_CONCURRENCY} threads. */
  public <I, T> List<BatchResult<T>> batch(Collection<? extends I> items, Function<? super I, ? extends T> task) {
    return batch(items, DEFAULT_CONCURRENCY, task);
  }

  private static AsinApiException apiException(RawResponse raw) {
    JsonNode body = null;
    String message = null;
    try {
      body = Serialization.MAPPER.readTree(raw.body());
    } catch (IOException e) {
      body = null;
    }
    if (body != null && body.isObject()) {
      // The API answers {"error": {"code", "message"}}.
      JsonNode nested = body.path("error").path("message");
      if (nested.isTextual()) {
        message = nested.asText();
      }
      for (String key : ERROR_MESSAGE_KEYS) {
        JsonNode value = body.get(key);
        if (value != null && value.isTextual() && !value.asText().isEmpty()) {
          message = value.asText();
          break;
        }
      }
    }
    return new AsinApiException(raw, body, message);
  }

  /** Builder for {@link AsinClient}. */
  public static final class Builder {
    private String apiKey = System.getenv(API_KEY_ENVIRONMENT_VARIABLE);
    private String baseUrl = DEFAULT_BASE_URL;
    private Duration timeout = DEFAULT_TIMEOUT;
    private final Map<String, String> headers = new LinkedHashMap<>();
    private AsinTransport transport;
    private int maxRetries = MAX_RETRIES;
    private Duration retryDelay = DEFAULT_RETRY_DELAY;

    private Builder() {}

    /** Retries after a network error or a 408/429/5xx response; 0 disables retries. Default 3. */
    public Builder maxRetries(int maxRetries) {
      this.maxRetries = maxRetries;
      return this;
    }

    /** Delay before the first retry, growing 1.5x per retry. Default 1 second. */
    public Builder retryDelay(Duration retryDelay) {
      this.retryDelay = retryDelay;
      return this;
    }

    /** API key sent as a Bearer token in the {@code Authorization} header. */
    public Builder apiKey(String apiKey) {
      this.apiKey = apiKey;
      return this;
    }

    /** API origin, for tests or proxies. */
    public Builder baseUrl(String baseUrl) {
      this.baseUrl = Objects.requireNonNull(baseUrl, "baseUrl");
      return this;
    }

    /** Client-side deadline per request; null disables it. */
    public Builder timeout(Duration timeout) {
      this.timeout = timeout;
      return this;
    }

    /** Header sent with every request. */
    public Builder header(String name, String value) {
      headers.put(name, value);
      return this;
    }

    /** Custom transport; defaults to {@link java.net.http.HttpClient} without redirects. */
    public Builder transport(AsinTransport transport) {
      this.transport = transport;
      return this;
    }

    /** Validates the configuration and creates the client. */
    public AsinClient build() {
      return new AsinClient(this);
    }
  }
}
