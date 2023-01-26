package no.nav.aareg.teknisk_historikk

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus
import org.springframework.test.annotation.DirtiesContext

@DirtiesContext
class ApiDocsTest : AaregTekniskHistorikkTest() {
    @Autowired
    lateinit var testRestTemplate: TestRestTemplate

    @Test
    fun `api-docs-endepunkt er tilgjengelig`() {
        val resultat = testRestTemplate.getForEntity("/api-docs", String::class.java)

        Assertions.assertEquals(HttpStatus.OK, resultat.statusCode)
    }

    @Test
    fun `swagger-endepunkt er tilgjengelig`() {
        val resultat = testRestTemplate.getForEntity("/swagger-ui/index.html", String::class.java)

        Assertions.assertEquals(HttpStatus.OK, resultat.statusCode)
    }

    @Test
    fun `spec-fil er tilgjengelig`() {
        val resultat = testRestTemplate.getForEntity("/openapi-spec.yaml", String::class.java)

        Assertions.assertEquals(HttpStatus.OK, resultat.statusCode)
    }
}