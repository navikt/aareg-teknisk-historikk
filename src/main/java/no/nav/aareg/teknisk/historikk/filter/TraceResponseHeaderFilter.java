package no.nav.aareg.teknisk.historikk.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import no.nav.aareg.teknisk.historikk.service.maskinporten.MaskinportenClaimsService;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static no.nav.aareg.teknisk.historikk.KonsumentLogging.MDC_CONSUMER_ID;
import static no.nav.aareg.teknisk.historikk.KonsumentLogging.MDC_SUPPLIER_ID;

@Component
@RequiredArgsConstructor
public class TraceResponseHeaderFilter extends OncePerRequestFilter {

    private final Tracer tracer;
    private final MaskinportenClaimsService maskinportenClaimsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (request.getRequestURI().startsWith("/api/")) {
            if (tracer.currentSpan() != null) {
                response.setHeader("X-Trace-Id", tracer.currentSpan().context().traceId());
            }

            var orgNummer = maskinportenClaimsService.hentOrgnrFraToken();

            MDC.put(MDC_CONSUMER_ID, orgNummer.konsument());
            if (orgNummer.databehandler() != null) {
                MDC.put(MDC_SUPPLIER_ID, orgNummer.databehandler());
            }

            request.setAttribute(MDC_CONSUMER_ID, orgNummer.konsument());
            request.setAttribute(MDC_SUPPLIER_ID, orgNummer.databehandler());
        }

        filterChain.doFilter(request, response);
    }
}
