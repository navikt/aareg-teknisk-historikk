package no.nav.aareg.teknisk.historikk.provider.api.contract;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ansettelsesperiode {

    private LocalDate startdato;
    private LocalDate sluttdato;
    private Kodeverksentitet sluttaarsak;
    private Sporingsinformasjon sporingsinformasjon;
}
