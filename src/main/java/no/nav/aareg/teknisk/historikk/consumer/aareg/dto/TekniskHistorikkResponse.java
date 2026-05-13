package no.nav.aareg.teknisk.historikk.consumer.aareg.dto;

import no.nav.aareg.teknisk.historikk.provider.api.contract.Arbeidsforhold;

import java.util.List;

public record TekniskHistorikkResponse(List<Arbeidsforhold> arbeidsforholdListe) {
}
