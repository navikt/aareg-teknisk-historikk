package no.nav.aareg.teknisk.historikk;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.client.RestTestClient;

@DirtiesContext
class ApiDocsTest extends AaregTekniskHistorikkTest {

    @Autowired
    private RestTestClient restTestClient;

    @Test
    void apiDocsEndepunktErTilgjengelig() {
        restTestClient.get().uri("/api-docs").exchange()
                .expectStatus().isOk();
    }

    @Test
    void swaggerEndepunktErTilgjengelig() {
        restTestClient.get().uri("/swagger-ui/index.html").exchange()
                .expectStatus().isOk();
    }

    @Test
    void specFilErTilgjengelig() {
        restTestClient.get().uri("/openapi-spec.yaml").exchange()
                .expectStatus().isOk();
    }
}

