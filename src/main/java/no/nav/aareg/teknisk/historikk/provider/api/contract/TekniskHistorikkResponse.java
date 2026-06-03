package no.nav.aareg.teknisk.historikk.provider.api.contract;

import java.util.List;

public record TekniskHistorikkResponse(List<Arbeidsforhold> arbeidsforhold, List<String> feilmeldinger, String traceId) {
}
