package no.nav.aareg.teknisk.historikk;

import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.client.RestTestClient;

import static no.nav.aareg.teknisk.historikk.KonsumentLogging.AAREG_TEKNISK_HISTORIKK_OPPSLAG_NAVN;
import static no.nav.aareg.teknisk.historikk.KonsumentLogging.DATABEHANDLER_METRIC_TAG_NAVN;
import static no.nav.aareg.teknisk.historikk.KonsumentLogging.OPPSLAGSTYPE_METRIC_TAG_NAVN;
import static no.nav.aareg.teknisk.historikk.KonsumentLogging.ORG_NUMMER_METRIC_TAG_NAVN;
import static no.nav.aareg.teknisk.historikk.api.ApiTestData.TEKNISK_HISTORIKK_RESPONSE_1;
import static no.nav.aareg.teknisk.historikk.api.ApiTestData.gyldigSoekeparameter;
import static org.junit.jupiter.api.Assertions.assertEquals;

class KonsumentLoggingTest extends AaregTekniskHistorikkTest {

    private static final String ENDEPUNKT_URI = "/api/v1/arbeidsforhold";
    private static final String FINN_TEKNISK_HISTORIKK_NAVN = "TekniskHistorikkForArbeidstaker";


    @Autowired
    private RestTestClient restTestClient;

    @Autowired
    private MeterRegistry meterRegistry;

    @Test
    void loggerOppslagsmetrikkerForKonsumenter() {
        aaregServicesStub.stubLegacyTekniskHistorikk(200, TEKNISK_HISTORIKK_RESPONSE_1);

        double forrigeTeller = meterRegistry.counter(
                AAREG_TEKNISK_HISTORIKK_OPPSLAG_NAVN,
                OPPSLAGSTYPE_METRIC_TAG_NAVN, FINN_TEKNISK_HISTORIKK_NAVN,
                ORG_NUMMER_METRIC_TAG_NAVN, TESTORG,
                DATABEHANDLER_METRIC_TAG_NAVN, ""
        ).count();

        restTestClient.post()
                .uri(ENDEPUNKT_URI)
                .headers(headers -> headers.putAll(headerMedAutentisering()))
                .body(gyldigSoekeparameter())
                .exchange()
                .expectStatus().isOk();

        assertEquals(1.0, meterRegistry.counter(
                AAREG_TEKNISK_HISTORIKK_OPPSLAG_NAVN,
                OPPSLAGSTYPE_METRIC_TAG_NAVN, FINN_TEKNISK_HISTORIKK_NAVN,
                ORG_NUMMER_METRIC_TAG_NAVN, TESTORG,
                DATABEHANDLER_METRIC_TAG_NAVN, ""
        ).count() - forrigeTeller);
    }

    @Test
    void loggerOppslagsmetrikkerForDatabehandlere() {
        aaregServicesStub.stubLegacyTekniskHistorikk(200, TEKNISK_HISTORIKK_RESPONSE_1);

        double forrigeTeller = meterRegistry.counter(
                AAREG_TEKNISK_HISTORIKK_OPPSLAG_NAVN,
                OPPSLAGSTYPE_METRIC_TAG_NAVN, FINN_TEKNISK_HISTORIKK_NAVN,
                ORG_NUMMER_METRIC_TAG_NAVN, TESTORG,
                DATABEHANDLER_METRIC_TAG_NAVN, TESTSUPPLIER
        ).count();

        restTestClient.post()
                .uri(ENDEPUNKT_URI)
                .headers(headers -> headers.putAll(headerMedAutentiseringMedDatabehandler()))
                .body(gyldigSoekeparameter())
                .exchange()
                .expectStatus().isOk();

        assertEquals(1.0, meterRegistry.counter(
                AAREG_TEKNISK_HISTORIKK_OPPSLAG_NAVN,
                OPPSLAGSTYPE_METRIC_TAG_NAVN, FINN_TEKNISK_HISTORIKK_NAVN,
                ORG_NUMMER_METRIC_TAG_NAVN, TESTORG,
                DATABEHANDLER_METRIC_TAG_NAVN, TESTSUPPLIER
        ).count() - forrigeTeller);
    }
}

