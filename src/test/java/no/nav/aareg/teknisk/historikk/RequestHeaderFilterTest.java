package no.nav.aareg.teknisk.historikk;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.client.RestTestClient;

import static no.nav.aareg.teknisk.historikk.api.ApiTestData.gyldigSoekeparameter;

class RequestHeaderFilterTest extends AaregTekniskHistorikkTest {

    private static final String ENDEPUNKT_URI = "/api/v1/arbeidsforhold";
    private static final String AAREG_SERVICES_URI = "/aareg-services/todo";


    @Autowired
    private RestTestClient restTestClient;

    @Test
    void aaregServicesMottarCallId() {
        WireMock.stubFor(
                WireMock.post(AAREG_SERVICES_URI)
                        .withHeader("Nav-Call-Id", WireMock.equalTo("aareg-teknisk-historikk-test"))
                        .willReturn(WireMock.okJson("{\"arbeidsforholdListe\":[]}"))
        );

        restTestClient.post()
                .uri(ENDEPUNKT_URI)
                .headers(headers -> headers.putAll(headerMedAutentisering()))
                .body(gyldigSoekeparameter())
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void aaregServicesMottarKorrelasjonsid() {
        WireMock.stubFor(
                WireMock.post(AAREG_SERVICES_URI)
                        .withHeader(KORRELASJONSID_HEADER, WireMock.equalTo("test-korrelasjons-id"))
                        .willReturn(WireMock.okJson("{\"arbeidsforholdListe\":[]}"))
        );

        restTestClient.post()
                .uri(ENDEPUNKT_URI)
                .headers(headers -> headers.putAll(medKorrelasjonsid(headerMedAutentisering(), "test-korrelasjons-id")))
                .body(gyldigSoekeparameter())
                .exchange()
                .expectStatus().isOk();
    }
}

