package no.nav.aareg.teknisk_historikk.api

import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo
import com.github.tomakehurst.wiremock.junit5.WireMockTest
import no.nav.aareg.teknisk_historikk.AaregTekniskHistorikkTest
import no.nav.aareg.teknisk_historikk.models.FinnTekniskHistorikkForArbeidstaker200Response
import no.nav.aareg.teknisk_historikk.models.Soekeparametere
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import java.nio.charset.StandardCharsets.UTF_8

@WireMockTest
class ArbeidsforholdV1ApiTest : AaregTekniskHistorikkTest() {
    @Autowired
    lateinit var testRestTemplate: TestRestTemplate

    companion object {
        val ENDEPUNKT_URI = "/api/v1/arbeidsforhold"

        @JvmStatic
        @BeforeAll
        internal fun setup(wmRuntimeInfo: WireMockRuntimeInfo) {
            System.setProperty("wiremock.server.baseUrl", wmRuntimeInfo.httpBaseUrl)
        }
    }

    @BeforeEach
    fun setup(wmRuntimeInfo: WireMockRuntimeInfo) {
        val arbeidsforholdString = hentDataFraRessurs("mocks/arbeidsforhold1.json")
        stubFor(
            post("/api/v1/arbeidsforhold")
                .withHeader("Authorization", equalTo("Bearer testtoken"))
                .willReturn(okJson(arbeidsforholdString))
        )

        stubFor(
            post("/token").willReturn(
                okJson("{\"access_token\":\"testtoken\", \"expires_in\": 10000}")
            )
        )
    }

    @Test
    fun arbeidsforholdApiTest(wmRuntimeInfo: WireMockRuntimeInfo) {
        val result = testRestTemplate.postForEntity(
            ENDEPUNKT_URI,
            HttpEntity(Soekeparametere().apply { arbeidstakerident = "123456789" }),
            FinnTekniskHistorikkForArbeidstaker200Response::class.java
        )
        Assertions.assertEquals(1, result.body?.antallArbeidsforhold ?: -1)
    }

    private fun hentDataFraRessurs(filsti: String) = this::class.java.classLoader.getResourceAsStream(filsti)?.let {
        String(it.readAllBytes(), UTF_8)
    }
}