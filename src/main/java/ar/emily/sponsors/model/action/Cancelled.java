package ar.emily.sponsors.model.action;

import ar.emily.sponsors.model.Sponsor;
import ar.emily.sponsors.model.Sponsorship;
import discord4j.discordjson.json.EmbedAuthorData;
import discord4j.discordjson.json.EmbedData;
import discord4j.discordjson.json.EmbedFieldData;
import discord4j.rest.util.Color;

public record Cancelled(Sponsorship sponsorship) implements Action {

  @Override
  public EmbedData asDiscordEmbed() {
    final Sponsor sponsor = this.sponsorship.sponsor();
    return EmbedData.builder()
        .color(Color.RED.getRGB())
        .title("Sponsorship Cancelled")
        .description(this.sponsorship.tier().name())
        .addField(
            EmbedFieldData.builder()
                .name("Created:")
                .value(PRETTY_DATE_FORMATTER.format(this.sponsorship.createdAt()))
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
