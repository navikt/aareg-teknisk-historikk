package no.nav.aareg.teknisk.historikk.service.tilgangskontroll;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.aareg.kontrakter.teknisk.historikk.TekniskHistorikk;
import no.nav.aareg.teknisk.historikk.consumer.aareg.tilgangskontroll.AaregTilgangskontrollConsumer;
import no.nav.aareg.teknisk.historikk.consumer.aareg.tilgangskontroll.dto.ForespurtArbeidstaker;
import no.nav.aareg.teknisk.historikk.consumer.aareg.tilgangskontroll.dto.Kontekst;
import no.nav.aareg.teknisk.historikk.consumer.aareg.tilgangskontroll.dto.Operasjon;
import no.nav.aareg.teknisk.historikk.consumer.aareg.tilgangskontroll.dto.Tilgang;
import no.nav.aareg.teknisk.historikk.exception.ApplicationException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TilgangskontrollService {

    private final AaregTilgangskontrollConsumer aaregTilgangskontrollConsumer;

    public List<Tilgang> hentTilganger(List<TekniskHistorikk> ufiltrertListe) {
        var unikeArbeidstakere = new HashSet<ForespurtArbeidstaker>();
        ufiltrertListe.forEach(rad -> unikeArbeidstakere.add(new ForespurtArbeidstaker(rad.opplysningspliktigId(), rad.arbeidsstedId(), rad.arbeidstakerId())));

        var tilganger = aaregTilgangskontrollConsumer.kontrollerTilganger(Kontekst.SYSTEM_UTEN_TILGANG_TIL_ADRESSEBESKYTTELSE, Operasjon.LESE, unikeArbeidstakere);
        if (tilganger.isEmpty()) {
            throw new ApplicationException("Feil ved tilgangkontroll - ingen tilganger funnet");
        }
        return tilganger;
    }
}