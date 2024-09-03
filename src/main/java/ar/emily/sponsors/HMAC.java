package ar.emily.sponsors;

import io.netty.buffer.ByteBuf;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public final class HMAC {

  private static final Map<String, Mac> MAC_CACHE = new ConcurrentHashMap<>();

  public static byte[] hash(final String algorithm, final byte[] key, final byte[] data) {
    return new HMAC(algorithm, key).update(data).doFinal();
  }

  private static Mac getMac(final String algorithm) throws NoSuchAlgorithmException, CloneNotSupportedException {
    final Mac mac = MAC_CACHE.computeIfAbsent(algorithm.toLowerCase(Locale.ROOT), algo -> {
      try {
        return Mac.getInstance(algo);
      } catch (final NoSuchAlgorithmException exception) {
        return sneakyThrow(exception);
      }
    });

    return (Mac) mac.clone();
  }

  @SuppressWarnings("unchecked")
  private static <X extends Throwable, R> R sneakyThrow(final Throwable ex) throws X {
    throw (X) ex;
  }

  private final Mac mac;

  public HMAC(final String algorithm, final byte[] key) {
    Objects.requireNonNull(algorithm, "algorithm");
    Objects.requireNonNull(key, "key");

    try {
      this.mac = getMac(algorithm);
      this.mac.init(new SecretKeySpec(key, algorithm));
    } catch (final InvalidKeyException | NoSuchAlgorithmException | CloneNotSupportedException exception) {
      throw new RuntimeException(exception.getMessage(), exception);
    }
  }

  public HMAC update(final byte[] data) {
    this.mac.update(data);
    return this;
  }

  public HMAC update(final ByteBuffer data) {
    this.mac.update(data);
    return this;
  }

  public HMAC update(final ByteBuf data) {
    return update(data.nioBuffer());
  }

  public byte[] doFinal() {
    return this.mac.doFinal();
  }
}
