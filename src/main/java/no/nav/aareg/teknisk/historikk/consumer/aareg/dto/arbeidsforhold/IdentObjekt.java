package no.nav.aareg.teknisk.historikk.consumer.aareg.dto.arbeidsforhold;

import java.util.List;

public record IdentObjekt(String type, List<Ident> identer) {
}
