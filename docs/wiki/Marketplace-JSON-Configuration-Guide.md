# Marketplace: JSON Configuration Guide

This page describes the structure and behavior of Marketplace data.

## Storage Location

Marketplace data is stored server-side at:

- `<world>/marketblocks/marketplace.json`

## Data Structure (High-Level)

- **MarketplaceData**
  - List of **MarketplacePage**
    - `name`
    - optional `icon`
    - List of **MarketplaceOffer**
      - `id` (UUID)
      - `result`
      - `payments` (max 2)
      - `limits`
      - `pricing`
      - `runtime_state`

## Limits

An offer can combine:

- Daily-Limit
- Stock-Limit
- Restock interval (seconds)

Values `<= 0` are treated as "not set".

## Demand Pricing

Pricing uses multipliers and scales payment costs dynamically:

- `enabled`
- `base_multiplier`
- `demand_step`
- `min_multiplier`
- `max_multiplier`

Effective payment amounts are rounded up (minimum 1).

## Runtime-State

At runtime, the Marketplace manages:

- remaining stock
- daily counter (global or per-player)
- restock timestamp
- demand state

## File Safety

Saving uses safety mechanisms:

- write via temporary file + replace/move
- backup file (`.bak`)
- restore from backup if the primary file is corrupted

## Notes

- JSON data is server-authoritative.
- Edit the file manually only if you fully understand the structure.
- For day-to-day operations, the in-game editor is the recommended path.
