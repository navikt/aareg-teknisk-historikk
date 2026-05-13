package no.nav.aareg.teknisk.historikk;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ActuatorTest extends AaregTekniskHistorikkTest {

    @Autowired
    private RestTestClient restTestClient;

    @Test
    void healthEndepunktErTilgjengelig() {
        restTestClient.get().uri("/actuator/health").exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("status").isEqualTo("UP")
                .jsonPath("groups").isArray()
                .jsonPath("groups").value(groups -> assertEquals(2, ((List<?>) groups).size()))
                .jsonPath("groups").value(groups -> assertEquals("liveness", ((List<?>) groups).get(0)))
                .jsonPath("groups").value(groups -> assertEquals("readiness", ((List<?>) groups).get(1)));
    }

    @Test
    void livenessProbeErTilgjengelig() {
        restTestClient.get().uri("/actuator/health/liveness").exchange()
                .expectStatus().isOk();
    }

    @Test
    void readinessProbeErTilgjengelig() {
        restTestClient.get().uri("/actuator/health/readiness").exchange()
                .expectStatus().isOk();
    }

    @Test
    void prometheusMetrikkerErTilgjengelig() {
        restTestClient.get().uri("/actuator/prometheus").exchange()
                .expectStatus().isOk();
    }
}

