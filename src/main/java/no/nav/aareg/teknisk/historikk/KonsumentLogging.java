package no.nav.aareg.teknisk.historikk;

import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.http.HttpServletRequest;

public final class KonsumentLogging {

    public static final String AAREG_ARBEIDSFORHOLD_OPPSLAG_NAVN = "aareg_arbeidsforhold_oppslag";
    public static final String MDC_CONSUMER_ID = "consumerId";
    public static final String MDC_SUPPLIER_ID = "supplierId";
    public static final String OPPSLAGSTYPE_METRIC_TAG_NAVN = "oppslagstype";
    public static final String ORG_NUMMER_METRIC_TAG_NAVN = "organisasjon";
    public static final String DATABEHANDLER_METRIC_TAG_NAVN = "databehandler";

    private KonsumentLogging() {
    }

    public static void loggOppslag(MeterRegistry meterRegistry, HttpServletRequest request, String oppslagsnavn, int size) {
        meterRegistry.counter(
                AAREG_ARBEIDSFORHOLD_OPPSLAG_NAVN,
                OPPSLAGSTYPE_METRIC_TAG_NAVN, oppslagsnavn,
                ORG_NUMMER_METRIC_TAG_NAVN, (String) request.getAttribute(MDC_CONSUMER_ID),
                DATABEHANDLER_METRIC_TAG_NAVN, (String) java.util.Objects.requireNonNullElse(request.getAttribute(MDC_SUPPLIER_ID), "")
        ).increment(size);
    }
}

