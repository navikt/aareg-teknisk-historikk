package no.nav.aareg.teknisk.historikk.consumer.aareg.services;

import lombok.RequiredArgsConstructor;
import no.nav.aareg.kontrakter.teknisk.historikk.TekniskHistorikkRequest;
import no.nav.aareg.kontrakter.teknisk.historikk.TekniskHistorikkResponse;
import no.nav.aareg.teknisk.historikk.consumer.texas.TexasConsumer;
import no.nav.aareg.teknisk.historikk.provider.api.contract.Soekeparametere;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class AaregServicesConsumer {

    private final RestClient aaregServicesRestClient;
    private final TexasConsumer texasConsumer;

    @Value("${app.texas.target.aareg.services}")
    private String target;

    public TekniskHistorikkResponse hentTekniskHistorikk(Soekeparametere soekeparametere) {
        var requestBody = new TekniskHistorikkRequest(soekeparametere.getArbeidstaker(), soekeparametere.getArbeidssted(), soekeparametere.getOpplysningspliktig(), soekeparametere.getFraOgMed(), soekeparametere.getTilOgMed());

        return aaregServicesRestClient.post()
                .uri("/api/teknisk-historikk/afp")
                .body(requestBody)
                .headers(httpHeaders -> {
                    httpHeaders.setBearerAuth(texasConsumer.hentEntraToken(target));
                    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
                })
                .body(soekeparametere)
                .retrieve()
                .body(TekniskHistorikkResponse.class);
    }
}
