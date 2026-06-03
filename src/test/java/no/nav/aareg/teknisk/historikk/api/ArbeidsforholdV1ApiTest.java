package no.nav.aareg.teknisk.historikk.api;

import no.nav.aareg.kontrakter.teknisk.historikk.TekniskHistorikk;
import no.nav.aareg.kontrakter.teknisk.historikk.TekniskHistorikkResponse;
import no.nav.aareg.teknisk.historikk.AaregTekniskHistorikkTest;
import no.nav.aareg.teknisk.historikk.provider.api.contract.Soekeparametere;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static no.nav.aareg.teknisk.historikk.api.ApiTestData.gyldigSoekeparameter;

class ArbeidsforholdV1ApiTest extends AaregTekniskHistorikkTest {

    private static final String ENDEPUNKT_URI = "/api/v1/arbeidsforhold";

    @Autowired
    private RestTestClient restTestClient;

    // --- Validation tests ---

    @Test
    void returnererBadRequestNaarArbeidstakerMangler() {
        var soekeparametere = new Soekeparametere();
        soekeparametere.setArbeidstaker(null);

        restTestClient.post()
                .uri(ENDEPUNKT_URI)
                .headers(headers -> {
                    headers.putAll(headerMedAutentisering());
                    headers.setContentType(MediaType.APPLICATION_JSON);
                })
                .body(soekeparametere)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void returnererBadRequestNaarArbeidstakerIkkeErTall() {
        var soekeparametere = new Soekeparametere();
        soekeparametere.setArbeidstaker("abcdef");

        restTestClient.post()
                .uri(ENDEPUNKT_URI)
                .headers(headers -> {
                    headers.putAll(headerMedAutentisering());
                    headers.setContentType(MediaType.APPLICATION_JSON);
                })
                .body(soekeparametere)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void returnererBadRequestNaarArbeidstakerHarFeilLengde() {
        var soekeparametere = new Soekeparametere();
        soekeparametere.setArbeidstaker("123456"); // not 11 digits

        restTestClient.post()
                .uri(ENDEPUNKT_URI)
                .headers(headers -> {
                    headers.putAll(headerMedAutentisering());
                    headers.setContentType(MediaType.APPLICATION_JSON);
                })
                .body(soekeparametere)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void returnererBadRequestNaarOpplysningspliktigIkkeErTall() {
        var soekeparametere = new Soekeparametere();
        soekeparametere.setArbeidstaker("12345678901");
        soekeparametere.setOpplysningspliktig("abc");

        restTestClient.post()
                .uri(ENDEPUNKT_URI)
                .headers(headers -> {
                    headers.putAll(headerMedAutentisering());
                    headers.setContentType(MediaType.APPLICATION_JSON);
                })
                .body(soekeparametere)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void returnererBadRequestNaarArbeidsstedIkkeErTall() {
        var soekeparametere = new Soekeparametere();
        soekeparametere.setArbeidstaker("12345678901");
        soekeparametere.setArbeidssted("abc");

        restTestClient.post()
                .uri(ENDEPUNKT_URI)
                .headers(headers -> {
                    headers.putAll(headerMedAutentisering());
                    headers.setContentType(MediaType.APPLICATION_JSON);
                })
                .body(soekeparametere)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void returnererBadRequestMedFlereFeilNaarFlereValideringsfeil() {
        var soekeparametere = new Soekeparametere();
        soekeparametere.setArbeidstaker(null);
        soekeparametere.setOpplysningspliktig("abc");
        soekeparametere.setArbeidssted("xyz");

        restTestClient.post()
                .uri(ENDEPUNKT_URI)
                .headers(headers -> {
                    headers.putAll(headerMedAutentisering());
                    headers.setContentType(MediaType.APPLICATION_JSON);
                })
                .body(soekeparametere)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void returnererBadRequestVedUgyldigJson() {
        restTestClient.post()
                .uri(ENDEPUNKT_URI)
                .headers(headers -> {
                    headers.putAll(headerMedAutentisering());
                    headers.setContentType(MediaType.APPLICATION_JSON);
                })
                .body("{ugyldig json")
                .exchange()
                .expectStatus().isBadRequest();
    }

    // --- Successful fetch from aareg-services ---

    @Test
    void returnererTekniskHistorikkFraAaregServices() {
        var responseData = new TekniskHistorikkResponse(
                List.of(new TekniskHistorikk(
                        1L,
                        "987654321",
                        "123456789",
                        "12345678901",
                        "ordinaertArbeidsforhold",
                        LocalDate.of(2020, 1, 1),
                        LocalDate.of(2021, 6, 30),
                        "Endring",
                        LocalDateTime.of(2021, 7, 1, 10, 0, 0, 0)
                ))
        );
        aaregServicesStub.stubPostTekniskHistorikk(200, responseData);

        var soekeparametere = new Soekeparametere();
        soekeparametere.setArbeidstaker("12345678901");

        restTestClient.post()
                .uri(ENDEPUNKT_URI)
                .headers(headers -> headers.putAll(headerMedAutentisering()))
                .body(soekeparametere)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.arbeidsforhold").isArray()
                .jsonPath("$.arbeidsforhold.length()").isEqualTo(1)
                .jsonPath("$.arbeidsforhold[0].arbeidsforholdId").isEqualTo(1)
                .jsonPath("$.arbeidsforhold[0].arbeidsforholdType").isEqualTo("ordinaertArbeidsforhold")
                .jsonPath("$.arbeidsforhold[0].endringstype").isEqualTo("Endring");
    }

    @Test
    void returnererTomListeNaarIngenArbeidsforholdFinnes() {
        aaregServicesStub.stubPostTekniskHistorikk(200, new TekniskHistorikkResponse(List.of()));

        restTestClient.post()
                .uri(ENDEPUNKT_URI)
                .headers(headers -> headers.putAll(headerMedAutentisering()))
                .body(gyldigSoekeparameter())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.arbeidsforhold").isArray()
                .jsonPath("$.arbeidsforhold.length()").isEqualTo(0);
    }

    // --- Access control / adressebeskyttelse tests ---

    @Test
    void returnererForbiddenNaarArbeidstakerErAdressebeskyttet() {
        var soekeparametere = new Soekeparametere();
        soekeparametere.setArbeidstaker("11111111111");

        aaregServicesStub.stubPostTekniskHistorikk(200, new TekniskHistorikkResponse(List.of()));

        restTestClient.post()
                .uri(ENDEPUNKT_URI)
                .headers(headers -> headers.putAll(headerMedAutentisering()))
                .body(soekeparametere)
                .exchange()
                .expectStatus().isForbidden()
                .expectBody()
                .jsonPath("$.feilmeldinger[0]").isEqualTo("Ingen tilgang til arbeidstaker");
    }

    @Test
    void returnererForbiddenNaarArbeidstakerErAdressebeskyttetMedHistorikk() {
        var arbeidstakerId = "11111111111";
        var responseData = new TekniskHistorikkResponse(
                List.of(new TekniskHistorikk(
                        1L,
                        "987654321",
                        "123456789",
                        arbeidstakerId,
                        "ordinaertArbeidsforhold",
                        LocalDate.of(2020, 1, 1),
                        LocalDate.of(2021, 6, 30),
                        "Endring",
                        LocalDateTime.of(2021, 7, 1, 10, 0, 0, 0)
                ))
        );
        aaregServicesStub.stubPostTekniskHistorikk(200, responseData);

        var soekeparametere = new Soekeparametere();
        soekeparametere.setArbeidstaker(arbeidstakerId);

        restTestClient.post()
                .uri(ENDEPUNKT_URI)
                .headers(headers -> headers.putAll(headerMedAutentisering()))
                .body(soekeparametere)
                .exchange()
                .expectStatus().isForbidden()
                .expectBody()
                .jsonPath("$.feilmeldinger[0]").isEqualTo("Ingen tilgang til arbeidstaker");
    }

    @Test
    void returnererOkNaarArbeidstakerIkkeErAdressebeskyttet() {
        var responseData = new TekniskHistorikkResponse(
                List.of(new TekniskHistorikk(
                        1L,
                        "987654321",
                        "123456789",
                        "12345678901",
                        "ordinaertArbeidsforhold",
                        LocalDate.of(2020, 1, 1),
                        LocalDate.of(2021, 6, 30),
                        "Endring",
                        LocalDateTime.of(2021, 7, 1, 10, 0, 0, 0)
                ))
        );
        aaregServicesStub.stubPostTekniskHistorikk(200, responseData);

        var soekeparametere = new Soekeparametere();
        soekeparametere.setArbeidstaker("12345678901");

        restTestClient.post()
                .uri(ENDEPUNKT_URI)
                .headers(headers -> headers.putAll(headerMedAutentisering()))
                .body(soekeparametere)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.arbeidsforhold.length()").isEqualTo(1);
    }

    @Test
    void skal_fjerne_arbeidsforhold_hvor_arbeidsgiver_er_adressebeskyttet() {
        var responseData = new TekniskHistorikkResponse(
                List.of(new TekniskHistorikk(
                        1L,
                        "987654321",
                        "123456789",
                        "12345678901",
                        "ordinaertArbeidsforhold",
                        LocalDate.of(2020, 1, 1),
                        LocalDate.of(2021, 6, 30),
                        "Endring",
                        LocalDateTime.of(2021, 7, 1, 10, 0, 0, 0)
                ),
                        new TekniskHistorikk(
                                2L,
                                "11111111111",
                                "11111111111",
                                "12345678901",
                                "ordinaertArbeidsforhold",
                                LocalDate.of(2020, 1, 1),
                                LocalDate.of(2021, 6, 30),
                                "Endring",
                                LocalDateTime.of(2021, 7, 1, 10, 0, 0, 0)
                        )
        ));
        aaregServicesStub.stubPostTekniskHistorikk(200, responseData);

        var soekeparametere = new Soekeparametere();
        soekeparametere.setArbeidstaker("12345678901");

        restTestClient.post()
                .uri(ENDEPUNKT_URI)
                .headers(headers -> headers.putAll(headerMedAutentisering()))
                .body(soekeparametere)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.arbeidsforhold.length()").isEqualTo(1);
    }

    // --- Authentication tests ---

    @Test
    void returnererUnauthorizedUtenToken() {
        restTestClient.post()
                .uri(ENDEPUNKT_URI)
                .headers(headers -> headers.setContentType(MediaType.APPLICATION_JSON))
                .body(gyldigSoekeparameter())
                .exchange()
                .expectStatus().isUnauthorized();
    }

    // --- Upstream service error ---

    @Test
    void returnererFeilNaarAaregTilgangskontrollErNede() {
        aaregServicesStub.stubPostTekniskHistorikk(200, new TekniskHistorikkResponse(List.of()));
        aaregTilgangskontrollStub.stubForNedetid();

        restTestClient.post()
                .uri(ENDEPUNKT_URI)
                .headers(headers -> headers.putAll(headerMedAutentisering()))
                .body(gyldigSoekeparameter())
                .exchange()
                .expectStatus().is5xxServerError();
    }

    // --- Valid request with optional fields ---

    @Test
    void akseptererGyldigSoekMedAlleOptionalFelt() {
        aaregServicesStub.stubPostTekniskHistorikk(200, new TekniskHistorikkResponse(List.of()));

        var soekeparametere = new Soekeparametere();
        soekeparametere.setArbeidstaker("12345678901");
        soekeparametere.setOpplysningspliktig("987654321");
        soekeparametere.setArbeidssted("123456789");

        restTestClient.post()
                .uri(ENDEPUNKT_URI)
                .headers(headers -> headers.putAll(headerMedAutentisering()))
                .body(soekeparametere)
                .exchange()
                .expectStatus().isOk();
    }

    // --- TraceId tests ---

    @Test
    void returnererTraceIdVedVellykketRespons() {
        aaregServicesStub.stubPostTekniskHistorikk(200, new TekniskHistorikkResponse(List.of()));

        restTestClient.post()
                .uri(ENDEPUNKT_URI)
                .headers(headers -> headers.putAll(headerMedAutentisering()))
                .body(gyldigSoekeparameter())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.traceId").isNotEmpty();
    }

    @Test
    void returnererTraceIdVedForbiddenRespons() {
        aaregServicesStub.stubPostTekniskHistorikk(200, new TekniskHistorikkResponse(List.of()));

        var soekeparametere = new Soekeparametere();
        soekeparametere.setArbeidstaker("11111111111");

        restTestClient.post()
                .uri(ENDEPUNKT_URI)
                .headers(headers -> headers.putAll(headerMedAutentisering()))
                .body(soekeparametere)
                .exchange()
                .expectStatus().isForbidden()
                .expectBody()
                .jsonPath("$.traceId").isNotEmpty();
    }

    @Test
    void returnererTraceIdVedInternalServerError() {
        aaregServicesStub.stubPostTekniskHistorikk(200, new TekniskHistorikkResponse(List.of()));
        aaregTilgangskontrollStub.stubForNedetid();

        restTestClient.post()
                .uri(ENDEPUNKT_URI)
                .headers(headers -> headers.putAll(headerMedAutentisering()))
                .body(gyldigSoekeparameter())
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody()
                .jsonPath("$.traceId").isNotEmpty();
    }
}
