Aareg-teknisk-historikk
================

API for utlevering av teknisk historikk tilknyttet arbeidsforhold

# Bruk av API

## Request

Konsumenter med rettigheter til APIet kan sende en POST-request til `/api/v1/teknisk-historikk` med bearer token fra maskinporten og følgende body:

```json
{
  "arbeidstaker": "FNR/DNR 11 siffer",
  "arbeidssted": "FNR/DNR/ORGNR",
  "opplysningspliktig": "FNR/DNR/ORGNR",
  "fraOgMed": "2000-01-01",
  "tilOgMed": "9999-01-01"
}
```

`arbeidstaker` er påkrevd, alle andre felt er valgfrie.

## Response

Responsen vil inneholde en liste med arbeidsforhold, eventuelle feilmeldinger og en traceId som kan benyttes ved oppfølging av feilsituasjoner.

Eksempel respons:

```json
{
  "arbeidsforhold": [
    {
      "arbeidsforholdId": 1,
      "arbeidsforholdType": "ordinaertArbeidsforhold",
      "opplysningspliktig": {
        "type": "Underenhet",
        "offentligIdent": "1234"
      },
      "arbeidssted": {
        "type": "Hovedenhet",
        "offentligIdent": "12345"
      },
      "arbeidstaker": {
        "type": "Person",
        "offentligIdent": "123456"
      },
      "ansattFra": "2006-01-25",
      "ansattTil": "2026-03-25",
      "endringstype": "ENDRET",
      "endringstidspunkt": "2026-05-07T11:52:00"
    }
  ],
  "feilmeldinger": [],
  "traceId": "12345abc"
}
```



---

# Henvendelser

Spørsmål knyttet til koden eller prosjektet kan stilles som issues her på GitHub

## For NAV-ansatte

Interne henvendelser kan sendes via Slack i kanalen #team-arbeidsforhold.
