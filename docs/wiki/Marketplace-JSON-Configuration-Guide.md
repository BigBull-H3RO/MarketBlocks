# Marketplace: JSON Configuration Guide

Diese Seite beschreibt den Aufbau und das Verhalten der Marketplace-Daten.

## Speicherort

Marketplace-Daten werden serverseitig unter folgendem Pfad gespeichert:

- `<world>/marketblocks/marketplace.json`

## Datenstruktur (High-Level)

- **MarketplaceData**
  - Liste von **MarketplacePage**
    - `name`
    - optional `icon`
    - Liste von **MarketplaceOffer**
      - `id` (UUID)
      - `result`
      - `payments` (max. 2)
      - `limits`
      - `pricing`
      - `runtimeState`

## Limits

Ein Offer kann kombinieren:

- Daily-Limit
- Stock-Limit
- Restock-Intervall (Sekunden)

Werte `<= 0` gelten als „nicht gesetzt“.

## Demand Pricing

Pricing nutzt Multiplikatoren und skaliert die Payment-Kosten dynamisch:

- `enabled`
- `base multiplier`
- `demand step`
- `min/max multiplier`

Die effektiven Payment-Mengen werden aufgerundet (mindestens 1).

## Runtime-State

Zur Laufzeit verwaltet der Marketplace u. a.:

- verbleibenden Stock
- Daily-Counter (global oder pro Spieler)
- Restock-Zeitpunkt
- Nachfragezustand (Demand)

## Dateisicherheit

Die Speicherung erfolgt mit Sicherheitsmechanismen:

- Schreiben über temporäre Datei + Replace/Move
- Backup-Datei (`.bak`)
- Restore aus Backup bei defekter Primärdatei

## Hinweise

- JSON-Daten sind serverautoritativ.
- Änderungen sollten nur erfolgen, wenn der Aufbau klar verstanden ist.
- Für Alltagsverwaltung im Betrieb ist die Ingame-Editor-Funktion der empfohlene Weg.
