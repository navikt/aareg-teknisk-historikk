package no.nav.aareg.teknisk.historikk.provider.api.contract;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record Arbeidsforhold(
        Long arbeidsforholdId,
        String arbeidsforholdType,
        Identifikator opplysningspliktig,
        Identifikator arbeidssted,
        Person arbeidstaker,
        LocalDate ansattFra,
        LocalDate ansattTil,
        String endringstype,
        LocalDateTime endringstidspunkt
) {
}
