package no.nav.aareg.teknisk_historikk

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpHeaders
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtClaimNames
import org.springframework.security.oauth2.jwt.JwtDecoder
import java.time.Instant

const val WIREMOCK_PORT = 23457

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
open class AaregTekniskHistorikkTest {

    @MockBean
    lateinit var jwtDecoder: JwtDecoder
}

fun headerMedAutentisering() = HttpHeaders().apply { set("Authorization", "Bearer $testToken") }

const val testToken =
    "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzY29wZSI6Im5hdjphYXJlZy92MS9hcmJlaWRzZm9yaG9sZC90ZWtuaXNraGlzdG9yaWtrIiwiaXNzIjoiaHR0cHM6Ly92ZXIyLm1hc2tpbnBvcnRlbi5uby8iLCJjbGllbnRfYW1yIjoicHJpdmF0ZV9rZXlfand0IiwidG9rZW5fdHlwZSI6IkJlYXJlciIsImV4cCI6MTY3MTc4OTIwMywiaWF0IjoxNjcxNzg5MDgzLCJjbGllbnRfaWQiOiJhYmNkMTIzNCIsImp0aSI6ImFiY2QxMjM0IiwiY29uc3VtZXIiOnsiYXV0aG9yaXR5IjoiaXNvNjUyMy1hY3RvcmlkLXVwaXMiLCJJRCI6IjEyMzQ6MTIzNDU2Nzg5In19.G32zTpMUTznNWr1I55VhMjzJBFfu3FUEwOVye-az5E0"

const val testorg = "98765432"
const val testsupplier = "89765432"

private val jwtClaims = mapOf(
    JwtClaimNames.SUB to "testUser",
    "scope" to "nav:aareg/v1/arbeidsforhold/tekniskhistorikk",
    "consumer" to mapOf(
        "ID" to "1234:$testorg"
    )
)

val testJwt = Jwt(
    "token",
    Instant.now(),
    Instant.MAX,
    mapOf(
        "alg" to "none"
    ),
    jwtClaims
)

val testJwtMedDatabehandler = Jwt(
    "token",
    Instant.now(),
    Instant.MAX,
    mapOf(
        "alg" to "none"
    ),
    jwtClaims.plus(
        "supplier" to mapOf(
            "ID" to "1234:$testsupplier"
        )
    )
)