# Marketplace: Troubleshooting

## „Angebot kann nicht gekauft werden“

Prüfen:

- Payment-Stapel und Mengen korrekt?
- Daily-Limit erreicht?
- Stock-Limit erreicht?
- Runtime-State durch Restock/Demand beeinflusst?

## „Marketplace öffnet nicht“

Prüfen:

- Keybind korrekt belegt?
- Command-Berechtigung vorhanden?
- Läuft der Server ohne Fehler beim Laden der Marketplace-Daten?

## „Änderungen sind nicht sichtbar“

Prüfen:

- Wurde die Änderung serverseitig bestätigt?
- Sind Viewer neu synchronisiert?
- Falls JSON manuell bearbeitet wurde: Reload korrekt ausgeführt?

## „JSON scheint beschädigt“

Prüfen:

- Existiert die `.bak`-Datei?
- Letzte manuelle Änderung rückgängig machen
- Server-Logs auf Parse-/I/O-Fehler prüfen
