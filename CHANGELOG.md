# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [1.0.0+26.2] - 2026-10-02
### Added
- **Minecraft 26.2 Support:** Initial release with full feature parity on Fabric & NeoForge for Minecraft 26.2.

### Changed
- **Marketplace Keybind:** Default keybind updated to `K` (previously `O`) to prevent collision with Minecraft 26.2's vanilla Friends list.

## [1.0.0+26.1.2] - 2026-10-01
### Added
- **Minecraft 26.1.2 Support:** Initial release with full feature parity on Fabric & NeoForge for Minecraft 26.1.2.

## [1.0.0+1.21.11] - 2026-09-30
### Added
- **Minecraft 1.21.11 Support:** Initial release with full feature parity on Fabric & NeoForge for Minecraft 1.21.11.

## [1.0.0+1.21.4] - 2026-09-28
### Added
- **Minecraft 1.21.4 Support:** Initial release with full feature parity on Fabric & NeoForge for Minecraft 1.21.4.

## [1.0.0+1.21.1] - 2026-09-27
### Added
- **Multi-Loader Support:** Full simultaneous support for **Fabric** and **NeoForge** on Minecraft 1.21.1.
- **Physical Player Shops:**
  - **Trade Stand:** 2-block-tall counter with spinning 3D display item inside a protective glass case.
  - **Market Crate:** Rustic 1-block crate with dynamically draining visual stock rendering.
  - **Custom Clerk NPCs:** Visual shopkeeper clerk supporting 15 Villager professions and custom player skins.
  - **Flexible Pricing:** 2-to-1 payment item combinations and safe bulk purchasing (Shift-click).
  - **Grief Protection:** Complete immunity to explosions and unauthorized breaking.
- **Autonomous Wandering Traders (AI Shoppers):**
  - Wandering Traders actively browse player market stalls and buy goods for passive income.
  - 3 distinct customer wealth tiers: Citizen, Wealthy, and Noble.
  - Living behaviors with custom animations, sound reactions, and carried purchased items.
- **Central Server Marketplace:**
  - Server-wide economy hub accessible via hotkey (`O`) or linked decorative counters.
  - Live in-game admin editor for categories and offers (no restarts or JSON needed).
  - Dynamic demand pricing system with automatic cooling curves.
- **Mod Compatibility:**
  - **Jade:** Live in-crosshair HUD displaying item previews, prices, stock, and owner info.
  - **JourneyMap:** Real-time map markers for shops/marketplace and waypoint creation.
  - **Xaero's Minimap & World Map:** Clickable chat waypoint suggestions (`/mb search <item>`).
  - **Just Enough Items (JEI):** Native GUI layout protection preventing panel overlap.
  - **FTB Chunks:** Native block tag interaction whitelist for claimed territories.
- **Localization & Configuration:**
  - Full configuration via `main.toml`, `trader.toml`, `singleoffer.toml`, and `client.toml`.
  - Native translations for English (`en_us`), German (`de_de`), Spanish (`es_es`), and French (`fr_fr`).
