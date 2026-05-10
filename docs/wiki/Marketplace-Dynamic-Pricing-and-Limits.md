# Marketplace: Dynamische Preise & Limits

## Limits im Überblick

Ein Offer kann gleichzeitig folgende Grenzen nutzen:

- **Daily-Limit** (pro Tag)
- **Stock-Limit** (Bestand)
- **Restock** (Wiederauffüllung nach Zeit)

## Daily-Limit: global vs. pro Spieler

Gesteuert über die Konfiguration:

- `marketplaceGlobalDailyLimit`

Verhalten:

- **aktiv**: alle Spieler teilen einen globalen Daily-Counter pro Offer
- **inaktiv**: jeder Spieler hat einen eigenen Daily-Counter pro Offer

## Demand Pricing

Demand Pricing passt die effektiven Payment-Kosten anhand der Nachfrage an.

Wichtige Parameter:

- Aktivierung (`enabled`)
- Basiswert (`base_multiplier`)
- Steigerung (`demand_step`)
- Untergrenze (`min_multiplier`)
- Obergrenze (`max_multiplier`)

## Runtime-Upkeep

Regelmäßige Hintergrundprozesse aktualisieren:

- Daily-Resets
- Restock-Zyklen
- Demand-Decay

## Empfehlung für Serverbetrieb

- Limits zuerst konservativ setzen und anhand realer Nutzung nachjustieren.
- Preisgrenzen (`min_multiplier`/`max_multiplier`) definieren, um extreme Ausschläge zu vermeiden.
- Restock-Werte an Server-Ökonomie und Spieltempo anpassen.
