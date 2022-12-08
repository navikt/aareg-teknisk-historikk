package no.nav.aareg.teknisk_historikk.aareg_services

import no.nav.aareg.teknisk_historikk.aareg_services.contract.Ansettelsesperiode
import no.nav.aareg.teknisk_historikk.aareg_services.contract.Arbeidsforhold
import no.nav.aareg.teknisk_historikk.aareg_services.contract.Identobjekt
import no.nav.aareg.teknisk_historikk.aareg_services.contract.Sporingsinformasjon
import no.nav.aareg.teknisk_historikk.models.Endringskilde
import no.nav.aareg.teknisk_historikk.models.Hovedenhet
import no.nav.aareg.teknisk_historikk.models.Person
import no.nav.aareg.teknisk_historikk.models.Underenhet
import java.time.ZoneOffset

val mapArbeidsforhold = { arbeidsforhold: Arbeidsforhold ->
    no.nav.aareg.teknisk_historikk.models.Arbeidsforhold().apply {
        id = arbeidsforhold.id
        uuid = arbeidsforhold.navUuid
        type = arbeidsforhold.type
        arbeidstaker = person(arbeidsforhold.arbeidstaker)
        arbeidssted = arbeidssted(arbeidsforhold.arbeidssted)
        opplysningspliktig = opplysningspliktig(arbeidsforhold.opplysningspliktig)
        rapporteringsordning = arbeidsforhold.rapporteringsordning
        ansettelsesperioder = arbeidsforhold.ansettelsesperioder?.map(mapAnsettelsesperiode)
        sporingsinformasjon = mapSporingsinformasjon(arbeidsforhold.sporingsinformasjon, arbeidsforhold.bruksperiode.tom != null)
    }
}

private val mapAnsettelsesperiode = { ansettelsesperiode: Ansettelsesperiode ->
    no.nav.aareg.teknisk_historikk.models.Ansettelsesperiode().apply {
        startdato = ansettelsesperiode.startdato
        sluttdato = ansettelsesperiode.sluttdato
        sluttaarsak = ansettelsesperiode.sluttaarsak
        sporingsinformasjon = mapSporingsinformasjon(ansettelsesperiode.sporingsinformasjon, ansettelsesperiode.bruksperiode.tom != null)
    }
}

private fun person(identobjekt: Identobjekt) = Person().apply {
    type = "Person"
    offentligIdent = identobjekt.identer.first { it.type == "FOLKEREGISTERIDENT" && it.gjeldende }.ident
}

private fun arbeidssted(identobjekt: Identobjekt) = identobjekt.let {
    if (it.type == "Underenhet") {
        Underenhet().apply {
            type = it.type
            offentligIdent = it.identer.first().ident
        }

    } else {
        person(it)
    }
}

private fun opplysningspliktig(identobjekt: Identobjekt) = identobjekt.let {
    if (it.type == "Hovedenhet") {
        Hovedenhet().apply {
            type = it.type
            offentligIdent = it.identer.first().ident
        }
    } else {
        person(it)
    }
}

private fun mapSporingsinformasjon(sporingsinformasjon: Sporingsinformasjon, avsluttet: Boolean) =
    no.nav.aareg.teknisk_historikk.models.Sporingsinformasjon().apply {
        opprettetTidspunkt = sporingsinformasjon.opprettetTidspunkt.atOffset(ZoneOffset.UTC)
        opprettetAv = mapEndringskilde(sporingsinformasjon.opprettetKilde)
        if (avsluttet) {
            slettetTidspunkt = sporingsinformasjon.endretTidspunkt.atOffset(ZoneOffset.UTC)
            slettetAv = mapEndringskilde(sporingsinformasjon.endretKilde)
        }
    }

fun mapEndringskilde(kilde: String): Endringskilde {
    return when (kilde) {
        "EDAG" -> Endringskilde.A_ORDNINGEN
        "KONVERTERING" -> Endringskilde.FOER_A_ORDNINGEN
        "AAREG" -> Endringskilde.SAKSBEHANDLER
        "GJENOPPBYGG_IM" -> Endringskilde.GJENOPPBYGGING
        "AVSLUTT_AF", "AVSLUTNING" -> Endringskilde.MASKINELT_AVSLUTTET
        "DB_SKRIPT", "OPPRYDNING", "STRT_SLUTT_ANSPER", "TILPASSET" -> Endringskilde.PATCH
        else -> throw IllegalArgumentException("Ukjent kilde")
    }
}
