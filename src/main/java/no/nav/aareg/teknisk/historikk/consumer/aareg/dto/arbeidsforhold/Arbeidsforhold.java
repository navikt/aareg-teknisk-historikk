package no.nav.aareg.teknisk.historikk.consumer.aareg.dto.arbeidsforhold;

import java.util.List;

public record Arbeidsforhold(String id, String navUuid, Kodeverksentitet type, IdentObjekt arbeidstaker,
                             IdentObjekt arbeidssted, IdentObjekt opplysningspliktig,
                             List<Ansettelsesperiode> ansettelsesperioder, Kodeverksentitet rapporteringsordning,
                             Sporingsinformasjon sporingsinformasjon, Bruksperiode bruksperiode) {
}

