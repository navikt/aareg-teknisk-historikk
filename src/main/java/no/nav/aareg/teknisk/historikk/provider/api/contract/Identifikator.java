package no.nav.aareg.teknisk.historikk.provider.api.contract;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = Person.class, name = "Person"),
    @JsonSubTypes.Type(value = Underenhet.class, name = "Underenhet"),
    @JsonSubTypes.Type(value = Hovedenhet.class, name = "Hovedenhet")
})
public interface Identifikator {
    String getType();
    String getOffentligIdent();
}
