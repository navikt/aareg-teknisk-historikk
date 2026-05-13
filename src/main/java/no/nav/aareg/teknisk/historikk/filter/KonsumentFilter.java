package no.nav.aareg.teknisk.historikk.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import no.nav.aareg.teknisk.historikk.service.maskinporten.MaskinportenClaimsService;
import org.jspecify.annotations.NonNull;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static no.nav.aareg.teknisk.historikk.KonsumentLogging.MDC_CONSUMER_ID;
import static no.nav.aareg.teknisk.historikk.KonsumentLogging.MDC_SUPPLIER_ID;

@RequiredArgsConstructor
public class KonsumentFilter extends OncePerRequestFilter {

    private final MaskinportenClaimsService maskinportenClaimsService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest req, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        if (req.getRequestURI().startsWith("/api/")) {
            var orgNummer = maskinportenClaimsService.hentOrgnrFraToken();

            MDC.put(MDC_CONSUMER_ID, orgNummer.konsument());
            if (orgNummer.databehandler() != null) {
                MDC.put(MDC_SUPPLIER_ID, orgNummer.databehandler());
            }

            req.setAttribute(MDC_CONSUMER_ID, orgNummer.konsument());
            req.setAttribute(MDC_SUPPLIER_ID, orgNummer.databehandler());
        }

        filterChain.doFilter(req, response);
    }
}

