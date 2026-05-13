package no.nav.aareg.teknisk.historikk;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.client.RestTestClient;

import static no.nav.aareg.teknisk.historikk.api.ApiTestData.gyldigSoekeparameter;

class MaskinportenTokenTest extends AaregTekniskHistorikkTest {

    private static final String ENDEPUNKT_URI = "/api/v1/arbeidsforhold";
    private static final String AAREG_SERVICES_URI = "/aareg-services/todo";

    @Autowired
    private RestTestClient restTestClient;

    @Test
    void kanLeseKonsumentFraJwtToken() {
        WireMock.stubFor(
                WireMock.post(AAREG_SERVICES_URI)
                        .withHeader("Nav-Konsument", WireMock.equalTo(TESTORG))
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
    void kanLeseDatabehandlerFraJwtTokenet() {
        WireMock.stubFor(
                WireMock.post(AAREG_SERVICES_URI)
                        .withHeader("Nav-Konsument", WireMock.equalTo(TESTORG))
                        .withHeader("Nav-Databehandler", WireMock.equalTo(TESTSUPPLIER))
                        .willReturn(WireMock.okJson("{\"arbeidsforholdListe\":[]}"))
        );

        restTestClient.post()
                .uri(ENDEPUNKT_URI)
                .headers(headers -> headers.putAll(headerMedAutentiseringMedDatabehandler()))
                .body(gyldigSoekeparameter())
                .exchange()
                .expectStatus().isOk();
    }
}

