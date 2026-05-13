package no.nav.aareg.teknisk.historikk.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "maskinporten")
public record MaskinportenConfig(
        String wellKnownUrl,
        String clientId,
        String clientJwk,
        String scopes
) {
}