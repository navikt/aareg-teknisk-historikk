package no.nav.aareg.teknisk.historikk.audit;

public record Auditelement(
        String ressurs,
        String arbeidstakerId,
        String konsumentId,
        String databehandlerId,
        String kilde,
        String traceId
) {
}
