package ar.emily.sponsors;

import org.tinylog.Logger;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import sun.misc.Signal;

import java.util.stream.Stream;

public final class ShutdownProcedure {

  public static Mono<Void> setupShutdownSignal() {
    final Sinks.Empty<Void> killSink = Sinks.empty();
    Signal.handle(new Signal("INT"), signal -> killSink.tryEmitEmpty());
    Signal.handle(new Signal("TERM"), signal -> killSink.tryEmitEmpty());

    final Thread t =
        new Thread(() -> {
          Stream.generate(System.console()::readLine)
              .anyMatch("shutdown"::equals); // result is ignored as it can only ever be true, the stream is infinite
          killSink.tryEmitEmpty();
        }, "CommandProcessor");
    t.setPriority(Thread.MIN_PRIORITY);
    t.setDaemon(true);
    t.start();

    return killSink.asMono().then(Mono.fromRunnable(() -> Logger.info("Shutting down...")));
  }

  private ShutdownProcedure() {
  }
}
