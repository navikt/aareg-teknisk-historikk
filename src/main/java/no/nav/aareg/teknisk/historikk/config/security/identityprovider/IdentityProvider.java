package no.nav.aareg.teknisk.historikk.config.security.identityprovider;

import lombok.Data;

@Data
public class IdentityProvider {

    private String issuerUrl;
    private String jwkSetUri;
}
