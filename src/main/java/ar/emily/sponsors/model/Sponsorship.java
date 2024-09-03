package ar.emily.sponsors.model;

import java.time.LocalDateTime;

public record Sponsorship(
    LocalDateTime createdAt,
    String privacyLevel,
    Sponsor sponsor,
    Tier tier
) {
}
