# Developer Info

This page is for developers building addons, integrations, and server extensions.

## API/Addons: Entry Points

Important technical entry points in the code:

- `shop/marketplace/MarketplaceManager`
- `shop/marketplace/MarketplaceData`
- `shop/singleoffer/block/entity/SingleOfferShopBlockEntity`
- `shop/singleoffer/menu/SingleOfferShopMenu`
- `network/NetworkHandler`
- `config/Config`

## Events/Hooks

The mod is server-authoritative with clear network and lifecycle separation.

Especially relevant:

- server start/server tick/server stop flow for Marketplace runtime
- packet-based mutations with server-side validation
- viewer synchronization after relevant state changes

## Data Formats / Integration

- Marketplace persistence via JSON: `<world>/marketblocks/marketplace.json`
- Backup/restore strategy with `.bak`
- Runtime views are kept separate from persistent configuration

## Integration Principles

- No client-side assumptions for critical transactions
- Apply changes only through validated server paths
- For external tools, plan defensive parsing and fallbacks
