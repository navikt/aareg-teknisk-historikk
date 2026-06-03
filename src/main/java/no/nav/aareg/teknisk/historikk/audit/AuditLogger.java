package no.nav.aareg.teknisk.historikk.audit;

import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.aareg.kontrakter.teknisk.historikk.TekniskHistorikk;
import no.nav.aareg.teknisk.historikk.provider.api.contract.Soekeparametere;
import no.nav.aareg.teknisk.historikk.service.maskinporten.MaskinportenClaimsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import static java.time.Instant.now;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogger {

    private static final Logger AUDIT_LOGGER = LoggerFactory.getLogger("auditLogger");

    private final Tracer tracer;
    private final MaskinportenClaimsService maskinportenClaimsService;

    @Value("${app.name}")
    private String appName;

    public void loggOppslag(Soekeparametere soekeparametere, boolean harTilgang, boolean erAdressebeskyttet) {
        var epochInMillis = now().toEpochMilli();
        var arbeidstakerId = soekeparametere.getArbeidstaker();
        var auditelement = opprettAuditelement(arbeidstakerId);

        var auditmelding = lagAuditmelding(
                arbeidstakerId,
                auditelement,
                harTilgang,
                erAdressebeskyttet ? org.slf4j.event.Level.WARN : org.slf4j.event.Level.INFO,
                epochInMillis
        );

        AUDIT_LOGGER.info(auditmelding);
    }

    private String lagAuditmelding(
            String arbeidstakerId,
            Auditelement auditelement,
            boolean harTilgang,
            org.slf4j.event.Level loggnivaa,
            long epochInMillis
    ) {
        // CEF-format => CEF:0|Aareg|<kilde:applikasjonsnavn>|<versjon:1.0>|audit:access|<ressurs>|<loggnivå:INFO|WARN>|flexString1Label=Decision flexString1=<permit|deny> dproc=<konsument-id> suid=<databehandler-id/konsument-id> sproc=<call-id> end=<epoch-millis> duid=<arbeidstaker-id>

        var dproc = auditelement.konsumentId();
        var suid = StringUtils.hasText(auditelement.databehandlerId()) ? auditelement.databehandlerId() : dproc;
        return "CEF:0" + "|"
                + "Aareg" + "|"
                + auditelement.kilde() + "|"
                + "1.0" + "|"
                + "audit:access" + "|"
                + auditelement.ressurs() + "|"
                + loggnivaa.name() + "|"
                + "flexString1Label" + "=" + "Decision" + " "
                + "flexString1" + "=" + (harTilgang ? "permit" : "deny") + " "
                + "dproc" + "=" + dproc + " "
                + "suid" + "=" + suid + " "
                + "sproc" + "=" + auditelement.traceId() + " "
                + "end" + "=" + epochInMillis + " "
                + "duid" + "=" + arbeidstakerId;

    }

    private Auditelement opprettAuditelement(
            String arbeidstakerId
    ) {
        var konsumentId = maskinportenClaimsService.hentOrgnrFraToken().konsument();
        var databehandlerId = maskinportenClaimsService.hentOrgnrFraToken().databehandler();

        return new Auditelement(TekniskHistorikk.class.getSimpleName(), arbeidstakerId, konsumentId, databehandlerId, appName, tracer.currentSpan().context().traceId());
    }
}