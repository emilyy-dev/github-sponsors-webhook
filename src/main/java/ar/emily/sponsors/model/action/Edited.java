package ar.emily.sponsors.model.action;

import ar.emily.sponsors.model.Sponsor;
import ar.emily.sponsors.model.Sponsorship;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.JsonNode;
import discord4j.discordjson.json.EmbedAuthorData;
import discord4j.discordjson.json.EmbedData;
import discord4j.discordjson.json.EmbedFieldData;
import discord4j.rest.util.Color;

@JsonTypeName("edited")
public record Edited(Sponsorship sponsorship, Changes changes) implements Action {

  @Override
  public EmbedData asDiscordEmbed() {
    final Sponsor sponsor = this.sponsorship.sponsor();
    return EmbedData.builder()
        .color(Color.GRAY_CHATEAU.getRGB())
        .title("Sponsorship Edited")
        .description(this.sponsorship.tier().name())
        .addField(
            EmbedFieldData.builder()
                .name("Privacy Level:")
                .value("%s → %s".formatted(this.changes.privacyLevel().from(), this.sponsorship.privacyLevel()))
                .build()
        ).author(
            EmbedAuthorData.builder()
                .name(sponsor.login())
                .urlOrNull(sponsor.htmlUrl().orElse(null))
                .iconUrl(sponsor.avatarUrl())
                .build()
        ).build();
  }

  public record Changes(PrivacyLevel privacyLevel) {
  }

  public record PrivacyLevel(String from) {
  }
}
