package ar.emily.sponsors.model.action;

import ar.emily.sponsors.model.Sponsor;
import ar.emily.sponsors.model.Sponsorship;
import com.fasterxml.jackson.databind.JsonNode;
import discord4j.discordjson.json.EmbedAuthorData;
import discord4j.discordjson.json.EmbedData;
import discord4j.discordjson.json.EmbedFieldData;
import discord4j.rest.util.Color;

import java.time.LocalDateTime;

public record PendingCancellation(Sponsorship sponsorship, LocalDateTime effectiveDate) implements Action {

  @Override
  public EmbedData asDiscordEmbed() {
    final Sponsor sponsor = this.sponsorship.sponsor();
    return EmbedData.builder()
        .color(Color.TAHITI_GOLD.getRGB())
        .title("Pending Sponsorship Cancellation")
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
        ).author(
            EmbedAuthorData.builder()
                .name(sponsor.login())
                .urlOrNull(sponsor.htmlUrl().orElse(null))
                .iconUrl(sponsor.avatarUrl())
                .build()
        ).build();
  }
}
