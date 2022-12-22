package no.nav.aareg.teknisk_historikk.api

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo
import com.github.tomakehurst.wiremock.junit5.WireMockTest
import no.nav.aareg.teknisk_historikk.*
import no.nav.aareg.teknisk_historikk.aareg_services.AaregServicesConsumer
import no.nav.aareg.teknisk_historikk.models.FinnTekniskHistorikkForArbeidstaker200Response
import no.nav.aareg.teknisk_historikk.models.Soekeparametere
import no.nav.aareg.teknisk_historikk.models.TjenestefeilResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus

@WireMockTest(httpPort = WIREMOCK_PORT)
class ArbeidsforholdV1ApiTest : AaregTekniskHistorikkTest() {
    lateinit var logWatcher: ListAppender<ILoggingEvent>

    @Autowired
    lateinit var testRestTemplate: TestRestTemplate

    companion object {
        val ENDEPUNKT_URI = "/api/v1/arbeidsforhold"
        val AAREG_SERVICES_URI = "/api/beta/tekniskhistorikk"

        @JvmStatic
        @BeforeAll
        internal fun setup(wmRuntimeInfo: WireMockRuntimeInfo) {
            System.setProperty("wiremock.server.baseUrl", wmRuntimeInfo.httpBaseUrl)
        }
    }

    @BeforeEach
    fun setup(wmRuntimeInfo: WireMockRuntimeInfo) {
        logWatcher = ListAppender<ILoggingEvent>().apply { this.start() }
        Mockito.`when`(jwtDecoder.decode(testToken)).thenReturn(testJwt)
        stubFor(
            post("/token").willReturn(
                okJson("{\"access_token\":\"testtoken\", \"expires_in\": 10000}")
            )
        )

        stubFor(
            get("/wellknown").willReturn(
                okJson("{\"token_endpoint\":\"${wmRuntimeInfo.httpBaseUrl}/token\"}")
            )
        )
    }

    @Test
    fun `bruker sendte inn gyldige data`(wmRuntimeInfo: WireMockRuntimeInfo) {
        stubFor(
            get(AAREG_SERVICES_URI)
                .withHeader("Authorization", equalTo("Bearer testtoken"))
                .withHeader("Nav-Personident", equalTo("123456789"))
                .withHeader("Nav-Opplysningspliktigident", equalTo("123456"))
                .withHeader("Nav-Arbeidsstedident", equalTo("12345"))
                .willReturn(okJson(arbeidsforhold1))
        )

        val result = testRestTemplate.postForEntity(
            ENDEPUNKT_URI,
            HttpEntity(gyldigSoekeparameter().apply {
                opplysningspliktig = "123456"
                arbeidssted = "12345"
            }, headerMedAutentisering()),
            FinnTekniskHistorikkForArbeidstaker200Response::class.java
        )

        assertEquals(1, result.body?.antallArbeidsforhold ?: -1)
    }

    @Test
    fun `mangelfull respons fra aareg services`(wmRuntimeInfo: WireMockRuntimeInfo) {
        (LoggerFactory.getLogger(AaregServicesConsumer::class.java) as Logger).addAppender(logWatcher)
        stubFor(
            get(AAREG_SERVICES_URI)
                .withHeader("Authorization", equalTo("Bearer testtoken"))
                .willReturn(okJson("{}"))
        )

        val result = testRestTemplate.postForEntity(
            ENDEPUNKT_URI,
            HttpEntity(gyldigSoekeparameter(), headerMedAutentisering()),
            Feilrespons::class.java
        )

        assertEquals(Feilrespons(Feilkode.AAREG_SERVICES_MALFORMED), result.body)
        assertEquals(Level.ERROR, logWatcher.list.first().level)
        assertEquals(Feilkode.AAREG_SERVICES_MALFORMED.toString(), logWatcher.list.first().message)
    }

    @Test
    fun `500-feil fra aareg-services kastes ikke videre`(wmRuntimeInfo: WireMockRuntimeInfo) {
        stubFor(
            get(AAREG_SERVICES_URI)
                .withHeader("Authorization", equalTo("Bearer testtoken"))
                .willReturn(serverError().withBody("Jeg er ikke synlig for brukeren"))
        )

        val result = testRestTemplate.postForEntity(
            ENDEPUNKT_URI,
            HttpEntity(gyldigSoekeparameter(), headerMedAutentisering()),
            Feilrespons::class.java
        )

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.statusCode)
        assertEquals(Feilrespons(Feilkode.AAREG_SERVICES_ERROR), result.body)
    }

    @Test
    fun `500-feil fra aareg-services logges`(wmRuntimeInfo: WireMockRuntimeInfo) {
        (LoggerFactory.getLogger(AaregServicesConsumer::class.java) as Logger).addAppender(logWatcher)
        stubFor(
            get(AAREG_SERVICES_URI)
                .withHeader("Authorization", equalTo("Bearer testtoken"))
                .willReturn(serverError().withBody("Jeg er ikke synlig for brukeren"))
        )

        val result = testRestTemplate.postForEntity(
            ENDEPUNKT_URI,
            HttpEntity(gyldigSoekeparameter(), headerMedAutentisering()),
            Feilrespons::class.java
        )

        assertEquals(Feilrespons(Feilkode.AAREG_SERVICES_ERROR), result.body)
        assertEquals(Level.ERROR, logWatcher.list.first().level)
        assertEquals(Feilkode.AAREG_SERVICES_ERROR.toString(), logWatcher.list.first().message)
    }

    @Test
    fun `get i stedet for post`(wmRuntimeInfo: WireMockRuntimeInfo) {
        val result = testRestTemplate.exchange(
            ENDEPUNKT_URI,
            HttpMethod.GET,
            HttpEntity<Void>(headerMedKorrelasjonsId()),
            TjenestefeilResponse::class.java
        )

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, result.statusCode)
        assertEquals(
            tjenestefeilMedMelding("Http-verb ikke tillatt: GET", "Verb som er støttet: POST"),
            result.body
        )
    }

    @Test
    fun `bruker sendte ikke inn json`(wmRuntimeInfo: WireMockRuntimeInfo) {
        val result = testRestTemplate.postForEntity(
            ENDEPUNKT_URI,
            HttpEntity("", headerMedKorrelasjonsId()),
            TjenestefeilResponse::class.java
        )

        assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, result.statusCode)
        assertEquals(
            tjenestefeilMedMelding("Feil mediatype: text/plain;charset=UTF-8", "Støttede typer: application/json"),
            result.body
        )
    }

    @Test
    fun `bruker sendte ikke inn json-objekt`(wmRuntimeInfo: WireMockRuntimeInfo) {
        val result = testRestTemplate.postForEntity(
            ENDEPUNKT_URI,
            HttpEntity(emptyList<String>(), headerMedKorrelasjonsId()),
            TjenestefeilResponse::class.java
        )

        assertEquals(tjenestefeilMedMelding(IKKE_LESBAR_FEILMELDING), result.body)
    }

    @Test
    fun `bruker sendte ikke inn arbeidstaker`(wmRuntimeInfo: WireMockRuntimeInfo) {
        val result = testRestTemplate.postForEntity(
            ENDEPUNKT_URI,
            HttpEntity(Soekeparametere(), headerMedKorrelasjonsId()),
            TjenestefeilResponse::class.java
        )

        assertEquals(HttpStatus.BAD_REQUEST, result.statusCode)
        assertEquals(
            tjenestefeilMedMelding(ARBEIDSTAKER_ER_PAAKREVD, ARBEIDSTAKER_MAA_VAERE_TALL),
            result.body
        )
    }

    @Test
    fun `bruker sendte ikke inn kun tall for verdiene`(wmRuntimeInfo: WireMockRuntimeInfo) {
        val result = testRestTemplate.postForEntity(
            ENDEPUNKT_URI,
            HttpEntity(Soekeparametere().apply {
                arbeidstaker = "abcd1234"
                opplysningspliktig = "abcd1234"
                arbeidssted = "abcd1234"
            }, headerMedKorrelasjonsId()),
            TjenestefeilResponse::class.java
        )

        assertEquals(
            tjenestefeilMedMelding(
                ARBEIDSTAKER_MAA_VAERE_TALL,
                OPPLYSNINGSPLIKTIG_MAA_VAERE_TALL,
                ARBEIDSSTED_MAA_VAERE_TALL
            ), result.body
        )
    }

    @Test
    fun `401-feil fra aareg-services logges`(wmRuntimeInfo: WireMockRuntimeInfo) {
        (LoggerFactory.getLogger(AaregServicesConsumer::class.java) as Logger).addAppender(logWatcher)
        stubFor(
            get(AAREG_SERVICES_URI)
                .withHeader("Authorization", equalTo("Bearer testtoken"))
                .willReturn(unauthorized().withBody("Jeg er ikke synlig for brukeren"))
        )

        val result = testRestTemplate.postForEntity(
            ENDEPUNKT_URI,
            HttpEntity(gyldigSoekeparameter(), headerMedAutentisering()),
            Feilrespons::class.java
        )

        assertEquals(Feilrespons(Feilkode.AAREG_SERVICES_UNAUTHORIZED), result.body)
        assertEquals(Level.ERROR, logWatcher.list.first().level)
        assertEquals(Feilkode.AAREG_SERVICES_UNAUTHORIZED.toString(), logWatcher.list.first().message)
    }

    @Test
    fun `403-feil fra aareg-services gir fornuftig feilmelding`(wmRuntimeInfo: WireMockRuntimeInfo) {
        (LoggerFactory.getLogger(AaregServicesConsumer::class.java) as Logger).addAppender(logWatcher)
        stubFor(
            get(AAREG_SERVICES_URI)
                .withHeader("Authorization", equalTo("Bearer testtoken"))
                .willReturn(forbidden().withBody("Jeg er ikke synlig for brukeren"))
        )

        val result = testRestTemplate.postForEntity(
            ENDEPUNKT_URI,
            HttpEntity(gyldigSoekeparameter(), headerMedKorrelasjonsId()),
            TjenestefeilResponse::class.java
        )

        assertEquals(
            tjenestefeilMedMelding("Du mangler tilgang til å gjøre oppslag på arbeidstakeren"),
            result.body
        )
    }
}

fun headerMedKorrelasjonsId() = HttpHeaders().apply {
    set(KORRELASJONSID_HEADER, "korrelasjonsid")
    set("Authorization", "Bearer $testToken")
}

fun gyldigSoekeparameter() = Soekeparametere().apply {
    arbeidstaker = "123456789"
}

fun tjenestefeilMedMelding(vararg melding: String) = TjenestefeilResponse().apply {
    korrelasjonsid = "korrelasjonsid"
    meldinger = melding.asList()
}

private val arbeidsforhold1 = """
        [
          {
            "id": "abc-321",
            "navUuid": "54c260b3-344a-48da-b2ab-e472978cd2b4",
            "type": {
              "kode": "TEST",
              "beskrivelse": "testbeskrivelse"
            },
            "arbeidstaker": {
              "type": "Person",
              "identer": [
                {
                  "type": "FOLKEREGISTERIDENT",
                  "ident": "123456789123",
                  "gjeldende": true
                }
              ]
            },
            "opplysningspliktig": {
              "type": "Hovedenhet",
              "identer": [
                {
                  "type": "ORGANISASJONSNUMMER",
                  "ident": "123456789"
                }
              ]
            },
            "arbeidssted": {
              "type": "Underenhet",
              "identer": [
                {
                  "type": "ORGANISASJONSNUMMER",
                  "ident": "213456789"
                }
              ]
            },
            "rapporteringsordning": {
              "kode": "TEST",
              "beskrivelse": "testbeskrivelse"
            },
            "bruksperiode": {
              "fom": "2018-09-19T12:10:58.059Z",
              "tom": "2018-09-19T12:10:58.059Z"
            },
            "ansettelsesperioder": [
              {
                "startdato": "2014-07-01",
                "sluttdato": "2015-12-31",
                "sluttaarsak": {
                  "kode": "TEST",
                  "beskrivelse": "testbeskrivelse"
                },
                "bruksperiode": {
                  "fom": "2018-09-19T12:10:58.059Z",
                  "tom": "2018-09-19T12:10:58.059Z"
                },
                "sporingsinformasjon": {
                  "opprettetTidspunkt": "2018-09-19T12:10:58.059Z",
                  "opprettetKilde": "EDAG",
                  "endretTidspunkt": "2018-09-19T12:11:20.79Z",
                  "endretKilde": "AVSLUTNING"
                }
              }
            ],
            "sporingsinformasjon": {
              "opprettetTidspunkt": "2018-09-19T12:10:58.059Z",
              "opprettetKilde": "EDAG",
              "endretTidspunkt": "2018-09-19T12:11:20.79Z",
              "endretKilde": "AVSLUTNING"
            }
          }
        ]
    """.trimIndent()