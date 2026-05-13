package no.nav.aareg.teknisk.historikk.api;

import no.nav.aareg.teknisk.historikk.AaregTekniskHistorikkTest;
import no.nav.aareg.teknisk.historikk.Feilkode;
import no.nav.aareg.teknisk.historikk.provider.api.contract.Soekeparametere;
import no.nav.aareg.teknisk.historikk.provider.api.contract.TjenestefeilResponse;
import no.nav.aareg.teknisk.historikk.wiremock.aareg.services.AaregServicesStub;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;

import static com.github.tomakehurst.wiremock.client.WireMock.notFound;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static no.nav.aareg.teknisk.historikk.api.ApiTestData.ARBEIDSFORHOLD_1;
import static no.nav.aareg.teknisk.historikk.api.ApiTestData.gyldigSoekeparameter;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ArbeidsforholdV1ApiTest extends AaregTekniskHistorikkTest {

    private static final String ENDEPUNKT_URI = "/api/v1/arbeidsforhold";

    @Autowired
    private RestTestClient restTestClient;

    @BeforeEach
    void setupPerTest() {
        aaregServicesStub.stubLegacyTekniskHistorikkOk();
    }

    @Test
    void brukerSendteInnGyldigeData() {
        aaregServicesStub.stubLegacyTekniskHistorikkWithHeaders(200, ARBEIDSFORHOLD_1, "123456789", "123456", "12345");

        var soekeparametere = gyldigSoekeparameter();
        soekeparametere.setOpplysningspliktig("123456");
        soekeparametere.setArbeidssted("12345");

        restTestClient.post()
                .uri(ENDEPUNKT_URI)
                .headers(headers -> headers.putAll(headerMedAutentisering()))
                .body(soekeparametere)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class);
    }

    @Test
    void brukerErIkkeLoggetInn() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("FalskToken");

        restTestClient.post()
                .uri(ENDEPUNKT_URI)
                .headers(h -> h.putAll(headers))
                .body(gyldigSoekeparameter())
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void mangelfullResponsFraAaregServices() {
        aaregServicesStub.stubLegacyTekniskHistorikk(200, "{}");

        restTestClient.post()
                .uri(ENDEPUNKT_URI)
                .headers(headers -> headers.putAll(headerMedAutentiseringOgKorrelasjon()))
                .body(gyldigSoekeparameter())
                .exchange()
                .expectStatus().isOk()
                .expectBody(TjenestefeilResponse.class)
                .value(body -> {
                    assertEquals(Feilkode.AAREG_SERVICES_MALFORMED.toString(), body.getMeldinger().get(0));
                });
    }

    @Test
    void feil500FraAaregServicesKastesIkkeVidere() {
        aaregServicesStub.stubLegacyTekniskHistorikk(500, "Jeg er ikke synlig for brukeren");

        restTestClient.post()
                .uri(ENDEPUNKT_URI)
                .headers(headers -> headers.putAll(headerMedAutentiseringOgKorrelasjon()))
                .body(gyldigSoekeparameter())
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody(TjenestefeilResponse.class)
                .value(body -> assertEquals(Feilkode.AAREG_SERVICES_ERROR.toString(), body.getMeldinger().get(0)));
    }

    @Test
    void feil500FraAaregServicesLogges() {
        aaregServicesStub.stubLegacyTekniskHistorikk(500, "Jeg er ikke synlig for brukeren");

        restTestClient.post()
                .uri(ENDEPUNKT_URI)
                .headers(headers -> headers.putAll(headerMedAutentiseringOgKorrelasjon()))
                .body(gyldigSoekeparameter())
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody(TjenestefeilResponse.class)
                .value(body -> {
                    assertEquals(Feilkode.AAREG_SERVICES_ERROR.toString(), body.getMeldinger().get(0));
                });
    }

    @Test
    void getIStedetForPost() {
        restTestClient.get()
                .uri(ENDEPUNKT_URI)
                .headers(headers -> headers.putAll(headerMedAutentiseringOgKorrelasjon()))
                .exchange()
                .expectStatus().is4xxClientError()
                .expectBody(TjenestefeilResponse.class)
                .value(body -> assertEquals("Http-verb ikke tillatt: GET", body.getMeldinger().get(0)));
    }

    @Test
    void brukerSendteIkkeInnJson() {
        restTestClient.post()
                .uri(ENDEPUNKT_URI)
                .headers(headers -> headers.putAll(headerMedAutentiseringOgKorrelasjon()))
                .contentType(MediaType.TEXT_PLAIN)
                .body("")
                .exchange()
                .expectStatus().is4xxClientError()
                .expectBody(TjenestefeilResponse.class);
    }

    @Test
    void brukerSendteIkkeInnJsonObjekt() {
        restTestClient.post()
                .uri(ENDEPUNKT_URI)
                .headers(headers -> headers.putAll(headerMedAutentiseringOgKorrelasjon()))
                .body(java.util.List.<String>of())
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(TjenestefeilResponse.class);
    }

    @Test
    void brukerSendteIkkeInnArbeidstaker() {
        restTestClient.post()
                .uri(ENDEPUNKT_URI)
                .headers(headers -> headers.putAll(headerMedAutentiseringOgKorrelasjon()))
                .body(new Soekeparametere())
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(TjenestefeilResponse.class);
    }

    @Test
    void brukerSendteIkkeInnKunTallForVerdiene() {
        Soekeparametere soekeparametere = new Soekeparametere();
        soekeparametere.setArbeidstaker("abcd1234");
        soekeparametere.setOpplysningspliktig("abcd1234");
        soekeparametere.setArbeidssted("abcd1234");

        restTestClient.post()
                .uri(ENDEPUNKT_URI)
                .headers(headers -> headers.putAll(headerMedAutentiseringOgKorrelasjon()))
                .body(soekeparametere)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(TjenestefeilResponse.class);
    }

    @Test
    void feil401FraAaregServicesLogges() {
        aaregServicesStub.stubLegacyTekniskHistorikk(401, "Jeg er ikke synlig for brukeren");

        restTestClient.post()
                .uri(ENDEPUNKT_URI)
                .headers(headers -> headers.putAll(headerMedAutentiseringOgKorrelasjon()))
                .body(gyldigSoekeparameter())
                .exchange()
                .expectStatus().isOk()
                .expectBody(TjenestefeilResponse.class)
                .value(body -> {
                    assertEquals(Feilkode.AAREG_SERVICES_UNAUTHORIZED.toString(), body.getMeldinger().get(0));
                });
    }

    @Test
    void feil403FraAaregServicesGirFornuftigFeilmelding() {
        aaregServicesStub.stubLegacyTekniskHistorikk(403, "Jeg er ikke synlig for brukeren");

        restTestClient.post()
                .uri(ENDEPUNKT_URI)
                .headers(headers -> headers.putAll(headerMedAutentiseringOgKorrelasjon()))
                .body(gyldigSoekeparameter())
                .exchange()
                .expectStatus().isOk()
                .expectBody(TjenestefeilResponse.class)
                .value(body -> assertEquals(Feilkode.AAREG_SERVICES_FORBIDDEN.toString(), body.getMeldinger().get(0)));
    }

    @Test
    void feil404FraAaregServicesKastesVidere() {
        aaregServicesStub.stubLegacyTekniskHistorikk(404, "Ukjent ident");

        restTestClient.post()
                .uri(ENDEPUNKT_URI)
                .headers(headers -> headers.putAll(headerMedAutentiseringOgKorrelasjon()))
                .body(gyldigSoekeparameter())
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(TjenestefeilResponse.class)
                .value(body -> assertEquals("Ukjent ident", body.getMeldinger().get(0)));
    }

    @Test
    void men404FeilFraAzureKastesIkke() {
        stubFor(post("/token").willReturn(notFound()));

        restTestClient.post()
                .uri(ENDEPUNKT_URI)
                .headers(headers -> headers.putAll(headerMedAutentiseringOgKorrelasjon()))
                .body(gyldigSoekeparameter())
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody(TjenestefeilResponse.class)
                .value(body -> assertEquals(Feilkode.AZURE_KONSUMENT_FEIL.toString(), body.getMeldinger().get(0)));
    }

}

