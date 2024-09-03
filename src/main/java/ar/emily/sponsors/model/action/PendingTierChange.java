package ar.emily.sponsors.model.action;

import ar.emily.sponsors.model.Sponsor;
import ar.emily.sponsors.model.Sponsorship;
import ar.emily.sponsors.model.Tier;
import discord4j.discordjson.json.EmbedAuthorData;
import discord4j.discordjson.json.EmbedData;
import discord4j.discordjson.json.EmbedFieldData;
import discord4j.rest.util.Color;

import java.time.LocalDateTime;

public record PendingTierChange(
    Sponsorship sponsorship,
    LocalDateTime effectiveDate,
    Changes changes
) implements Action {

  @Override
  public EmbedData asDiscordEmbed() {
    final Sponsor sponsor = this.sponsorship.sponsor();
    return EmbedData.builder()
        .color(Color.DEEP_LILAC.getRGB())
        .title("Pending Sponsorship Tier Change")
        .description(this.sponsorship.tier().name())
        .addField(
            EmbedFieldData.builder()
                .name("Created:")
                .value(PRETTY_DATE_FORMATTER.format(this.sponsorship.createdAt()))
                .build()
        ).addField(
            EmbedFieldData.builder()
                .name("Effective Date:")
                .value(PRETTY_DATE_FORMATTER.format(this.effectiveDate))
                .build()
        ).addField(
            EmbedFieldData.builder()
                .name("Tier:")
                .value("%s → %s".formatted(this.changes.tier().from().name(), this.sponsorship.tier().name()))
                .build()
        ).author(
            EmbedAuthorData.builder()
                .name(sponsor.login())
                .urlOrNull(sponsor.htmlUrl().orElse(null))
                .iconUrl(sponsor.avatarUrl())
                .build()
        ).build();
  }

  public record Changes(TierChange tier) {

    public record TierChange(Tier from) {
    }
  }
}
