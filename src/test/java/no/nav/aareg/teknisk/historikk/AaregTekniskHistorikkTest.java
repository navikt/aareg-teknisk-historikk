package no.nav.aareg.teknisk.historikk;

import no.nav.aareg.teknisk.historikk.wiremock.WireMockStubs;
import no.nav.aareg.teknisk.historikk.wiremock.aareg.services.AaregServicesStub;
import no.nav.aareg.teknisk.historikk.wiremock.maskinporten.MaskinportenStub;
import no.nav.aareg.teknisk.historikk.wiremock.texas.TexasStub;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;
import net.minidev.json.JSONObject;
import java.util.function.Function;

@ActiveProfiles("test")
@AutoConfigureRestTestClient
@WireMockStubs({
        TexasStub.class,
        AaregServicesStub.class,
        MaskinportenStub.class
})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, classes = Main.class)
public class AaregTekniskHistorikkTest {

    public static final int WIREMOCK_PORT = 23457;
    public static final String TEST_TOKEN = "testmaskinportentoken";
    public static final String TEST_AZURE_TOKEN = "testazuretoken";
    public static final String TESTORG = "98765432";
    public static final String TESTSUPPLIER = "89765432";
    public static final String KORRELASJONSID_HEADER = "correlation-id";
    public static final String TEST_SCOPE = "nav:aareg/v1/arbeidsforhold/tekniskhistorikk";

    private static Function<Boolean, String> tokenProvider = includeSupplier -> {
        throw new IllegalStateException("Maskinporten token provider is not initialized");
    };

    protected AaregServicesStub aaregServicesStub;

    protected MaskinportenStub maskinportenStub;

    @BeforeEach
    void setupMaskinportenStub() {
        tokenProvider = this::generateMaskinportenToken;
    }

    public static HttpHeaders headerMedAutentisering() {
        return medAutentisering(new HttpHeaders());
    }

    public static HttpHeaders medAutentisering(HttpHeaders headers) {
        headers.setBearerAuth(maskinportenToken(false));
        return headers;
    }

    public static HttpHeaders headerMedAutentiseringMedDatabehandler() {
        return medAutentiseringMedDatabehandler(new HttpHeaders());
    }

    public static HttpHeaders medAutentiseringMedDatabehandler(HttpHeaders headers) {
        headers.setBearerAuth(maskinportenToken(true));
        return headers;
    }

    public static HttpHeaders medKorrelasjonsid(HttpHeaders headers) {
        return medKorrelasjonsid(headers, "korrelasjonsid");
    }

    public static HttpHeaders medKorrelasjonsid(HttpHeaders headers, String korrelasjonsid) {
        headers.set(KORRELASJONSID_HEADER, korrelasjonsid);
        return headers;
    }

    public static HttpHeaders headerMedAutentiseringOgKorrelasjon() {
        return medKorrelasjonsid(headerMedAutentisering());
    }

    private static String maskinportenToken(boolean includeSupplier) {
        return tokenProvider.apply(includeSupplier);
    }

    private String generateMaskinportenToken(boolean includeSupplier) {
        var overrides = new java.util.HashMap<String, Object>();
        overrides.put("consumer", identClaim(TESTORG));
        if (includeSupplier) {
            overrides.put("supplier", identClaim(TESTSUPPLIER));
            return maskinportenStub.generateTokenWithOverrides(TEST_SCOPE, overrides);
        }
        return maskinportenStub.generateTokenWithOverrides(TEST_SCOPE, overrides, "supplier");
    }

    private static JSONObject identClaim(String orgnummer) {
        return new JSONObject(Map.of("authority", "iso", "ID", "0192:" + orgnummer));
    }
}

