package no.nav.aareg.teknisk.historikk.service.maskinporten;

import no.nav.aareg.teknisk.historikk.config.security.exception.MaskinportenTokenException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import static com.nimbusds.jose.util.JSONObjectUtils.getJSONObject;
import static org.springframework.security.core.context.SecurityContextHolder.getContext;


@Service
public class MaskinportenClaimsService {

    public static final String CONSUMER = "consumer";
    public static final String SUPPLIER = "supplier";

    public OrgnummerFraMaskinportenToken hentOrgnrFraToken() {
        var principal = getContext().getAuthentication().getPrincipal();
        var claims = principal instanceof Jwt jwt ? jwt.getClaims() : new HashMap<String, Object>();

        String consumer;
        String supplier = null;
        if (claims.containsKey(CONSUMER)) {
            consumer = hentOrgnrFraClaim(claims, CONSUMER);
        } else {
            throw new MaskinportenTokenException("Token mangler organisasjonsnummer for konsument");
        }

        if (claims.containsKey(SUPPLIER)) {
            supplier = hentOrgnrFraClaim(claims, SUPPLIER);
        }

        return new OrgnummerFraMaskinportenToken(consumer, supplier);
    }

    private String hentOrgnrFraClaim(Map<String, Object> claims, String key) {
        try {
            return getJSONObject(claims, key).get("ID").toString().split(":")[1];
        } catch (ParseException e) {
            throw new MaskinportenTokenException("Uventet feil ved parsing av claims");
        }
    }
}
