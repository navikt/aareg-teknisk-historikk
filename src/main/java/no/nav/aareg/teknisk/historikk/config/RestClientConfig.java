package no.nav.aareg.teknisk.historikk.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.impl.DefaultHttpRequestRetryStrategy;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.util.TimeValue;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Slf4j
@Configuration
public class RestClientConfig {

    private static final Marker TEAM_LOGS_MARKER = MarkerFactory.getMarker("TEAM_LOGS");

    @Bean
    RestClient azureRestClient() {
        return restClientBuilder("Azure").build();
    }

    @Bean
    RestClient aaregServicesRestClient(@Value("${app.url.aareg.services}") String baseUrl) {
        return restClientBuilder("aareg-services")
                .baseUrl(baseUrl)
                .build();
    }

    @Bean
    RestClient texasRestClient() {
        return restClientBuilder("Texas")
                .build();
    }

    public RestClient.Builder restClientBuilder(String tjeneste) {
        return RestClient.builder()
                .requestFactory(clientHttpRequestFactory())
                .defaultStatusHandler(HttpStatusCode::is4xxClientError, (clientRequest, clientResponse) -> {
                    var responseBody = new String(clientResponse.getBody().readAllBytes());
                    var melding = String.format("Tjeneste [%s] returnerte client error med status %s og melding %s", tjeneste, clientResponse.getStatusCode(), responseBody);
                    log.error(TEAM_LOGS_MARKER, melding);
                    throw new UpstreamServiceException(tjeneste, clientResponse.getStatusCode().value(), responseBody);
                })
                .defaultStatusHandler(HttpStatusCode::is5xxServerError, (clientRequest, clientResponse) -> {
                    var responseBody = new String(clientResponse.getBody().readAllBytes());
                    var melding = String.format("Tjeneste [%s] returnerte server error med status %s og melding %s", tjeneste, clientResponse.getStatusCode(), responseBody);
                    log.warn(TEAM_LOGS_MARKER, melding);
                    throw new UpstreamServiceException(tjeneste, clientResponse.getStatusCode().value(), responseBody);
                });
    }

    @Bean
    public HttpComponentsClientHttpRequestFactory clientHttpRequestFactory() {
        var client = HttpClients.custom()
                .useSystemProperties()
                .setRetryStrategy(new DefaultHttpRequestRetryStrategy(2, TimeValue.ofSeconds(1L)))
                .build();
        return new HttpComponentsClientHttpRequestFactory(client);
    }
}
