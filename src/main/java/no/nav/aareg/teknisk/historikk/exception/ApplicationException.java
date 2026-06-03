package no.nav.aareg.teknisk.historikk.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Getter
@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public class ApplicationException extends RuntimeException {

    public ApplicationException(String melding) {
        this(melding, null);
    }

    public ApplicationException(String message, Throwable cause) {
        super(message, cause);
    }

}
