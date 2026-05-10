# Marketplace: Dynamic Pricing & Limits

## Limits Overview

An offer can use these limits at the same time:

- **Daily limit** (per day)
- **Stock limit** (inventory)
- **Restock** (refill over time)

## Daily Limit: Global vs Per Player

Controlled by configuration:

- `marketplaceGlobalDailyLimit`

Behavior:

- **enabled**: all players share one global daily counter per offer
- **disabled**: each player has an individual daily counter per offer

## Demand Pricing

Demand pricing adjusts effective payment costs based on demand.

Important parameters:

- Enabled (`enabled`)
- Base value (`base_multiplier`)
- Demand step (`demand_step`)
- Lower bound (`min_multiplier`)
- Upper bound (`max_multiplier`)

## Runtime-Upkeep

Regular background upkeep updates:

- daily resets
- restock cycles
- demand decay

## Recommendations for Server Operation

- Start with conservative limits and tune based on real usage.
- Define price bounds (`min_multiplier`/`max_multiplier`) to avoid extreme swings.
- Adjust restock values to your server economy and gameplay pace.
