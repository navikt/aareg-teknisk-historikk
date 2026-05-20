package no.nav.aareg.teknisk.historikk;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import no.nav.aareg.teknisk.historikk.config.UpstreamServiceException;
import no.nav.aareg.teknisk.historikk.provider.api.contract.TjenestefeilResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Order
@ControllerAdvice
public class Feilmeldinger {

    private static final String KORRELASJONSID_HEADER = "correlation-id";

    private static final Marker TEAM_LOGS_MARKER = MarkerFactory.getMarker("TEAM_LOGS");

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<TjenestefeilResponse> feilMediaType(HttpMediaTypeNotSupportedException exception, HttpServletRequest httpServletRequest) {
        return tjenestefeilRespons(
                httpServletRequest,
                HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "Feil mediatype: " + httpServletRequest.getContentType(),
                "Stottede typer: " + exception.getSupportedMediaTypes().stream()
                        .map(Object::toString)
                        .collect(Collectors.joining(", "))
        );
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<TjenestefeilResponse> feilRequestType(HttpRequestMethodNotSupportedException exception, HttpServletRequest httpServletRequest) {
        var supportedHttpMethods = exception.getSupportedHttpMethods();
        String supportedMethods = supportedHttpMethods == null
                ? null
                : supportedHttpMethods.stream()
                .map(Object::toString)
                .collect(Collectors.joining(", "));

        return tjenestefeilRespons(
                httpServletRequest,
                HttpStatus.METHOD_NOT_ALLOWED,
                "Http-verb ikke tillatt: " + httpServletRequest.getMethod(),
                "Verb som er stottet: " + supportedMethods
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<TjenestefeilResponse> generiskFeil(Exception exception, HttpServletRequest httpServletRequest) {
        log.error("Uhandtert feil oppstod. Sjekk sikker log for detaljer");
        log.error(TEAM_LOGS_MARKER, "Uhandtert feil oppstod", exception);
        return tjenestefeilRespons(httpServletRequest, HttpStatus.INTERNAL_SERVER_ERROR, "En ukjent feil oppstod");
    }

    @ExceptionHandler(UpstreamServiceException.class)
    public ResponseEntity<TjenestefeilResponse> upstreamFeil(UpstreamServiceException exception, HttpServletRequest request) {
        if ("aareg-services".equals(exception.getService())) {
            return switch (exception.getStatusCode()) {
                case 401 -> tjenestefeilRespons(request, HttpStatus.OK, Feilkode.AAREG_SERVICES_UNAUTHORIZED.toString());
                case 403 -> tjenestefeilRespons(request, HttpStatus.OK, Feilkode.AAREG_SERVICES_FORBIDDEN.toString());
                case 404 -> tjenestefeilRespons(request, HttpStatus.NOT_FOUND, exception.getResponseBody());
                case 200 -> tjenestefeilRespons(request, HttpStatus.OK, Feilkode.AAREG_SERVICES_MALFORMED.toString());
                default -> tjenestefeilRespons(request, HttpStatus.INTERNAL_SERVER_ERROR, Feilkode.AAREG_SERVICES_ERROR.toString());
            };
        }

        if ("Texas".equals(exception.getService()) || "Azure".equals(exception.getService())) {
            return tjenestefeilRespons(request, HttpStatus.INTERNAL_SERVER_ERROR, Feilkode.AZURE_KONSUMENT_FEIL.toString());
        }

        return tjenestefeilRespons(request, HttpStatus.INTERNAL_SERVER_ERROR, "En ukjent feil oppstod");
    }

    private ResponseEntity<TjenestefeilResponse> tjenestefeilRespons(HttpServletRequest request, HttpStatus status, String... feilmeldinger) {
        return tjenestefeilRespons(request, status, Arrays.asList(feilmeldinger));
    }

    private ResponseEntity<TjenestefeilResponse> tjenestefeilRespons(HttpServletRequest request, HttpStatus status, List<String> feilmeldinger) {
        TjenestefeilResponse response = new TjenestefeilResponse();
        response.setKorrelasjonsid((String) request.getAttribute(KORRELASJONSID_HEADER));
        response.setMeldinger(feilmeldinger);
        return ResponseEntity.status(status).body(response);
    }
}

