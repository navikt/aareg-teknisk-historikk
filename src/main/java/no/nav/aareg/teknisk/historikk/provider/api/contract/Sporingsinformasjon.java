package no.nav.aareg.teknisk.historikk.provider.api.contract;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Sporingsinformasjon {

    private OffsetDateTime opprettetTidspunkt;
    private Endringskilde opprettetAv;
    private OffsetDateTime slettetTidspunkt;
    private Endringskilde slettetAv;
}
