package ar.emily.sponsors;

import ar.emily.sponsors.model.action.Action;
import discord4j.rest.RestClient;
import discord4j.rest.service.WebhookService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public final class DiscordSponsorsWebhook {

  private static final String DISCORD_BOT_TOKEN = EnvVars.env("DISCORD_BOT_TOKEN");
  private static final long DISCORD_WEBHOOK_ID = Long.parseUnsignedLong(EnvVars.env("DISCORD_WEBHOOK_ID"));
  private static final String DISCORD_WEBHOOK_TOKEN = EnvVars.env("DISCORD_WEBHOOK_TOKEN");

  private static final WebhookService WEBHOOK_SERVICE = RestClient.create(DISCORD_BOT_TOKEN).getWebhookService();

  public static Mono<Void> create(final Flux<? extends Action> jsonSource) {
    return jsonSource.map(Action::asDiscordMultipartRequest)
        .flatMap(request -> WEBHOOK_SERVICE.executeWebhook(DISCORD_WEBHOOK_ID, DISCORD_WEBHOOK_TOKEN, false, request))
        .then();
  }

  private DiscordSponsorsWebhook() {
  }
}
