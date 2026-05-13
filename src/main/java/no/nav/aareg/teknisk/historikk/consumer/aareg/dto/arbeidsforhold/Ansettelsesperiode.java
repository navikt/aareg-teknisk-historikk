package no.nav.aareg.teknisk.historikk.consumer.aareg.dto.arbeidsforhold;

import java.time.LocalDate;

public record Ansettelsesperiode(LocalDate startdato, LocalDate sluttdato, Kodeverksentitet sluttaarsak, Bruksperiode bruksperiode, Sporingsinformasjon sporingsinformasjon) {
}
