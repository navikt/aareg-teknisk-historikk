package no.nav.aareg.teknisk.historikk.wiremock.aareg.tilgangskontroll;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.extension.ResponseDefinitionTransformerV2;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import no.nav.aareg.teknisk.historikk.consumer.aareg.tilgangskontroll.dto.Tilgang;
import no.nav.aareg.teknisk.historikk.consumer.aareg.tilgangskontroll.dto.TilgangsforespoerselRequest;
import no.nav.aareg.teknisk.historikk.consumer.aareg.tilgangskontroll.dto.TilgangsforespoerselResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;

import java.util.ArrayList;

@RequiredArgsConstructor
public class AaregTilgangskontrollResponseTransformer implements ResponseDefinitionTransformerV2 {

    private final AaregTilgangskontrollStub aaregTilgangskontrollStub;

    @Override
    public String getName() {
        return "aareg-tilgangskontroll-stub";
    }

    @Override
    @SneakyThrows
    public ResponseDefinition transform(ServeEvent serveEvent) {
        var requestBody = new ObjectMapper().readValue(serveEvent.getRequest().getBody(), TilgangsforespoerselRequest.class);
        TilgangsforespoerselResponse response;

        var adressebeskyttedePersoner = aaregTilgangskontrollStub.getAdressebeskyttedePersoner();

        var tilgangsliste = new ArrayList<Tilgang>();

        requestBody.forespurteArbeidstakere().forEach(forespurtArbeidstaker -> {
            var opplysningspliktig = forespurtArbeidstaker.opplysningspliktigIdentifikator();
            var arbeidstaker = forespurtArbeidstaker.arbeidstakerIdentifikator();

            var harTilgang = false;

            if (StringUtils.hasText(opplysningspliktig)) {
                harTilgang = !adressebeskyttedePersoner.contains(opplysningspliktig) && !adressebeskyttedePersoner.contains(arbeidstaker);
            } else {
                harTilgang = !adressebeskyttedePersoner.contains(arbeidstaker);
            }

            tilgangsliste.add(
                    new Tilgang(
                            opplysningspliktig,
                            forespurtArbeidstaker.arbeidsstedIdentifikator(),
                            arbeidstaker,
                            harTilgang ? null : "Adressebeskyttet",
                            harTilgang,
                            StringUtils.hasText(opplysningspliktig) ? adressebeskyttedePersoner.contains(opplysningspliktig) : false,
                            adressebeskyttedePersoner.contains(arbeidstaker)
                    )
            );
        });

        response = new TilgangsforespoerselResponse(tilgangsliste);
        var responseBody = new ObjectMapper().writeValueAsString(response);

        return ResponseDefinitionBuilder
                .like(serveEvent.getResponseDefinition())
                .withStatus(200)
                .withHeader(HttpHeaders.CONNECTION, "close")
                .withBody(responseBody)
                .build();
    }

    @Override
    public boolean applyGlobally() {
        return false;
    }
}
