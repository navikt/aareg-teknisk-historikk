package no.nav.aareg.teknisk.historikk.provider.api;

import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import no.nav.aareg.teknisk.historikk.consumer.aareg.AaregServicesConsumer;
import no.nav.aareg.teknisk.historikk.provider.api.contract.Arbeidsforhold;
import no.nav.aareg.teknisk.historikk.provider.api.contract.Soekeparametere;
import no.nav.aareg.teknisk.historikk.provider.api.contract.TjenestefeilResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.json.JsonMapper;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static no.nav.aareg.teknisk.historikk.KonsumentLogging.loggOppslag;

@RestController
@RequestMapping("/api/v1/arbeidsforhold")
@RequiredArgsConstructor
public class TekniskHistorikkController {

    private static final String KORRELASJONSID_HEADER = "correlation-id";
    private static final String ARBEIDSTAKER_ER_PAAKREVD = "arbeidstaker er et pakrevd felt";
    private static final String ARBEIDSTAKER_MAA_VAERE_TALL = "arbeidstaker ma kun vaere tall";
    private static final String OPPLYSNINGSPLIKTIG_MAA_VAERE_TALL = "opplysningspliktig ma kun vaere tall";
    private static final String ARBEIDSSTED_MAA_VAERE_TALL = "arbeidssted ma kun vaere tall";
    private static final String FINN_TEKNISK_HISTORIKK_NAVN = "TekniskHistorikkForArbeidstaker";
    private static final Pattern ALL_NUMBERS_PATTERN = Pattern.compile("\\d+");
    private static final String IKKE_LESBAR_FEILMELDING =
            "Kunne ikke lese sokeforesporselen. Eksempel pa gyldig json: " + eksempelSoek();

    private final AaregServicesConsumer aaregServicesConsumer;
    private final MeterRegistry meterRegistry;
    private final HttpServletRequest httpServletRequest;

    @PostMapping
    public ResponseEntity<TekniskHistorikkResponse> finnTekniskHistorikkForArbeidstaker(@RequestBody Soekeparametere soekeparametere) {
        validerSoekeparametere(soekeparametere);

        var consumerRequest = new no.nav.aareg.teknisk.historikk.consumer.aareg.dto.Soekeparametere(
                soekeparametere.getArbeidstaker(),
                soekeparametere.getArbeidssted(),
                soekeparametere.getOpplysningspliktig()
        );

        var respons = aaregServicesConsumer.hentTekniskHistorikk(consumerRequest);
        var arbeidsforholdliste = respons.arbeidsforholdListe();

        loggOppslag(meterRegistry, httpServletRequest, FINN_TEKNISK_HISTORIKK_NAVN, arbeidsforholdliste.size());

        return ResponseEntity.ok(new TekniskHistorikkResponse(arbeidsforholdliste.size(), arbeidsforholdliste));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<TjenestefeilResponse> ikkeLesbarFeil(HttpMessageNotReadableException ignored, HttpServletRequest request) {
        return tjenestefeilRespons(request, HttpStatus.BAD_REQUEST, IKKE_LESBAR_FEILMELDING);
    }

    @ExceptionHandler(Valideringsfeil.class)
    public ResponseEntity<TjenestefeilResponse> valideringsfeil(Valideringsfeil exception, HttpServletRequest request) {
        return tjenestefeilRespons(request, HttpStatus.BAD_REQUEST, exception.getFeilmeldinger());
    }

    private void validerSoekeparametere(Soekeparametere soekeparametere) {
        var valideringsfeil = Stream.of(
                soekeparametere.getArbeidstaker() == null ? ARBEIDSTAKER_ER_PAAKREVD : null,
                soekeparametere.getArbeidstaker() == null || !ALL_NUMBERS_PATTERN.matcher(soekeparametere.getArbeidstaker()).matches() ? ARBEIDSTAKER_MAA_VAERE_TALL : null,
                soekeparametere.getOpplysningspliktig() != null && !ALL_NUMBERS_PATTERN.matcher(soekeparametere.getOpplysningspliktig()).matches() ? OPPLYSNINGSPLIKTIG_MAA_VAERE_TALL : null,
                soekeparametere.getArbeidssted() != null && !ALL_NUMBERS_PATTERN.matcher(soekeparametere.getArbeidssted()).matches() ? ARBEIDSSTED_MAA_VAERE_TALL : null
        ).filter(Objects::nonNull).toList();

        if (!valideringsfeil.isEmpty()) {
            throw new Valideringsfeil(valideringsfeil);
        }
    }

    private static String eksempelSoek() {
        try {
            var eksempel = new Soekeparametere();
            eksempel.setArbeidstaker("12345678912");
            eksempel.setOpplysningspliktig(null);
            eksempel.setArbeidssted(null);
            return new JsonMapper().writerFor(Soekeparametere.class).writeValueAsString(eksempel);
        } catch (Exception ignored) {
            return "{\"arbeidstaker\":\"12345678912\",\"opplysningspliktig\":null,\"arbeidssted\":null}";
        }
    }

    private ResponseEntity<TjenestefeilResponse> tjenestefeilRespons(HttpServletRequest request, HttpStatus status, String... feilmeldinger) {
        return tjenestefeilRespons(request, status, Arrays.asList(feilmeldinger));
    }

    private ResponseEntity<TjenestefeilResponse> tjenestefeilRespons(HttpServletRequest request, HttpStatus status, List<String> feilmeldinger) {
        var response = new TjenestefeilResponse();
        response.setKorrelasjonsid((String) request.getAttribute(KORRELASJONSID_HEADER));
        response.setMeldinger(feilmeldinger);
        return ResponseEntity.status(status).body(response);
    }

    public record TekniskHistorikkResponse(int antallArbeidsforhold, List<Arbeidsforhold> arbeidsforhold) {
    }

    public static class Valideringsfeil extends RuntimeException {
        private final List<String> feilmeldinger;

        public Valideringsfeil(List<String> feilmeldinger) {
            this.feilmeldinger = feilmeldinger;
        }

        public List<String> getFeilmeldinger() {
            return feilmeldinger;
        }
    }
}
