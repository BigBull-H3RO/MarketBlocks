# SingleOfferShop: Admin Shop Mode

## Zweck

Der Admin-Shop-Modus ist für servergesteuerte Shops ohne klassisches Bestandsmanagement gedacht.

## Voraussetzungen

- Globaler Admin-Mode muss aktiviert sein (`marketblocksAdminModeEnabled`).
- Umschalten ist nur mit OP-Rechten möglich.

## Verhalten

Bei aktivem Admin-Shop-Modus:

- kein Input-Bestand für Verkäufe erforderlich
- keine Output-Kapazitätsprüfung
- Inventory-Tab ist aus Sicherheits-/UX-Gründen nicht zugänglich

## Empfehlung

- Nur für klar definierte Server-Shops einsetzen.
- Preise und Payment-Items regelmäßig administrativ prüfen.
