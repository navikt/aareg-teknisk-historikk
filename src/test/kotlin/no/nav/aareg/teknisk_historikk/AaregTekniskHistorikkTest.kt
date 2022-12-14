package no.nav.aareg.teknisk_historikk

import org.springframework.boot.test.context.SpringBootTest

const val WIREMOCK_PORT = 23457
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
open class AaregTekniskHistorikkTest {
}