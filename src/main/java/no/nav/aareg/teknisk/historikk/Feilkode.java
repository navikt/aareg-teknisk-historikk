package no.nav.aareg.teknisk.historikk;

public enum Feilkode {
    AAREG_SERVICES_ERROR("AS1", "Feil ved henting av arbeidsforhold"),
    AAREG_SERVICES_UNAUTHORIZED("AS2", "Feil ved henting av arbeidsforhold"),
    AAREG_SERVICES_MALFORMED("AS3", "Feil ved henting av arbeidsforhold"),
    AAREG_SERVICES_FORBIDDEN("AS4", "Du mangler tilgang til ressursen"),
    AZURE_KONSUMENT_FEIL("AD1", "En feil oppstod");

    private final String feilkode;
    private final String feiltekst;

    Feilkode(String feilkode, String feiltekst) {
        this.feilkode = feilkode;
        this.feiltekst = feiltekst;
    }

    public String getFeilkode() {
        return feilkode;
    }

    public String getFeiltekst() {
        return feiltekst;
    }

    @Override
    public String toString() {
        return feilkode + ": " + feiltekst;
    }
}

