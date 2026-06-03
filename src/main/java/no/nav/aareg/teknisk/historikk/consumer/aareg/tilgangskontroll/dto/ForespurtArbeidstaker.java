package no.nav.aareg.teknisk.historikk.consumer.aareg.tilgangskontroll.dto;

public record ForespurtArbeidstaker(
        String opplysningspliktigIdentifikator,
        String arbeidsstedIdentifikator,
        String arbeidstakerIdentifikator
) {
}
