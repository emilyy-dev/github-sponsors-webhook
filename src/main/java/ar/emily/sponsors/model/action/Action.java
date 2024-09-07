package ar.emily.sponsors.model.action;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import discord4j.discordjson.json.EmbedData;
import discord4j.discordjson.json.WebhookExecuteRequest;
import discord4j.rest.util.MultipartRequest;

import java.time.format.DateTimeFormatter;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "action", visible = true, defaultImpl = Unknown.class)
@JsonSubTypes(
    {
        @JsonSubTypes.Type(Cancelled.class),
        @JsonSubTypes.Type(Created.class),
        @JsonSubTypes.Type(Edited.class),
        @JsonSubTypes.Type(PendingCancellation.class),
        @JsonSubTypes.Type(PendingTierChange.class),
        @JsonSubTypes.Type(TierChanged.class)
    }
)
public sealed interface Action
    permits Cancelled, Created, Edited, PendingCancellation, PendingTierChange, TierChanged, Unknown {

  DateTimeFormatter PRETTY_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");

  default MultipartRequest<WebhookExecuteRequest> asDiscordMultipartRequest() {
    return MultipartRequest.ofRequest(WebhookExecuteRequest.builder().addEmbed(asDiscordEmbed()).build());
  }

  EmbedData asDiscordEmbed();
}
