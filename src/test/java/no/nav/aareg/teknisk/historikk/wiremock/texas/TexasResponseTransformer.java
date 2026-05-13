package no.nav.aareg.teknisk.historikk.wiremock.texas;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.extension.ResponseDefinitionTransformerV2;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import no.nav.aareg.teknisk.historikk.consumer.texas.dto.ExchangeRequest;
import no.nav.aareg.teknisk.historikk.consumer.texas.dto.TexasResponse;
import org.springframework.http.HttpHeaders;
import tools.jackson.databind.json.JsonMapper;

@RequiredArgsConstructor
public class TexasResponseTransformer implements ResponseDefinitionTransformerV2 {

    private final TexasStub texasStub;
    private final JsonMapper jsonMapper;

    @Override
    public String getName() {
        return "texas-stub";
    }

    @Override
    @SneakyThrows
    public ResponseDefinition transform(ServeEvent serveEvent) {
        var url = serveEvent.getRequest().getUrl();
        String accessToken = "token";

        if (url.contains("/exchange")) {
            try {
                var exchangeRequest = jsonMapper.readValue(serveEvent.getRequest().getBody(), ExchangeRequest.class);
                if (exchangeRequest.userToken() != null) {
                    accessToken = exchangeRequest.userToken();
                }
            } catch (Exception ignored) {
                // Fallback til "token" hvis body ikke kan parses
            }
        }

        var responseBody = jsonMapper.writeValueAsString(new TexasResponse(accessToken, "Bearer", 10000));

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
