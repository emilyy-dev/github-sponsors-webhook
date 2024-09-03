package ar.emily.sponsors;

import ar.emily.sponsors.model.action.Action;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.tinylog.Logger;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;
import reactor.netty.http.server.HttpServerRoutes;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;
import java.util.function.Consumer;

public final class WebhookEndpoint implements Consumer<HttpServerRoutes> {

  public static final JsonMapper MAPPER =
      JsonMapper.builder()
          .enable(SerializationFeature.INDENT_OUTPUT)
          // models are not complete as not all fields are needed
          .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
          .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
          .addModules(
              new JavaTimeModule().addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ISO_DATE_TIME)),
              new Jdk8Module()
          ).build();

  private static final String HUB_SIGNATURE = "X-Hub-Signature-256";
  private static final String PREFIX = "sha256=";
  private static final byte[] GITHUB_WEBHOOK_SECRET = EnvVars.env("GITHUB_WEBHOOK_SECRET").getBytes(StandardCharsets.UTF_8);

  public static WebhookEndpoint create() {
    return new WebhookEndpoint();
  }

  private static String truncate(final String str, final int maxLength) {
    if (str.length() <= maxLength) {
      return str;
    } else {
      return str.substring(0, maxLength).concat("...");
    }
  }

  private final Sinks.Many<Action> jsonSink = Sinks.many().multicast().onBackpressureBuffer(16);

  private WebhookEndpoint() {
  }

  public Flux<Action> actionSource() {
    return this.jsonSink.asFlux();
  }

  @Override
  public void accept(final HttpServerRoutes routes) {
    routes.post("/", this::processRequest);
  }

  private Mono<Void> processRequest(final HttpServerRequest request, final HttpServerResponse response) {
    final String hubSignature = request.requestHeaders().get(HUB_SIGNATURE);
    if (hubSignature == null) {
      Logger.warn("Missing signature from {}", request.remoteAddress());
      return response.status(HttpResponseStatus.BAD_REQUEST).send();
    }

    if (!hubSignature.startsWith(PREFIX)) {
      Logger.warn("Invalid signature from {} - {}", request.remoteAddress(), truncate(hubSignature, 16));
      return response.status(HttpResponseStatus.BAD_REQUEST).send();
    }

    final Flux<ByteBuf> bodyFlux = request.receive().retain().cache();
    return ReactorNetty.hmac(bodyFlux, "HmacSHA256", GITHUB_WEBHOOK_SECRET).flatMap(signature -> {
      final byte[] signatureReceived = HexFormat.of().parseHex(hubSignature, PREFIX.length(), hubSignature.length());
      if (!MessageDigest.isEqual(signatureReceived, signature)) {
        Logger.warn("Invalid signature from {}", request.remoteAddress());
        Logger.warn("Received: {}", truncate(hubSignature, 16));
        Logger.warn("Calculated: {}", HexFormat.of().formatHex(signature));
        return response.status(HttpResponseStatus.BAD_REQUEST).send();
      }

      return ReactorNetty.concatBuffers(bodyFlux).<Void>handle((body, sink) -> {
        try {
          try (final InputStream in = new ByteBufInputStream(body, true)) {
            this.jsonSink.tryEmitNext(MAPPER.readValue(in, Action.class));
          }

          sink.complete();
        } catch (final IOException ex) {
          sink.error(ex);
        }
      }).then(response.status(HttpResponseStatus.NO_CONTENT).send());

    });
  }
}
