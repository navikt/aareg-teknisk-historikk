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
public class Arbeidsforhold {

    private String id;

    private String uuid;

    private Person arbeidstaker;

    private Identifikator opplysningspliktig;

    private Identifikator arbeidssted;

    private Kodeverksentitet type;

    private Kodeverksentitet rapporteringsordning;

    private List<Ansettelsesperiode> ansettelsesperioder;

    private Sporingsinformasjon sporingsinformasjon;
}
