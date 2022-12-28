package no.nav.aareg.teknisk_historikk

import com.nimbusds.jose.util.JSONObjectUtils
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import java.text.ParseException

fun hentOrgnrFraToken(): OrgnummerFraMaskinportenToken {
    val principal = SecurityContextHolder.getContext().authentication.principal
    val claims = if (principal is Jwt) principal.claims else HashMap()
    var konsument = ""
    var databehandler: String? = null
    if (claims.containsKey("consumer")) {
        konsument = hentOrgnrFraClaim(claims, "consumer")
    } else {
        throw MaskinportenTokenException("Token mangler organisasjonsnummer for konsument")
    }
    if (claims.containsKey("supplier")) {
        databehandler = hentOrgnrFraClaim(claims, "supplier")
    }
    return OrgnummerFraMaskinportenToken(konsument, databehandler)
}

private fun hentOrgnrFraClaim(claims: Map<String, Any>, key: String): String {
    return try {
        JSONObjectUtils.getJSONObject(claims, key)["ID"].toString().split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]
    } catch (e: ParseException) {
        throw MaskinportenTokenException("Uventet feil ved parsing av claims")
    }
}

data class OrgnummerFraMaskinportenToken(val konsument: String, val databehandler: String?)

class MaskinportenTokenException(message: String) : Exception(message)