package no.nav.aareg.teknisk_historikk

import io.micrometer.core.instrument.MeterRegistry
import org.slf4j.MDC
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

const val AAREG_ARBEIDSFORHOLD_OPPSLAG_NAVN = "aareg_arbeidsforhold_oppslag"
const val MDC_CONSUMER_ID = "consumerId"
const val MDC_SUPPLIER_ID = "supplierId"

const val OPPSLAGSTYPE_METRIC_TAG_NAVN = "oppslagstype"
const val ORG_NUMMER_METRIC_TAG_NAVN = "organisasjon"
const val DATABEHANDLER_METRIC_TAG_NAVN = "databehandler"

@Component
class KonsumentFilter : OncePerRequestFilter() {
    override fun doFilterInternal(req: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        try {
            val orgNummer = hentOrgnrFraToken()

            MDC.put(MDC_CONSUMER_ID, orgNummer.konsument)
            if (orgNummer.databehandler != null) MDC.put(MDC_SUPPLIER_ID, orgNummer.databehandler)

            req.setAttribute(MDC_CONSUMER_ID, orgNummer.konsument)
            req.setAttribute(MDC_SUPPLIER_ID, orgNummer.databehandler)

            filterChain.doFilter(req, response)
        } finally {
            MDC.remove(KORRELASJONSID_HEADER)
        }
    }
}

fun MeterRegistry.loggOppslag(request: HttpServletRequest, oppslagsnavn: String, size: Int) {
    this.counter(
        AAREG_ARBEIDSFORHOLD_OPPSLAG_NAVN,
        OPPSLAGSTYPE_METRIC_TAG_NAVN, oppslagsnavn,
        ORG_NUMMER_METRIC_TAG_NAVN, request.getAttribute(MDC_CONSUMER_ID) as String,
        DATABEHANDLER_METRIC_TAG_NAVN, (request.getAttribute(MDC_SUPPLIER_ID) as String?) ?: ""
    )
        .increment(size.toDouble())
}