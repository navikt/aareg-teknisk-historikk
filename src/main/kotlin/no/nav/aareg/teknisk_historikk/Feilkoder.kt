package no.nav.aareg.teknisk_historikk

enum class Feilkoder(val feilkode: String, val feiltekst: String) {
    AAREG_SERVICES_ERROR("AS1", "Feil oppstod ved henting av arbeidsforhold"),
    AAREG_SERVICES_UNAUTHORIZED("AS2", "Feil oppstod ved henting av arbeidsforhold"),
    AAREG_SERVICES_FORBIDDEN("AS3", "Feil oppstod ved henting av arbeidsforhold"),
    AAREG_SERVICES_MALFORMED("AS4", "Feil oppstod ved henting av arbeidsforhold"),
    AZURE_TOKEN_FAILED("AD1", "Feil oppstod ved henting av arbeidsforhold");

    override fun toString(): String {
        return "$feilkode: $feiltekst"
    }
}