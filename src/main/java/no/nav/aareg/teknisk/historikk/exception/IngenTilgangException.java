package no.nav.aareg.teknisk.historikk.exception;

import lombok.Getter;
import no.nav.aareg.teknisk.historikk.Feilkode;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Getter
@ResponseStatus(value = HttpStatus.FORBIDDEN)
public class IngenTilgangException extends RuntimeException {

    private Feilkode feilkode;

    public IngenTilgangException(Feilkode feilkode) {
        super(feilkode.getFeiltekst());
        this.feilkode = feilkode;
    }
}
