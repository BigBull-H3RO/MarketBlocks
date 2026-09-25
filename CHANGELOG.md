# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [1.0.0] - 2026-09-25
### Added
- **Multi-Loader Support:** Full simultaneous support for **Fabric** and **NeoForge** on Minecraft 1.21.1.
- **Physical Player Shops:**
  - **Shop Crate:** Compact single-offer visual chest shop with item display and automated restocking.
  - **Shop Counter:** Visual shop counter block with interactive clerk register.
  - **Decorative Clerk NPC:** Ambient shopkeeper villager NPC that watches players and gives authentic audio feedback.
- **Server Marketplace:**
  - Dynamic server-wide marketplace terminal supporting global offers, pagination, and player listings.
  - Transaction history log and secure offline earnings collection.
- **Autonomous Wandering Traders:**
  - Wandering traders actively seek out and browse player shops, purchasing goods with realistic budgets across wealth tiers (Citizen, Wealthy, Noble).
- **Mod Compatibility:**
  - **Jade:** In-game inspection tooltips for shops and traders with privacy controls.
  - **JEI:** Integration for trading items and currency blocks.
  - **JourneyMap:** Shop waypoint integration.
- **Localization & Configuration:**
  - In-depth configuration via `main.toml`, `trader.toml`, and `client.toml`.
  - Translations for English (`en_us`), German (`de_de`), Spanish (`es_es`), and French (`fr_fr`).
