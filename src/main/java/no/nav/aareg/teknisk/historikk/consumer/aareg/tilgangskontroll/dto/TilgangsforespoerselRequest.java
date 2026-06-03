package no.nav.aareg.teknisk.historikk.consumer.aareg.tilgangskontroll.dto;

import java.util.Set;

public record TilgangsforespoerselRequest(
        Kontekst kontekst,
        Operasjon operasjon,
        Set<ForespurtArbeidstaker> forespurteArbeidstakere
) {
}
