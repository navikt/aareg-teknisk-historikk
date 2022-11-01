package no.nav.aareg.teknisk_historikk

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.HttpStatusEntryPoint

@Profile("!test")
@EnableWebSecurity
open class SecurityConfig {
    @Bean
    @Throws(Exception::class)
    open fun filterChain(http: HttpSecurity): SecurityFilterChain {
        return http.authorizeHttpRequests {
            it.antMatchers("/api/**")
                .hasAuthority("SCOPE_${SCOPE_KONTROLL_API}")
        }
            .exceptionHandling { it.authenticationEntryPoint(HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)) }
            .build()
    }

    companion object {
        private const val SCOPE_KONTROLL_API = "nav:aareg/v1/arbeidsforhold/kontroll"
    }
}

@Profile("test")
@Configuration
open class NoSecurityConfig {
    @Bean
    @Throws(Exception::class)
    open fun filterChain(http: HttpSecurity): SecurityFilterChain {
        return http.authorizeHttpRequests {
            it.antMatchers("**").permitAll()
        }
            .build()
    }
}