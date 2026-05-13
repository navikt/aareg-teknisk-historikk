package no.nav.aareg.teknisk.historikk.provider.api.contract;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TjenestefeilResponse {

    private String korrelasjonsid;
    private List<String> meldinger;
}
