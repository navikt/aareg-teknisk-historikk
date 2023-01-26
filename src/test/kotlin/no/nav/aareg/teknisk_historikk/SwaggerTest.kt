package no.nav.aareg.teknisk_historikk

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus

class SwaggerTest : AaregTekniskHistorikkTest() {
    @Autowired
    lateinit var testRestTemplate: TestRestTemplate

    @Test
    fun `swagger-endepunkt er tilgjengelig`() {
        val resultat = testRestTemplate.getForEntity("/swagger-ui/index.html", String::class.java)

        Assertions.assertEquals(HttpStatus.OK, resultat.statusCode)
    }
}