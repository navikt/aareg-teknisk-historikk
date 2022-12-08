package no.nav.aareg.teknisk_historikk.api

import no.nav.aareg.teknisk_historikk.AaregServicesConsumer
import no.nav.aareg.teknisk_historikk.models.FinnTekniskHistorikkForArbeidstaker200Response
import no.nav.aareg.teknisk_historikk.models.Soekeparametere
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.ok
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.context.request.ServletWebRequest
import java.util.*
import java.util.Optional.ofNullable
import java.util.logging.Logger
import javax.servlet.http.HttpServletRequest

@RestController
class ArbeidsforholdApi(
    val aaregServicesConsumer: AaregServicesConsumer
) : ApiApi {
    private val log = Logger.getLogger(this.javaClass.name)

    @Autowired
    lateinit var httpservletRequest: HttpServletRequest

    override fun getRequest(): Optional<NativeWebRequest> = ofNullable(ServletWebRequest(httpservletRequest))

    override fun finnTekniskHistorikkForArbeidstaker(soekeparametere: Soekeparametere): ResponseEntity<FinnTekniskHistorikkForArbeidstaker200Response> {
        return ok(aaregServicesConsumer.hentArbeidsforholdForArbeidstaker(soekeparametere))
    }
}