package ar.emily.sponsors.model.action;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import discord4j.discordjson.json.EmbedData;
import discord4j.discordjson.json.WebhookExecuteRequest;
import discord4j.rest.util.MultipartRequest;

import java.time.format.DateTimeFormatter;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "action", defaultImpl = Unknown.class)
@JsonSubTypes(
    {
        @JsonSubTypes.Type(value = Cancelled.class, name = "cancelled"),
        @JsonSubTypes.Type(value = Created.class, name = "created"),
        @JsonSubTypes.Type(value = Edited.class, name = "edited"),
        @JsonSubTypes.Type(value = PendingCancellation.class, name = "pending_cancellation"),
        @JsonSubTypes.Type(value = PendingTierChange.class, name = "pending_tier_change"),
        @JsonSubTypes.Type(value = TierChange.class, name = "tier_change")
    }
)
public interface Action {

  DateTimeFormatter PRETTY_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");

  default MultipartRequest<WebhookExecuteRequest> asDiscordMultipartRequest() {
    return MultipartRequest.ofRequest(WebhookExecuteRequest.builder().addEmbed(asDiscordEmbed()).build());
  }

  EmbedData asDiscordEmbed();
}
