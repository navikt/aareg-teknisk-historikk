package no.nav.aareg.teknisk_historikk

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.UUID.randomUUID

const val KORRELASJONSID_HEADER = "correlation-id"

@Component
class RequestHeaderFilter : OncePerRequestFilter() {
    override fun doFilterInternal(req: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        try {
            val korrelasjonsid = req.getHeader(KORRELASJONSID_HEADER) ?: randomUUID().toString()

            MDC.put(KORRELASJONSID_HEADER, korrelasjonsid)
            req.setAttribute(KORRELASJONSID_HEADER, korrelasjonsid)
            response.addHeader(KORRELASJONSID_HEADER, korrelasjonsid)

            filterChain.doFilter(req, response)
        } finally {
            MDC.remove(KORRELASJONSID_HEADER)
        }
    }
}