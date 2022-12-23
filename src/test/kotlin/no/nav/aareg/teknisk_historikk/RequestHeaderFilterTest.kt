package no.nav.aareg.teknisk_historikk

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo
import com.github.tomakehurst.wiremock.junit5.WireMockTest
import no.nav.aareg.teknisk_historikk.api.gyldigSoekeparameter
import no.nav.aareg.teknisk_historikk.models.FinnTekniskHistorikkForArbeidstaker200Response
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders

@WireMockTest(httpPort = WIREMOCK_PORT)
class RequestHeaderFilterTest : AaregTekniskHistorikkTest() {
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
        WireMock.stubFor(
            WireMock.post("/token").willReturn(
                WireMock.okJson("{\"access_token\":\"testtoken\", \"expires_in\": 10000}")
            )
        )

        WireMock.stubFor(
            WireMock.get("/wellknown").willReturn(
                WireMock.okJson("{\"token_endpoint\":\"${wmRuntimeInfo.httpBaseUrl}/token\"}")
            )
        )
    }

    @Test
    fun `aareg-services mottar call-id og korrelasjons-id`(wmRuntimeInfo: WireMockRuntimeInfo) {
        WireMock.stubFor(
            WireMock.get(AAREG_SERVICES_URI)
                .withHeader("Authorization", WireMock.equalTo("Bearer testtoken"))
                .withHeader("Nav-Personident", WireMock.equalTo("123456789"))
                .withHeader("Nav-Call-Id", WireMock.equalTo("aareg-teknisk-historikk-test"))
                .withHeader(KORRELASJONSID_HEADER, WireMock.equalTo("test-korrelasjons-id"))
                .willReturn(WireMock.okJson("[]"))
        )

        val result = testRestTemplate.postForEntity(
            ENDEPUNKT_URI,
            HttpEntity(gyldigSoekeparameter(), HttpHeaders().apply { set(KORRELASJONSID_HEADER, "test-korrelasjons-id") }),
            FinnTekniskHistorikkForArbeidstaker200Response::class.java
        )

        Assertions.assertEquals(0, result.body?.antallArbeidsforhold)
    }
}