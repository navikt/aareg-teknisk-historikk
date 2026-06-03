package no.nav.aareg.teknisk.historikk.provider.api.contract;

import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TekniskHistorikkMapper {

    private final Tracer tracer;

    public TekniskHistorikkResponse map(no.nav.aareg.kontrakter.teknisk.historikk.TekniskHistorikkResponse domeneRespons) {
        var apiArbeidsforhold = new ArrayList<Arbeidsforhold>();
        domeneRespons.tekniskHistorikk().forEach(th -> {
            apiArbeidsforhold.add(new Arbeidsforhold(
                    th.arbeidsforholdId(),
                    th.arbeidsforholdType(),
                    mapArbeidssted(th.arbeidsstedId()),
                    mapOpplysningspliktig(th.opplysningspliktigId()),
                    mapArbeidstaker(th.arbeidstakerId()),
                    th.ansattFra(),
                    th.ansattTil(),
                    th.type(),
                    th.datoEndret()
            ));
        });
        return new TekniskHistorikkResponse(apiArbeidsforhold, List.of(), tracer.currentSpan().context().traceId());
    }

    private Identifikator mapOpplysningspliktig(String opplysningspliktigId) {
        if (opplysningspliktigId == null) {
            return null;
        } else if (opplysningspliktigId.length() == 11) {
            return Person.builder().offentligIdent(opplysningspliktigId).build();
        } else {
            return Hovedenhet.builder().offentligIdent(opplysningspliktigId).build();
        }
    }

    private Identifikator mapArbeidssted(String arbeidsstedId) {
        if (arbeidsstedId == null) {
            return null;
        } else if (arbeidsstedId.length() == 11) {
            return Person.builder().offentligIdent(arbeidsstedId).build();
        } else {
            return Underenhet.builder().offentligIdent(arbeidsstedId).build();
        }
    }

    private Person mapArbeidstaker(String arbeidstakerId) {
        if (arbeidstakerId == null) {
            return null;
        }
        return Person.builder().offentligIdent(arbeidstakerId).build();
    }

    public TekniskHistorikkResponse map(String feilmelding) {
        return new TekniskHistorikkResponse(List.of(), List.of(feilmelding), tracer.currentSpan().context().traceId());
    }
}
