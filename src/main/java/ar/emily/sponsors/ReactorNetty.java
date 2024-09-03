package ar.emily.sponsors;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Function;

public final class ReactorNetty {

  public static Mono<ByteBuf> concatBuffers(final Flux<? extends ByteBuf> source) {
    return source.reduceWith(
        Unpooled::compositeBuffer,
        (composite, buff) -> composite.addComponent(true, buff)
    ).map(Function.identity()); // Mono<CompositeByteBuf> isn't convertible to Mono<ByteBuf>, woo
  }

  public static Mono<byte[]> hmac(final Flux<? extends ByteBuf> source, final String algorithm, final byte[] key) {
    return source.reduceWith(() -> new HMAC(algorithm, key), HMAC::update).map(HMAC::doFinal);
  }

  private ReactorNetty() {
  }
}
