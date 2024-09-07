package ar.emily.sponsors;

import io.netty.channel.unix.DomainSocketAddress;
import org.tinylog.Logger;
import org.tinylog.jul.JulTinylogBridge;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServer;

import java.io.File;
import java.util.Objects;

public final class Main {

  private static final String SOCKET_PATH = EnvVars.env("SOCKET_PATH");

  static {
    Thread.setDefaultUncaughtExceptionHandler((t, e) -> Logger.error(e));
    JulTinylogBridge.activate();
  }

  public static void main(final String[] args) {
    Objects.requireNonNull(System.console(), "System.console() is unavailable");
    final Mono<Void> shutdownSignal = ShutdownProcedure.setupShutdownSignal();
    final WebhookEndpoint endpoint = WebhookEndpoint.create();

    final HttpServer httpServer =
        HttpServer.create()
            .bindAddress(() -> new DomainSocketAddress(new File(SOCKET_PATH)))
            .route(endpoint)
            .wiretap(true);
    httpServer.warmup()
        .then(httpServer.bind())
        .flatMap(server ->
            DiscordSponsorsWebhook.create(endpoint.actionSource())
                .takeUntilOther(shutdownSignal)
                .then(Mono.fromRunnable(server::dispose))
                .then(server.onDispose())
        ).doOnError(Logger::error)
        .then(Mono.fromRunnable(() -> Logger.info("Shut down")))
        .block();
  }

  private Main() {
  }
}
