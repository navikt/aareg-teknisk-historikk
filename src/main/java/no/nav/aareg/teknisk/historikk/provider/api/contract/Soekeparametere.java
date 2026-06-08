package no.nav.aareg.teknisk.historikk.provider.api.contract;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Soekeparametere {

    @NotNull(message = "arbeidstakerId is required")
    @Pattern(regexp = "^\\d{11}$", message = "arbeidstakerId må være nummerisk")
    private String arbeidstaker;

    @Pattern(regexp = "^\\d{9,11}$", message = "arbeidsstedId må være nummerisk")
    private String arbeidssted;

    @Pattern(regexp = "^\\d{9,11}$", message = "arbeidsstedId må være nummerisk")
    private String opplysningspliktig;

    private LocalDate fraOgMed;
    private LocalDate tilOgMed;
}
