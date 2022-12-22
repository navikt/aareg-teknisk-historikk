package no.nav.aareg.teknisk_historikk.api

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.aareg.teknisk_historikk.aareg_services.AaregServicesConsumer
import no.nav.aareg.teknisk_historikk.models.FinnTekniskHistorikkForArbeidstaker200Response
import no.nav.aareg.teknisk_historikk.models.Soekeparametere
import no.nav.aareg.teknisk_historikk.tjenestefeilRespons
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.ok
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.context.request.ServletWebRequest
import java.util.*
import java.util.Optional.ofNullable
import javax.servlet.http.HttpServletRequest

val EKSEMPEL_SOEK = ObjectMapper().writerFor(Soekeparametere::class.java).writeValueAsString(Soekeparametere().apply {
    arbeidstakerident = "12345678912"
    opplysningspliktig = null
    arbeidsstedident = null
})

val IKKE_LESBAR_FEILMELDING = "Kunne ikke lese søkeforespørselen. Eksempel på gyldig søk: $EKSEMPEL_SOEK"
const val ARBEIDSTAKER_ER_PAAKREVD = "arbeidstakerident er et påkrevd felt"
const val ARBEIDSTAKER_MAA_VAERE_TALL = "arbeidstakerident må kun være tall"
const val OPPLYSNINGSPLIKTIG_MAA_VAERE_TALL = "opplysningspliktig må kun være tall"
const val ARBEIDSSTED_MAA_VAERE_TALL = "arbeidssted må kun være tall"

@RestController
class ArbeidsforholdApi(
    private val aaregServicesConsumer: AaregServicesConsumer
) : ApiApi {

    @Autowired
    lateinit var httpservletRequest: HttpServletRequest

    override fun getRequest(): Optional<NativeWebRequest> = ofNullable(ServletWebRequest(httpservletRequest))

    override fun finnTekniskHistorikkForArbeidstaker(soekeparametere: Soekeparametere): ResponseEntity<FinnTekniskHistorikkForArbeidstaker200Response> {
        validerSoekeparametere(soekeparametere)
        return ok(aaregServicesConsumer.hentArbeidsforholdForArbeidstaker(soekeparametere))
    }

    private fun validerSoekeparametere(soekeparametere: Soekeparametere) {
        val allNumbersMatcher = "\\d+".toRegex()
        val valideringsfeil = listOf(
            if (soekeparametere.arbeidstakerident == null) ARBEIDSTAKER_ER_PAAKREVD else null,
            if (soekeparametere.arbeidstakerident?.matches(allNumbersMatcher) != true) ARBEIDSTAKER_MAA_VAERE_TALL else null,
            if (soekeparametere.opplysningspliktig != null && !soekeparametere.opplysningspliktig.matches(allNumbersMatcher)) OPPLYSNINGSPLIKTIG_MAA_VAERE_TALL else null,
            if (soekeparametere.arbeidsstedident != null && !soekeparametere.arbeidsstedident.matches(allNumbersMatcher)) ARBEIDSSTED_MAA_VAERE_TALL else null
        ).filterNotNull()

        if (valideringsfeil.isNotEmpty())
            throw Valideringsfeil(valideringsfeil)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun ikkeLesbarFeil(exception: HttpMessageNotReadableException, httpServletRequest: HttpServletRequest) =
        tjenestefeilRespons(HttpStatus.BAD_REQUEST, IKKE_LESBAR_FEILMELDING)

    @ExceptionHandler(Valideringsfeil::class)
    fun ikkeLesbarFeil(exception: Valideringsfeil, httpServletRequest: HttpServletRequest) =
        tjenestefeilRespons(HttpStatus.BAD_REQUEST, exception.feilmeldinger)
}

class Valideringsfeil(val feilmeldinger: List<String>) : Throwable()