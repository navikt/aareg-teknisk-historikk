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

fun headerMedAutentisering() = HttpHeaders().medAutentisering()
fun HttpHeaders.medAutentisering(): HttpHeaders {
    set("Authorization", "Bearer $testToken")
    return this
}

fun HttpHeaders.medKorrelasjonsid() = this.medKorrelasjonsid("korrelasjonsid")
fun HttpHeaders.medKorrelasjonsid(korrelasjonsid: String): HttpHeaders {
    set(KORRELASJONSID_HEADER, korrelasjonsid)
    return this
}

fun headerMedAutentiseringOgKorrelasjon() = headerMedAutentisering().medKorrelasjonsid()

const val testToken = "testmaskinportentoken"

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