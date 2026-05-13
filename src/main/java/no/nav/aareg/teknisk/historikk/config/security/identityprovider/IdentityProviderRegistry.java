package no.nav.aareg.teknisk.historikk.config.security.identityprovider;

import no.nav.aareg.teknisk.historikk.config.security.exception.OidcException;

import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;
import static org.apache.logging.log4j.util.Strings.isBlank;

public class IdentityProviderRegistry {

    private final Map<String, IdentityProvider> idpByIssuerMap = new HashMap<>();
    private final Map<String, IdentityProvider> idpByNameMap = new HashMap<>();

    public IdentityProviderRegistry(Map<String, IdentityProvider> identityProviders) {
        identityProviders.forEach(this::add);
        idpByNameMap.putAll(identityProviders);
    }

    private void add(String name, IdentityProvider identityProvider) {
        if (isBlank(identityProvider.getIssuerUrl())) {
            throw new OidcException(format("IdentityProvider with name: %s does not have issuer url", name));
        }
        if (isBlank(identityProvider.getJwkSetUri())) {
            throw new OidcException(format("IdentityProvider with name: %s does not have jwkSet uri", name));
        }
        idpByIssuerMap.put(identityProvider.getIssuerUrl(), identityProvider);
    }

    public IdentityProvider getIdp(String idpName) {
        return idpByIssuerMap.get(idpName);
    }

    public IdentityProvider getIdpByName(String idpName) {
        return idpByNameMap.get(idpName);
    }
}
