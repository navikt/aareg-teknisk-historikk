package no.nav.aareg.teknisk.historikk.api;

import no.nav.aareg.kontrakter.teknisk.historikk.TekniskHistorikk;
import no.nav.aareg.kontrakter.teknisk.historikk.TekniskHistorikkResponse;
import no.nav.aareg.teknisk.historikk.provider.api.contract.Soekeparametere;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public final class ApiTestData {

    private ApiTestData() {
    }

    public static Soekeparametere gyldigSoekeparameter() {
        Soekeparametere soekeparametere = new Soekeparametere();
        soekeparametere.setArbeidstaker("12345678912");
        return soekeparametere;
    }

    public static final TekniskHistorikkResponse TEKNISK_HISTORIKK_RESPONSE_1 = new TekniskHistorikkResponse(
            List.of(new TekniskHistorikk(
                    123L,
                    "123456789",
                    "213456789",
                    "12345678912",
                    "ordinaertArbeidsforhold",
                    LocalDate.of(2014, 7 ,1),
                    LocalDate.of(2015,12,31),
                    "Endring",
                    LocalDateTime.of(2018,9,19, 12, 11, 20, 79)
            ))
    );
}

