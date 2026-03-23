# MarketBlocks AI Guidelines

## Project Overview
MarketBlocks is a Minecraft NeoForge 1.21.1 mod enabling player-driven economy via custom shop blocks.
Key Features: Player shops with input/output chest integration, owner security, redstone signals.

## Architecture & Code Organization
- **Main Entry Point**: `de.bigbull.marketblocks.MarketBlocks` (Common), `MarketBlocksClient` (Client).
- **Registration**: All registries (Blocks, Items, BlockEntities, Menus) are in `de.bigbull.marketblocks.util.RegistriesInit`.
  - Uses `DeferredRegister` pattern exclusively.
  - Helper methods: `registerBlock` automatically registers BlockItem.
- **Data Generation**: Located in `de.bigbull.marketblocks.data.DataGenerators` (Lang, Recipes).
  - Uses `GatherDataEvent` to attach providers.
- **Custom implementations**: 
  - Blocks/Entities/Menus are unusually located in `de.bigbull.marketblocks.util.custom.*`.
  - Core logic resides in `SmallShopBlockEntity.java` (inventory management, ownership).
- **Client Side**: `de.bigbull.marketblocks.event.ClientEvents` handles:
  - Screen registration (`RegisterMenuScreensEvent`).
  - BlockEntityRenderers (`EntityRenderersEvent.RegisterRenderers`).
  - KeyMappings.

## Key Patterns
- **Events**: Use `@EventBusSubscriber(modid = MarketBlocks.MODID, bus = Bus.MOD/GAME)` annotations.
- **Networking**: `de.bigbull.marketblocks.network.NetworkHandler` for packet handling.
- **MapCodec**: Blocks use `MapCodec` via `simpleCodec` (1.20.5+ standard).
- **Recipes**: Custom `ModRecipeProvider` extends `RecipeProvider` for datagen.

## Critical Developer Workflows
- **Build**: `./gradlew build`
- **Run Client**: `./gradlew runClient` (runs with mixins/access transformers applied).
- **Data Generation**: `./gradlew runData` (generates JSONs in `src/generated/resources`).
  - Must run after changing recipes, loot tables, or blockstate definitions.
- **Testing**: Manual testing required for UI/Inventory interactions.
  - Ensure logic works for both Client and Server (check `!level.isClientSide`).

## Dependencies
- **NeoForge**: 21.1.x
- **Java**: 21
- **Gradle**: 8.x (managed via wrapper)

## Common Integration Points
- **Items**: Add to `de.bigbull.marketblocks.util.CreativeTabInit` for creative tabs.
- **Config**: `de.bigbull.marketblocks.config.Config` (TOML based).
- **UI**: Modify `de.bigbull.marketblocks.util.custom.screen.SmallShopScreen` for GUI changes.

