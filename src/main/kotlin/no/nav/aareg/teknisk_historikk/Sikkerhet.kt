package no.nav.aareg.teknisk_historikk

import org.springframework.context.annotation.Bean
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.HttpStatusEntryPoint

@EnableWebSecurity
open class SecurityConfig {
    @Bean
    open fun filterChain(http: HttpSecurity): SecurityFilterChain {
        return http.csrf().disable().authorizeHttpRequests {
            it.antMatchers("/api/**")
                .hasAnyAuthority("SCOPE_${SCOPE_KONTROLL_API}")
        }
            .exceptionHandling { it.authenticationEntryPoint(HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)) }
            .oauth2ResourceServer { obj -> obj.jwt() }
            .build()
    }
}

const val SCOPE_KONTROLL_API = "nav:aareg/v1/arbeidsforhold/tekniskhistorikk"