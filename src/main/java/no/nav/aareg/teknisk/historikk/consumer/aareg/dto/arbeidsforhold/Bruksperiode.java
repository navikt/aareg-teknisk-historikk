package no.nav.aareg.teknisk.historikk.consumer.aareg.dto.arbeidsforhold;

import java.time.LocalDateTime;

public record Bruksperiode(LocalDateTime fom, LocalDateTime tom) {
}
