package no.nav.aareg.teknisk.historikk.config.security;

import lombok.RequiredArgsConstructor;
import no.nav.aareg.teknisk.historikk.filter.KonsumentFilter;
import no.nav.aareg.teknisk.historikk.service.maskinporten.MaskinportenClaimsService;
import no.nav.aareg.teknisk.historikk.config.security.identityprovider.IdentityProviderConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;


@Import({IdentityProviderConfig.class})
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final AaregAuthenticationResolver aaregAuthenticationResolver;
    private final MaskinportenClaimsService maskinportenClaimsService;

    @Bean
    KonsumentFilter konsumentFilter() {
        return new KonsumentFilter(maskinportenClaimsService);
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) {
        var pprm = PathPatternRequestMatcher.withDefaults();
        return httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .oauth2ResourceServer(oauth -> oauth.authenticationManagerResolver(aaregAuthenticationResolver))
                .authorizeHttpRequests(
                        request -> request
                                .requestMatchers(
                                        pprm.matcher("/actuator/**"),
                                        pprm.matcher("/swagger-ui/**"),
                                        pprm.matcher("/api-docs/**"),
                                        pprm.matcher("/openapi-spec.yaml")
                                ).permitAll()
                                .requestMatchers(
                                        pprm.matcher("/api/**")
                                ).hasAnyAuthority("SCOPE_nav:aareg/v1/arbeidsforhold/tekniskhistorikk")
                                .anyRequest().permitAll()
                )
                .addFilterAfter(konsumentFilter(), BearerTokenAuthenticationFilter.class)
                .build();
    }
}
