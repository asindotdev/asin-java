# Asin Java SDK

The official Java SDK for the [Asin](https://www.asin.dev) Amazon price tracking API.

Track Amazon prices and products with a scheduled watchlist, price history, offers and alerts.

The SDK exposes **11 typed methods**, one per documented endpoint, each with a parameter builder, plus a generic `request()`. It uses `java.net.http.HttpClient` and Jackson.

## Requirements

- Java 17 or newer

## Installation

Maven:

```xml
<dependency>
  <groupId>dev.asin</groupId>
  <artifactId>asin</artifactId>
  <version>0.1.0</version>
</dependency>
```

Gradle:

```kotlin
implementation("dev.asin:asin:0.1.0")
```

## Get your API key

[Sign up for Asin](https://www.asin.dev/signup) or sign in, then copy your API key from the [dashboard](https://www.asin.dev/app). `AsinClient.fromEnv()` reads the `ASIN_API_KEY` environment variable.

## Quick start

```java
import dev.asin.ApiResponse;
import dev.asin.AsinClient;
import dev.asin.params.SearchAmazonParams;

AsinClient client = AsinClient.fromEnv();
ApiResponse response = client.searchAmazon(SearchAmazonParams.builder().query("wireless earbuds").build());
System.out.println(response.json().toPrettyString());
```

Parameter builders mirror the [API reference](https://www.asin.dev/docs); `build()` checks required parameters. Use `response.as(MyType.class)` to map the JSON onto your own types.

## HTTP methods

Each method uses the HTTP verb of its endpoint. `GET` and `DELETE` send parameters in the query string; `POST` and `PATCH` send them as a JSON body, except `wait` on `/products/{id}/check`, which stays in the query string. Null values are never sent, so `target_price` cannot be removed through the SDK yet.

## Any endpoint

```java
ApiResponse response = client.request("GET", "/account", Map.of());
```

## Errors

```java
try {
  client.searchAmazon(SearchAmazonParams.builder().query("wireless earbuds").build());
} catch (AsinApiException e) {
  System.err.println(e.status() + ": " + e.getMessage());
}
```

| Exception | When |
| --- | --- |
| `AsinConfigurationException` | Missing API key, invalid options or unsupported method. |
| `AsinSerializationException` | A required parameter is missing or a value can't be sent. |
| `AsinTransportException` | Network failure, timeout or interruption. |
| `AsinApiException` | Non-2xx status (`status()`, `errorMessage()`, `responseBody()`, `rawResponse()`). |
| `AsinResponseDecodingException` | A success response could not be decoded. |

All of them extend the unchecked `AsinException`.

## Retries

Requests are retried up to 3 times after a network error, a `408`, `429`, `500`, `502`, `503` or `504`. Other statuses, such as `400`, `401`, `402` or `404`, and client-side timeouts are not retried. The delay starts at 1 second and grows 1.5× per retry (capped at 30 seconds); a `Retry-After` header wins.

```java
AsinClient client = AsinClient.builder().maxRetries(5).retryDelay(Duration.ofMillis(500)).build();
client.searchAmazon(SearchAmazonParams.builder().query("wireless earbuds").build(), RequestOptions.builder().maxRetries(0).build()); // one request without retries
```

## Batch requests

`batch()` runs a function per item on 10 threads (`concurrency`) and returns one `BatchResult` per item, in input order. A failing item never stops the others.

```java
List<String> inputs = List.of("wireless earbuds", "mechanical keyboard", "usb-c hub");
List<BatchResult<ApiResponse>> results = client.batch(inputs, 5, value ->
    client.searchAmazon(SearchAmazonParams.builder().query(value).build()));
for (BatchResult<ApiResponse> result : results) {
  if (result.ok()) {
    System.out.println(result.value().json());
  } else {
    System.err.println(inputs.get(result.index()) + ": " + result.error().getMessage());
  }
}
```

| Constant | Default | Description |
| --- | --- | --- |
| `AsinClient.DEFAULT_CONCURRENCY` | `10` | Tasks run at the same time by batch |
| `AsinClient.DEFAULT_TIMEOUT` | `Duration.ofSeconds(120)` | Request timeout |
| `AsinClient.MAX_RETRIES` | `3` | Retry attempts after a network error, a 408/429/5xx |
| `AsinClient.RETRY_BACKOFF_FACTOR` | `1.5` | Exponential backoff multiplier |
| `AsinClient.DEFAULT_RETRY_DELAY` | `Duration.ofSeconds(1)` | Delay before the first retry |

## Configuration

```java
AsinClient client = AsinClient.builder()
    .apiKey(System.getenv("ASIN_API_KEY"))
    .timeout(Duration.ofSeconds(60)) // default 120 s, null disables
    .header("X-Trace", "abc")
    .transport(AsinTransport.fromHttpClient(myHttpClient)) // optional
    .build();
```

## Endpoints

11 methods, generated from the [OpenAPI document](https://www.asin.dev/openapi.json).

| Method | HTTP | Description |
| --- | --- | --- |
| `checkProduct()` | POST `/products/{id}/check` | Asks for a check right away (1 credit), at most once per 5 minutes; earlier calls return the product unchanged. A paused product has to be… |
| `deleteProduct()` | DELETE `/products/{id}` | Removes the product and its price history. |
| `fetchOffers()` | POST `/offers` | Reads the offers on Amazon for 1 credit, refunded if it doesn't answer. A read made in the last 3 hours, by anyone, is returned instead, fo… |
| `getAccount()` | GET `/account` | Your plan, the credits left (monthly allowance and packs), how many products you track and the credits they use per month. |
| `getAsinHistory()` | GET `/history` | Every price Asin read for an ASIN on a marketplace over the period, whoever tracks it, oldest first, with the lowest, highest and average o… |
| `getOffers()` | GET `/offers` | Who sells the product and at what price, as last read, cheapest first, with Amazon's own price and the cheapest new and used offers (used i… |
| `getProduct()` | GET `/products/{id}` | The product's latest state and every price read of the period, oldest first. The history covers every read of this ASIN on this marketplace… |
| `listProducts()` | GET `/products` | Your watchlist, newest first, with each product's latest state and its recent prices (trend). Filter with asin (and marketplace) to know if… |
| `searchAmazon()` | GET `/search` | Amazon search results (up to 60, sponsored included), to find the ASIN of a product. Free, limited to 30 searches per hour. |
| `trackProduct()` | POST `/products` | Adds an ASIN, an Amazon link or a short link (amzn.to, a.co…) to your watchlist. The first check runs in the background within seconds (che… |
| `updateProduct()` | PATCH `/products/{id}` | Target price, drop alerts, check interval, or pause. Send only what changes. |

## License

MIT — see [LICENSE](LICENSE).
