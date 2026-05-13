package no.nav.aareg.teknisk.historikk.consumer.texas.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TokenRequest(
        @JsonProperty("identity_provider") String identityProvider,
        String target
) {
}
