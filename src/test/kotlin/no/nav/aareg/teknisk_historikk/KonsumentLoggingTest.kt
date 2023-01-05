package no.nav.aareg.teknisk_historikk

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo
import com.github.tomakehurst.wiremock.junit5.WireMockTest
import io.micrometer.core.instrument.MeterRegistry
import no.nav.aareg.teknisk_historikk.api.FINN_TEKNISK_HISTORIKK_NAVN
import no.nav.aareg.teknisk_historikk.api.arbeidsforhold1
import no.nav.aareg.teknisk_historikk.api.gyldigSoekeparameter
import no.nav.aareg.teknisk_historikk.models.FinnTekniskHistorikkForArbeidstaker200Response
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity

@WireMockTest(httpPort = WIREMOCK_PORT)
class KonsumentLoggingTest : AaregTekniskHistorikkTest() {
    lateinit var logWatcher: ListAppender<ILoggingEvent>

    @Autowired
    lateinit var testRestTemplate: TestRestTemplate

    @Autowired
    lateinit var meterRegistry: MeterRegistry

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
        WireMock.stubFor(
            WireMock.post("/token").willReturn(
                WireMock.okJson("{\"access_token\":\"$testAzureToken\", \"expires_in\": 10}")
            )
        )

        WireMock.stubFor(
            WireMock.get("/wellknown").willReturn(
                WireMock.okJson("{\"token_endpoint\":\"${wmRuntimeInfo.httpBaseUrl}/token\"}")
            )
        )
    }

    @Test
    fun `logger oppslagsmetrikker for konsumenter`(wmRuntimeInfo: WireMockRuntimeInfo) {
        Mockito.`when`(jwtDecoder.decode(testToken)).thenReturn(testJwt)
        WireMock.stubFor(
            WireMock.get(AAREG_SERVICES_URI)
                .willReturn(WireMock.okJson(arbeidsforhold1))
        )

        // TODO nullstill teller før test kjøres
        val forrigeTeller = meterRegistry.counter(AAREG_ARBEIDSFORHOLD_OPPSLAG_NAVN,
            OPPSLAGSTYPE_METRIC_TAG_NAVN, FINN_TEKNISK_HISTORIKK_NAVN,
            ORG_NUMMER_METRIC_TAG_NAVN, testorg,
            DATABEHANDLER_METRIC_TAG_NAVN, "").count()

        testRestTemplate.postForEntity(
            ENDEPUNKT_URI,
            HttpEntity(gyldigSoekeparameter(), headerMedAutentisering()),
            FinnTekniskHistorikkForArbeidstaker200Response::class.java
        )

        assertEquals(1.0, meterRegistry.counter(AAREG_ARBEIDSFORHOLD_OPPSLAG_NAVN,
            OPPSLAGSTYPE_METRIC_TAG_NAVN, FINN_TEKNISK_HISTORIKK_NAVN,
            ORG_NUMMER_METRIC_TAG_NAVN, testorg,
            DATABEHANDLER_METRIC_TAG_NAVN, "").count() - forrigeTeller)
    }

    @Test
    fun `logger oppslagsmetrikker for databehandlere`(wmRuntimeInfo: WireMockRuntimeInfo) {
        Mockito.`when`(jwtDecoder.decode(testToken)).thenReturn(testJwtMedDatabehandler)
        WireMock.stubFor(
            WireMock.get(AAREG_SERVICES_URI)
                .willReturn(WireMock.okJson(arbeidsforhold1))
        )

        // TODO nullstill teller før test kjøres
        val forrigeTeller = meterRegistry.counter(AAREG_ARBEIDSFORHOLD_OPPSLAG_NAVN,
            OPPSLAGSTYPE_METRIC_TAG_NAVN, FINN_TEKNISK_HISTORIKK_NAVN,
            ORG_NUMMER_METRIC_TAG_NAVN, testorg,
            DATABEHANDLER_METRIC_TAG_NAVN, testsupplier).count()

        testRestTemplate.postForEntity(
            ENDEPUNKT_URI,
            HttpEntity(gyldigSoekeparameter(), headerMedAutentisering()),
            FinnTekniskHistorikkForArbeidstaker200Response::class.java
        )

        assertEquals(1.0, meterRegistry.counter(AAREG_ARBEIDSFORHOLD_OPPSLAG_NAVN,
            OPPSLAGSTYPE_METRIC_TAG_NAVN, FINN_TEKNISK_HISTORIKK_NAVN,
            ORG_NUMMER_METRIC_TAG_NAVN, testorg,
            DATABEHANDLER_METRIC_TAG_NAVN, testsupplier).count() - forrigeTeller)
    }
}