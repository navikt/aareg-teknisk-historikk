package no.nav.aareg.teknisk.historikk.consumer.aareg.tilgangskontroll.dto;

import java.util.List;

public record TilgangsforespoerselResponse(List<Tilgang> tilganger) {
}
