package no.nav.aareg.teknisk_historikk

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus

class ActuatorTest: AaregTekniskHistorikkTest() {
    @Autowired
    lateinit var testRestTemplate: TestRestTemplate

    @Test
    fun `health-endepunkt er tilgjengelig`(){
        val resultat = testRestTemplate.getForEntity("/actuator/health", String::class.java)

        assertEquals(HttpStatus.OK, resultat.statusCode)
        assertEquals("""
            {"status":"UP","groups":["liveness","readiness"]}
        """.trimIndent(), resultat.body)
    }

    @Test
    fun `liveness-probe er tilgjengelig`(){
        val resultat = testRestTemplate.getForEntity("/actuator/health/liveness", String::class.java)

        assertEquals(HttpStatus.OK, resultat.statusCode)
    }

    @Test
    fun `readiness-probe er tilgjengelig`(){
        val resultat = testRestTemplate.getForEntity("/actuator/health/readiness", String::class.java)

        assertEquals(HttpStatus.OK, resultat.statusCode)
    }

    @Test
    fun `prometheus-metrikker er tilgjengelig`(){
        val resultat = testRestTemplate.getForEntity("/actuator/prometheus", String::class.java)

        assertEquals(HttpStatus.OK, resultat.statusCode)
    }
}