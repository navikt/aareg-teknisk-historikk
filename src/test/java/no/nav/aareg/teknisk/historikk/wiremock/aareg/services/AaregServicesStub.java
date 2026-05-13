package no.nav.aareg.teknisk.historikk.wiremock.aareg.services;

import com.github.tomakehurst.wiremock.client.WireMock;
import no.nav.aareg.teknisk.historikk.wiremock.WireMockStub;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class AaregServicesStub implements WireMockStub {

    public static final String TEKNISK_HISTORIKK_PATH = "/aareg-services/todo";

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
        stubPostTekniskHistorikk(OK.value(), "{\"arbeidsforholdListe\":[]}");
    }

    public void stubLegacyTekniskHistorikkOk() {
        stubLegacyTekniskHistorikk(OK.value(), "{\"arbeidsforholdListe\":[]}");
    }

    public void stubPostTekniskHistorikk(int statusCode, String responseBody) {
        WireMock.stubFor(post(urlPathEqualTo(TEKNISK_HISTORIKK_PATH))
                .willReturn(jsonResponse(statusCode, responseBody)));
    }

    public void stubLegacyTekniskHistorikk(int statusCode, String responseBody) {
        WireMock.stubFor(post(urlPathEqualTo(TEKNISK_HISTORIKK_PATH))
                .willReturn(jsonResponse(statusCode, normalizeLegacyBody(responseBody))));
    }

    public void stubLegacyTekniskHistorikkWithHeaders(int statusCode, String responseBody,
                                                      String personident, String opplysningspliktigident, String arbeidsstedident) {
        WireMock.stubFor(post(urlPathEqualTo(TEKNISK_HISTORIKK_PATH))
                .withHeader("Nav-Personident", equalTo(personident))
                .withHeader("Nav-Opplysningspliktigident", equalTo(opplysningspliktigident))
                .withHeader("Nav-Arbeidsstedident", equalTo(arbeidsstedident))
                .willReturn(jsonResponse(statusCode, normalizeLegacyBody(responseBody))));
    }

    private static String normalizeLegacyBody(String responseBody) {
        var trimmedBody = responseBody == null ? "" : responseBody.trim();
        if (trimmedBody.startsWith("[")) {
            return "{\"arbeidsforholdListe\":" + responseBody + "}";
        }
        return responseBody;
    }

    private static com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder jsonResponse(int statusCode, String responseBody) {
        return aResponse()
                .withStatus(statusCode)
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(responseBody);
    }
}

