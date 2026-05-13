package no.nav.aareg.teknisk.historikk.consumer.aareg.dto.arbeidsforhold;

import java.time.LocalDateTime;

public record Sporingsinformasjon(LocalDateTime opprettetTidspunkt, LocalDateTime endretTidspunkt, String opprettetKilde, String endretKilde) {
}
