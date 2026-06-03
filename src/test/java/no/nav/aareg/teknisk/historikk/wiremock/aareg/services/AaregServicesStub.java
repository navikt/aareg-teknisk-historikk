package no.nav.aareg.teknisk.historikk.wiremock.aareg.services;

import com.github.tomakehurst.wiremock.client.WireMock;
import no.nav.aareg.kontrakter.teknisk.historikk.TekniskHistorikkResponse;
import no.nav.aareg.teknisk.historikk.wiremock.WireMockStub;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class AaregServicesStub implements WireMockStub {

    public static final String TEKNISK_HISTORIKK_PATH = "/aareg-services/api/teknisk-historikk/afp";
    private final JsonMapper jsonMapper = new JsonMapper();

    @Override
    public void registerResponseMappingBeforeAll() {
        stubTekniskHistorikkOk();
        stubLegacyTekniskHistorikkOk();
    }

    @Override
    public void registerResponseMappingBeforeEach() {
        stubTekniskHistorikkOk();
    }

    public void stubTekniskHistorikkOk() {
        var emptyBody = new TekniskHistorikkResponse(List.of());
        stubPostTekniskHistorikk(OK.value(), emptyBody);
    }

    public void stubLegacyTekniskHistorikkOk() {
        var emptyBody = new TekniskHistorikkResponse(List.of());
        stubLegacyTekniskHistorikk(OK.value(), emptyBody);
    }

    public void stubPostTekniskHistorikk(int statusCode, TekniskHistorikkResponse responseBody) {
        WireMock.stubFor(post(urlPathEqualTo(TEKNISK_HISTORIKK_PATH))
                .willReturn(jsonResponse(statusCode, responseBody)));
    }

    public void stubLegacyTekniskHistorikk(int statusCode, TekniskHistorikkResponse responseBody) {
        WireMock.stubFor(post(urlPathEqualTo(TEKNISK_HISTORIKK_PATH))
                .willReturn(jsonResponse(statusCode, responseBody)));
    }

    private com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder jsonResponse(int statusCode, TekniskHistorikkResponse responseBody) {
        return aResponse()
                .withStatus(statusCode)
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(jsonMapper.writeValueAsString(responseBody));
    }
}

