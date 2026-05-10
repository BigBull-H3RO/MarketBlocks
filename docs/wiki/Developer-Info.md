# Developer Info

Diese Seite richtet sich an Entwickler von Addons, Integrationen und Server-Erweiterungen.

## API/Addons: Einstieg

Wichtige technische Einstiegspunkte im Code:

- `shop/marketplace/MarketplaceManager`
- `shop/marketplace/MarketplaceData`
- `shop/singleoffer/block/entity/SingleOfferShopBlockEntity`
- `shop/singleoffer/menu/SingleOfferShopMenu`
- `network/NetworkHandler`
- `config/Config`

## Events/Hooks

Die Mod arbeitet serverautoritativ mit klarer Netzwerk- und Lifecycle-Trennung.

Relevant sind insbesondere:

- Serverstart/Servertick/Serverstop-Fluss für Marketplace-Runtime
- Paketbasierte Mutationen mit serverseitiger Validierung
- Viewer-Synchronisierung nach relevanten Zustandsänderungen

## Datenformate/Integration

- Marketplace-Persistenz über JSON: `<world>/marketblocks/marketplace.json`
- Backup-/Restore-Strategie mit `.bak`
- Runtime-Ansichten werden getrennt von persistenter Konfiguration geführt

## Integrationsprinzipien

- Keine clientseitigen Annahmen für kritische Transaktionen
- Änderungen nur über validierte Serverpfade
- Bei externen Tools: defensives Parsing und Fallbacks einplanen
