package no.nav.aareg.teknisk_historikk

enum class Feilkode(val feilkode: String, val feiltekst: String) {
    AAREG_SERVICES_ERROR("AS1", "Feil ved henting av arbeidsforhold"),
    AAREG_SERVICES_UNAUTHORIZED("AS2", "Feil ved henting av arbeidsforhold"),
    AAREG_SERVICES_MALFORMED("AS3", "Feil ved henting av arbeidsforhold"),
    AAREG_SERVICES_FORBIDDEN("AS4", "Du mangler tilgang til ressursen"),
    AZURE_KONSUMENT_FEIL("AD1", "En feil oppstod");

    override fun toString(): String {
        return "$feilkode: $feiltekst"
    }
}