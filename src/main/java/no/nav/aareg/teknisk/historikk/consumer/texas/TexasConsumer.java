package no.nav.aareg.teknisk.historikk.consumer.texas;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.aareg.teknisk.historikk.consumer.texas.dto.ExchangeRequest;
import no.nav.aareg.teknisk.historikk.consumer.texas.dto.TexasResponse;
import no.nav.aareg.teknisk.historikk.consumer.texas.dto.TokenRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@Slf4j
@Component
@RequiredArgsConstructor
public class TexasConsumer {

    public static final String IDP_TOKEN_X = "tokenx";
    public static final String IDP_ENTRA = "azuread";

    private static final String ERROR = "Feil ved henting av token fra texas";

    private final RestClient texasRestClient;

    @Value("${nais.token.exchange.endpoint}")
    private String exchangeEndpoint;

    @Value("${nais.token.endpoint}")
    private String tokenEndpoint;

    public String hentTokenForBruker(String brukerToken, String target, String idp) {
        var exchangeRequest = new ExchangeRequest(idp, target, brukerToken.replace("Bearer ", ""));

        var response = texasRestClient.post()
                .uri(exchangeEndpoint)
                .headers(httpHeaders -> httpHeaders.setContentType(APPLICATION_JSON))
                .body(exchangeRequest)
                .retrieve()
                .body(TexasResponse.class);

        if (response == null || response.accessToken() == null) {
            log.error(ERROR);
            throw new RuntimeException(ERROR);
        }

        return response.accessToken();
    }

    public String hentEntraToken(String target) {
        var tokenRequest = new TokenRequest(IDP_ENTRA, target);

        var response = texasRestClient.post()
                .uri(tokenEndpoint)
                .headers(httpHeaders -> httpHeaders.setContentType(APPLICATION_JSON))
                .body(tokenRequest)
                .retrieve()
                .body(TexasResponse.class);

        if (response == null || response.accessToken() == null) {
            log.error(ERROR);
            throw new RuntimeException(ERROR);
        }

        return response.accessToken();
    }
}
