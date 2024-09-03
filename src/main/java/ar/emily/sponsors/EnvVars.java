package ar.emily.sponsors;

import java.util.Objects;

public interface EnvVars {

  static String env(String s) {
    s = "GITHUB_SPONSORS_WEBHOOK_".concat(s);
    return Objects.requireNonNull(System.getenv(s), s);
  }
}
