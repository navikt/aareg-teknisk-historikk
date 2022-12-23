package no.nav.aareg.teknisk_historikk

import org.slf4j.MDC
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.UUID.randomUUID
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

const val KORRELASJONSID_HEADER = "correlation-id"

@Component
class RequestHeaderFilter : OncePerRequestFilter() {
    override fun doFilterInternal(req: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        MDC.put(KORRELASJONSID_HEADER, req.getHeader(KORRELASJONSID_HEADER) ?: randomUUID().toString())

        filterChain.doFilter(req, response)
    }
}