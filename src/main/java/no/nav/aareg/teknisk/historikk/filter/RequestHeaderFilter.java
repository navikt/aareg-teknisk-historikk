package no.nav.aareg.teknisk.historikk.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class RequestHeaderFilter extends OncePerRequestFilter {

    public static final String KORRELASJONSID_HEADER = "correlation-id";

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            var korrelasjonsid = req.getHeader(KORRELASJONSID_HEADER);
            if (korrelasjonsid == null || korrelasjonsid.isBlank()) {
                korrelasjonsid = UUID.randomUUID().toString();
            }

            req.setAttribute(KORRELASJONSID_HEADER, korrelasjonsid);
            response.setHeader(KORRELASJONSID_HEADER, korrelasjonsid);

            filterChain.doFilter(req, response);
        } finally {
            // no-op
        }
    }
}

