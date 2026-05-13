package no.nav.aareg.teknisk.historikk.provider.api.contract;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Soekeparametere {

    private String arbeidstaker;
    private String arbeidssted;
    private String opplysningspliktig;
}
