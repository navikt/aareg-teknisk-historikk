package no.nav.aareg.teknisk.historikk.config;

import lombok.Getter;

@Getter
public class UpstreamServiceException extends RuntimeException {

    private final String service;
    private final int statusCode;
    private final String responseBody;

    public UpstreamServiceException(String service, int statusCode, String responseBody) {
        super("Upstream call failed for " + service + " with status " + statusCode);
        this.service = service;
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }
}

