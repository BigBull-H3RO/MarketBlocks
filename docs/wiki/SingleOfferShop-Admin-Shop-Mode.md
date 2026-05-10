# SingleOfferShop: Admin Shop Mode

## Purpose

Admin shop mode is designed for server-controlled shops without classic inventory management.

## Requirements

- Global admin mode must be enabled (`marketblocksAdminModeEnabled`).
- Toggling is only possible with operator rights.

## Behavior

With admin shop mode enabled:

- no input stock required for sales
- no output capacity check
- inventory tab is inaccessible for safety/UX reasons

## Recommendation

- Use only for clearly defined server shops.
- Regularly review prices and payment items administratively.
