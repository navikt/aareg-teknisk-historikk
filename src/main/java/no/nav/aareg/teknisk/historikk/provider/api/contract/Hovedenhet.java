package no.nav.aareg.teknisk.historikk.provider.api.contract;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Hovedenhet implements Identifikator {

    @Builder.Default
    private String type = "Hovedenhet";

    private String offentligIdent;
}
