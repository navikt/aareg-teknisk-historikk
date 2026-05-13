package no.nav.aareg.teknisk.historikk.provider.api.contract;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum Endringskilde {
    @JsonProperty("saksbehandler")
    SAKSBEHANDLER,

    @JsonProperty("a-ordningen")
    A_ORDNINGEN,

    @JsonProperty("foer-a-ordningen")
    FOER_A_ORDNINGEN,

    @JsonProperty("gjenoppbygging")
    GJENOPPBYGGING,

    @JsonProperty("maskinelt-avsluttet")
    MASKINELT_AVSLUTTET,

    @JsonProperty("patch")
    PATCH,

    @JsonProperty("identbytte")
    IDENTBYTTE
}

