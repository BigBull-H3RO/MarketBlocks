# SingleOfferShop: Setup & Roles

## Grundsetup

1. Shop platzieren.
2. Als Owner Angebot konfigurieren:
   - Result-Item setzen
   - mindestens ein Payment setzen
3. Angebot aktivieren und Testkauf durchführen.

## Rollenmodell

- **Primary Owner**: volle Shopverwaltung
- **zusätzliche Owner**: erweiterte Verwaltungsrechte
- **Nicht-Owner**: können gemäß Shopzustand kaufen, aber nicht verwalten

## Tab-Rechte (Kurzfassung)

- **Offers**: allgemein zugänglich (zustandsabhängig)
- **Inventory**: nur Owner, sofern Admin-Shop nicht aktiv
- **Settings**: Owner oder OP bei globalem Admin-Mode
- **Log**: nur Owner

## Hinweise

- Alle kritischen Aktionen werden serverseitig validiert.
- Bei Rollenproblemen zuerst Ownership-Zustand prüfen.
