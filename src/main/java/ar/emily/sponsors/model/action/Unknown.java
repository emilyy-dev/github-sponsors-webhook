package ar.emily.sponsors.model.action;

import ar.emily.sponsors.WebhookEndpoint;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import discord4j.discordjson.json.EmbedData;
import discord4j.discordjson.json.WebhookExecuteRequest;
import discord4j.rest.util.MultipartRequest;
import reactor.util.function.Tuples;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

// @JsonAnySetter doesn't work with @JsonCreate (same mechanism used for record constructors), fixed in 2.18
@JsonDeserialize(builder = Unknown.BuilderProxy.class)
public record Unknown(String action, Map<String, Object> rest) implements Action {

  @Override
  public MultipartRequest<WebhookExecuteRequest> asDiscordMultipartRequest() {
    String jsonRest;
    try {
      jsonRest = WebhookEndpoint.MAPPER.writeValueAsString(this.rest);
    } catch (final JsonProcessingException ex) {
      jsonRest = "Unable to process json";
    }

    return MultipartRequest.ofRequestAndFiles(
        WebhookExecuteRequest.builder().addEmbed(asDiscordEmbed()).build(),
        List.of(Tuples.of(this.action, new ByteArrayInputStream(jsonRest.getBytes(StandardCharsets.UTF_8))))
    );
  }

  @Override
  public EmbedData asDiscordEmbed() {
    return EmbedData.builder().title("Unknown Sponsorship Event").build();
  }

  @JsonPOJOBuilder
  public static final class BuilderProxy {

    public String action;
    @JsonAnySetter public Map<String, Object> rest;

    public Unknown build() {
      return new Unknown(this.action, this.rest);
    }
  }
}
