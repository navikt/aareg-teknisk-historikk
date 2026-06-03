package no.nav.aareg.teknisk.historikk.consumer.aareg.tilgangskontroll;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.aareg.teknisk.historikk.consumer.aareg.tilgangskontroll.dto.ForespurtArbeidstaker;
import no.nav.aareg.teknisk.historikk.consumer.aareg.tilgangskontroll.dto.Kontekst;
import no.nav.aareg.teknisk.historikk.consumer.aareg.tilgangskontroll.dto.Operasjon;
import no.nav.aareg.teknisk.historikk.consumer.aareg.tilgangskontroll.dto.Tilgang;
import no.nav.aareg.teknisk.historikk.consumer.aareg.tilgangskontroll.dto.TilgangsforespoerselRequest;
import no.nav.aareg.teknisk.historikk.consumer.aareg.tilgangskontroll.dto.TilgangsforespoerselResponse;
import no.nav.aareg.teknisk.historikk.consumer.texas.TexasConsumer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Gatherers;

@Slf4j
@Component
@RequiredArgsConstructor
public class AaregTilgangskontrollConsumer {

    private static final int MAX_BATCH_SIZE = 1000;

    private final TexasConsumer texasConsumer;

    private final RestClient aaregTilgangskontrollRestClient;

    @Value("${app.texas.target.aareg.tilgangskontroll}")
    private String aaregTilgangskontrollTarget;

    public List<Tilgang> kontrollerTilganger(Kontekst kontekst, Operasjon operasjon, Set<ForespurtArbeidstaker> unikeArbeidstakere) {
        var token = texasConsumer.hentEntraToken(aaregTilgangskontrollTarget);

        if (unikeArbeidstakere.size() > MAX_BATCH_SIZE) {
            var alleTilganger = new ArrayList<Tilgang>();

            unikeArbeidstakere.stream().gather(Gatherers.windowFixed(MAX_BATCH_SIZE)).forEach(batch -> alleTilganger.addAll(hentTilganger(token, kontekst, operasjon, batch)));

            return alleTilganger;
        } else {
            return hentTilganger(token, kontekst, operasjon, new ArrayList<>(unikeArbeidstakere));
        }
    }

    private List<Tilgang> hentTilganger(String token, Kontekst kontekst, Operasjon operasjon, List<ForespurtArbeidstaker> forespurteArbeidstakere) {
        var body = new TilgangsforespoerselRequest(kontekst, operasjon, new HashSet<>(forespurteArbeidstakere));
        return aaregTilgangskontrollRestClient.post()
                .uri("/api/v1/kontroller")
                .body(body)
                .headers(httpHeaders -> {
                    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
                    httpHeaders.setBearerAuth(token);
                })
                .retrieve()
                .body(TilgangsforespoerselResponse.class)
                .tilganger();
    }
}