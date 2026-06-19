# Før du starter
Vær oppmerksom på at Intellj lagrer responsene under <code>.idea</code>. Dersom mulig, kjør tester mot helsyntetisk miljø (Q2)

Secrets rulleres ved deploy - så sjekk at <code>AZURE_APP_CLIENT_SECRET</code> er oppdatert dersom du opplever problemer.

Rar respons? Kjør en test alene istedet for å kjøre alle testene, kan være noe caching som ødelegger

# Oppsett

## Environment filer

Sammen med denne README filen finnes det en <code>http-client.env.json</code> fil som inneholder alle miljøvariablene som er nødvendige for å kjøre forespørslene.  
Det er anbefalt å opprette en kopi av denne filen og navngi den <code>http-client.private.env.json</code>.  
<code>http-client.private.env.json</code> er lagt til i <code>.gitignore</code> slik at vi slipper å legge ut secrets i repo, ta gjerne en sjekk av dette før du committer ny kode.


## Tilgang til deployet applikasjon

Token hentes fra aareg-maskinporten-token, bruk endepunkt i tekniskHistorikk.http