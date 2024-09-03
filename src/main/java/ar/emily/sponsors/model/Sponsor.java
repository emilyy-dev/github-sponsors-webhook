package ar.emily.sponsors.model;

import java.util.Optional;

public record Sponsor(
    String avatarUrl,
    Optional<String> htmlUrl,
    String login
) {
}
