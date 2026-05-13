package no.nav.aareg.teknisk.historikk.consumer.aareg;

import lombok.RequiredArgsConstructor;
import no.nav.aareg.teknisk.historikk.config.UpstreamServiceException;
import no.nav.aareg.teknisk.historikk.consumer.aareg.dto.Soekeparametere;
import no.nav.aareg.teknisk.historikk.consumer.aareg.dto.TekniskHistorikkResponse;
import no.nav.aareg.teknisk.historikk.consumer.texas.TexasConsumer;
import no.nav.aareg.teknisk.historikk.filter.RequestHeaderFilter;
import no.nav.aareg.teknisk.historikk.service.maskinporten.MaskinportenClaimsService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import jakarta.servlet.http.HttpServletRequest;

@Component
@RequiredArgsConstructor
public class AaregServicesConsumer {

    private final RestClient aaregServicesRestClient;
    private final TexasConsumer texasConsumer;
    private final MaskinportenClaimsService maskinportenClaimsService;
    private final ObjectProvider<HttpServletRequest> httpServletRequestProvider;

    @Value("${app.texas.target.aareg.services}")
    private String target;

    @Value("${app.name}")
    private String appName;

    public TekniskHistorikkResponse hentTekniskHistorikk(Soekeparametere soekeparametere) {
        var orgNummer = maskinportenClaimsService.hentOrgnrFraToken();
        var httpServletRequest = httpServletRequestProvider.getIfAvailable();
        var response = aaregServicesRestClient.post()
                .uri("/todo")
                .headers(httpHeaders -> {
                    httpHeaders.setBearerAuth(texasConsumer.hentEntraToken(target));
                    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
                    httpHeaders.set("Nav-Konsument", orgNummer.konsument());
                    if (orgNummer.databehandler() != null) {
                        httpHeaders.set("Nav-Databehandler", orgNummer.databehandler());
                    }
                    httpHeaders.set("Nav-Call-Id", appName);
                    if (httpServletRequest != null) {
                        var korrelasjonsid = (String) httpServletRequest.getAttribute(RequestHeaderFilter.KORRELASJONSID_HEADER);
                        if (korrelasjonsid != null) {
                            httpHeaders.set(RequestHeaderFilter.KORRELASJONSID_HEADER, korrelasjonsid);
                        }
                    }
                    httpHeaders.set("Nav-Personident", soekeparametere.arbeidstaker());
                    if (soekeparametere.opplysningspliktig() != null) {
                        httpHeaders.set("Nav-Opplysningspliktigident", soekeparametere.opplysningspliktig());
                    }
                    if (soekeparametere.arbeidssted() != null) {
                        httpHeaders.set("Nav-Arbeidsstedident", soekeparametere.arbeidssted());
                    }
                })
                .body(soekeparametere)
                .retrieve()
                .body(TekniskHistorikkResponse.class);

        if (response == null || response.arbeidsforholdListe() == null) {
            throw new UpstreamServiceException("aareg-services", 200, "Malformed response from aareg-services");
        }

        return response;
    }
}
