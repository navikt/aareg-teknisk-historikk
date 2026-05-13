package no.nav.aareg.teknisk.historikk.config.security.identityprovider;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(IdentityProviderProperties.class)
public class IdentityProviderConfig {

    @Bean
    public IdentityProviderRegistry identityProviderRegistry(IdentityProviderProperties identityProviderProperties) {
        return new IdentityProviderRegistry(identityProviderProperties.getIdp());
    }
}
