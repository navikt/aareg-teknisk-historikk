package no.nav.aareg.teknisk.historikk.provider.api;

import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import no.nav.aareg.teknisk.historikk.exception.IngenTilgangException;
import no.nav.aareg.teknisk.historikk.provider.api.contract.Soekeparametere;
import no.nav.aareg.teknisk.historikk.provider.api.contract.TekniskHistorikkMapper;
import no.nav.aareg.teknisk.historikk.service.TekniskHistorikkService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static no.nav.aareg.teknisk.historikk.KonsumentLogging.loggOppslag;

@RestController
@RequestMapping("/api/v1/arbeidsforhold")
@RequiredArgsConstructor
public class TekniskHistorikkController {

    private static final String FINN_TEKNISK_HISTORIKK_NAVN = "TekniskHistorikkForArbeidstaker";

    private final MeterRegistry meterRegistry;
    private final HttpServletRequest httpServletRequest;
    private final TekniskHistorikkService tekniskHistorikkService;
    private final TekniskHistorikkMapper mapper;

    @PostMapping
    public ResponseEntity<no.nav.aareg.teknisk.historikk.provider.api.contract.TekniskHistorikkResponse> finnTekniskHistorikkForArbeidstaker(@RequestBody @Validated Soekeparametere soekeparametere) {
        try {
            var domeneResponse = tekniskHistorikkService.hentTekniskHistorikk(soekeparametere);
            return ResponseEntity.ok(mapper.map(domeneResponse));
        } catch (IngenTilgangException e) {
            var feiltekst = e.getFeilkode().getFeiltekst();
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(mapper.map(feiltekst));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapper.map("Uventet feil - kontakt brukerstøtte"));
        } finally {
            loggOppslag(meterRegistry, httpServletRequest, FINN_TEKNISK_HISTORIKK_NAVN);
        }
    }
}
