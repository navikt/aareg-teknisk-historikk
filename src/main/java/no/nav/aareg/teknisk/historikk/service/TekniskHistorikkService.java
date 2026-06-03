package no.nav.aareg.teknisk.historikk.service;

import lombok.RequiredArgsConstructor;
import no.nav.aareg.kontrakter.teknisk.historikk.TekniskHistorikk;
import no.nav.aareg.kontrakter.teknisk.historikk.TekniskHistorikkResponse;
import no.nav.aareg.teknisk.historikk.Feilkode;
import no.nav.aareg.teknisk.historikk.audit.AuditLogger;
import no.nav.aareg.teknisk.historikk.consumer.aareg.services.AaregServicesConsumer;
import no.nav.aareg.teknisk.historikk.consumer.aareg.tilgangskontroll.dto.Tilgang;
import no.nav.aareg.teknisk.historikk.exception.ApplicationException;
import no.nav.aareg.teknisk.historikk.exception.IngenTilgangException;
import no.nav.aareg.teknisk.historikk.provider.api.contract.Soekeparametere;
import no.nav.aareg.teknisk.historikk.service.tilgangskontroll.TilgangskontrollService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TekniskHistorikkService {

    private final TilgangskontrollService tilgangskontrollService;
    private final AaregServicesConsumer aaregServicesConsumer;
    private final AuditLogger auditLogger;

    public TekniskHistorikkResponse hentTekniskHistorikk(Soekeparametere soekeparametere) {
        var aaregResponse = aaregServicesConsumer.hentTekniskHistorikk(soekeparametere);

        if (aaregResponse.tekniskHistorikk().isEmpty()) {
            var tilganger = tilgangskontrollService.hentTilganger(opprettDummyResponse(soekeparametere));
            var erAdressebeskyttetArbeidstaker = erAdressebeskyttetArbeidstaker(tilganger);
            var harTilgangTilArbeidstaker = harTilgangTilArbeidstaker(soekeparametere.getArbeidstaker(), tilganger);

            if (!harTilgangTilArbeidstaker) {
                auditLogger.loggOppslag(soekeparametere, false, erAdressebeskyttetArbeidstaker);
                throw new IngenTilgangException(Feilkode.AAREG_SERVICES_FORBIDDEN);
            } else {
                // FOR QA: Dette skal aldri skje - tilgangskontrollen skal alltid gi tilgang til arbeidstaker med adressebeskyttelse, ha det med eller ta det bort?
                if (harTilgangTilArbeidstaker && erAdressebeskyttetArbeidstaker) {
                    throw new ApplicationException("Feil ved tilgangskontroll - fikk tilgang til arbeidstaker som er adressebeskyttet");
                }

                auditLogger.loggOppslag(soekeparametere, true, erAdressebeskyttetArbeidstaker);
                return new TekniskHistorikkResponse(List.of());
            }
        } else {
            var tilganger = tilgangskontrollService.hentTilganger(aaregResponse.tekniskHistorikk());
            var erAdressebeskyttetArbeidstaker = erAdressebeskyttetArbeidstaker(tilganger);
            var harTilgangTilArbeidstaker = harTilgangTilArbeidstaker(soekeparametere.getArbeidstaker(), tilganger);

            if (!harTilgangTilArbeidstaker) {
                auditLogger.loggOppslag(soekeparametere, false, erAdressebeskyttetArbeidstaker);
                throw new IngenTilgangException(Feilkode.AAREG_SERVICES_FORBIDDEN);
            } else {
                // FOR QA: Dette skal aldri skje - tilgangskontrollen skal alltid gi tilgang til arbeidstaker med adressebeskyttelse, ha det med eller ta det bort?
                if (harTilgangTilArbeidstaker && erAdressebeskyttetArbeidstaker) {
                    throw new ApplicationException("Feil ved tilgangskontroll - fikk tilgang til arbeidstaker som er adressebeskyttet");
                }

                auditLogger.loggOppslag(soekeparametere, true, erAdressebeskyttetArbeidstaker);
                var filtrertResponse = aaregResponse.tekniskHistorikk().stream().filter(th -> {
                    var targetTilgang = tilganger.stream().filter(t -> t.opplysningspliktigIdentifikator().equals(th.opplysningspliktigId()) && t.arbeidstakerIdentifkator().equals(th.arbeidstakerId())).findFirst().orElseThrow();
                    return targetTilgang.harTilgang();
                }).toList();
                return new TekniskHistorikkResponse(filtrertResponse);
            }
        }
    }

    private boolean erAdressebeskyttetArbeidstaker(List<Tilgang> tilganger) {
        return tilganger.stream().anyMatch(Tilgang::adressebeskyttetArbeidstaker);
    }

    private List<TekniskHistorikk> opprettDummyResponse(Soekeparametere soekeparametere) {
        return List.of(new TekniskHistorikk(null, soekeparametere.getOpplysningspliktig(), soekeparametere.getArbeidssted(), soekeparametere.getArbeidstaker(), null, null, null, null, null));
    }

    private boolean harTilgangTilArbeidstaker(String arbeidstaker, List<Tilgang> tilganger) {
        return tilganger.stream().filter(t -> t.arbeidstakerIdentifkator().equals(arbeidstaker)).anyMatch(Tilgang::harTilgang);
    }
}
