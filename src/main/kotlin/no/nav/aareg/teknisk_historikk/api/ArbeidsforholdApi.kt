package no.nav.aareg.teknisk_historikk.api

import com.fasterxml.jackson.databind.ObjectMapper
import io.micrometer.core.instrument.MeterRegistry
import no.nav.aareg.teknisk_historikk.aareg_services.AaregServicesKonsument
import no.nav.aareg.teknisk_historikk.aareg_services.mapArbeidsforhold
import no.nav.aareg.teknisk_historikk.loggOppslag
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
    arbeidstaker = "12345678912"
    opplysningspliktig = null
    arbeidssted = null
})

val IKKE_LESBAR_FEILMELDING = "Kunne ikke lese søkeforespørselen. Eksempel på gyldig json: $EKSEMPEL_SOEK"
const val ARBEIDSTAKER_ER_PAAKREVD = "arbeidstaker er et påkrevd felt"
const val ARBEIDSTAKER_MAA_VAERE_TALL = "arbeidstaker må kun være tall"
const val OPPLYSNINGSPLIKTIG_MAA_VAERE_TALL = "opplysningspliktig må kun være tall"
const val ARBEIDSSTED_MAA_VAERE_TALL = "arbeidssted må kun være tall"
const val FINN_TEKNISK_HISTORIKK_NAVN = "TekniskHistorikkForArbeidstaker"

@RestController
class ArbeidsforholdApi(private val aaregServicesKonsument: AaregServicesKonsument, private val meterRegistry: MeterRegistry) : ApiApi {

    @Autowired
    private lateinit var httpservletRequest: HttpServletRequest

    override fun getRequest(): Optional<NativeWebRequest> = ofNullable(ServletWebRequest(httpservletRequest))

    override fun finnTekniskHistorikkForArbeidstaker(soekeparametere: Soekeparametere): ResponseEntity<FinnTekniskHistorikkForArbeidstaker200Response> {
        validerSoekeparametere(soekeparametere)
        val arbeidsforholdliste = aaregServicesKonsument.hentArbeidsforholdForArbeidstaker(soekeparametere)

        meterRegistry.loggOppslag(httpservletRequest, FINN_TEKNISK_HISTORIKK_NAVN, arbeidsforholdliste.size)

        return ok(FinnTekniskHistorikkForArbeidstaker200Response().apply {
            antallArbeidsforhold = arbeidsforholdliste.size
            arbeidsforhold = arbeidsforholdliste.map(mapArbeidsforhold)
        })
    }

    private fun validerSoekeparametere(soekeparametere: Soekeparametere) {
        val allNumbersMatcher = "\\d+".toRegex()
        val valideringsfeil = listOf(
            if (soekeparametere.arbeidstaker == null) ARBEIDSTAKER_ER_PAAKREVD else null,
            if (soekeparametere.arbeidstaker?.matches(allNumbersMatcher) != true) ARBEIDSTAKER_MAA_VAERE_TALL else null,
            if (soekeparametere.opplysningspliktig != null && !soekeparametere.opplysningspliktig.matches(allNumbersMatcher)) OPPLYSNINGSPLIKTIG_MAA_VAERE_TALL else null,
            if (soekeparametere.arbeidssted != null && !soekeparametere.arbeidssted.matches(allNumbersMatcher)) ARBEIDSSTED_MAA_VAERE_TALL else null
        ).filterNotNull()

        if (valideringsfeil.isNotEmpty())
            throw Valideringsfeil(valideringsfeil)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun ikkeLesbarFeil(exception: HttpMessageNotReadableException, httpServletRequest: HttpServletRequest) =
        tjenestefeilRespons(httpServletRequest, HttpStatus.BAD_REQUEST, IKKE_LESBAR_FEILMELDING)

    @ExceptionHandler(Valideringsfeil::class)
    fun valideringsfeil(exception: Valideringsfeil, httpServletRequest: HttpServletRequest) =
        tjenestefeilRespons(httpServletRequest, HttpStatus.BAD_REQUEST, exception.feilmeldinger)
}

class Valideringsfeil(val feilmeldinger: List<String>) : Throwable()