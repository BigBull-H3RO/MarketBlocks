package de.bigbull.marketblocks.data.advancement;

import de.bigbull.marketblocks.core.init.RegistriesInit;
import de.bigbull.marketblocks.feature.singleoffer.advancement.ShopSellTrigger;
import de.bigbull.marketblocks.feature.singleoffer.advancement.ShopNpcTrigger;
import de.bigbull.marketblocks.feature.singleoffer.advancement.ShopNpcCustomizeTrigger;
import de.bigbull.marketblocks.feature.singleoffer.advancement.ShopCoOwnerTrigger;
import de.bigbull.marketblocks.feature.singleoffer.advancement.ShopOutOfStockTrigger;
import de.bigbull.marketblocks.feature.singleoffer.advancement.ShopWholesalerTrigger;
import de.bigbull.marketblocks.feature.singleoffer.advancement.ShopRedstoneTrigger;
import de.bigbull.marketblocks.feature.singleoffer.advancement.ShopAutoIoTrigger;
import de.bigbull.marketblocks.feature.singleoffer.advancement.ShopAdminModeTrigger;
import de.bigbull.marketblocks.feature.marketplace.advancement.MarketplaceOpenTrigger;
import de.bigbull.marketblocks.feature.marketplace.advancement.MarketplaceBuyTrigger;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.ItemUsedOnLocationTrigger;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

import java.util.function.Consumer;

/**
 * Generates all advancements for the MarketBlocks mod.
 * Advancements are structured into different branches (Trade, Design, Network, Automation)
 * to guide the player through the features of the mod.
 */
public class ModAdvancementProvider implements AdvancementSubProvider {

        @Override
        public void generate(HolderLookup.Provider registries, Consumer<AdvancementHolder> saver) {


                AdvancementHolder rootAdvancement = Advancement.Builder.advancement()
                                .display(
                                                RegistriesInit.MARKETCRATE_BLOCK.get(),
                                                Component.translatable("advancements.marketblocks.root.title"),
                                                Component.translatable("advancements.marketblocks.root.description"),
                                                ResourceLocation.withDefaultNamespace(
                                                                "textures/gui/advancements/backgrounds/stone.png"),
                                                AdvancementType.TASK,
                                                false,
                                                false,
                                                false
                                )
                                .addCriterion("has_trade_stand",
                                                InventoryChangeTrigger.TriggerInstance
                                                                .hasItems(RegistriesInit.TRADE_STAND_BLOCK.get()))
                                .addCriterion("has_marketcrate",
                                                InventoryChangeTrigger.TriggerInstance
                                                                .hasItems(RegistriesInit.MARKETCRATE_BLOCK.get()))
                                .requirements(AdvancementRequirements.Strategy.OR)
                                .save(saver, "marketblocks:marketblocks/root");

                AdvancementHolder firstShopAdvancement = Advancement.Builder.advancement()
                                .parent(rootAdvancement)
                                .display(
                                                RegistriesInit.TRADE_STAND_BLOCK.get(),
                                                Component.translatable("advancements.marketblocks.first_shop.title"),
                                                Component.translatable(
                                                                "advancements.marketblocks.first_shop.description"),
                                                null,
                                                AdvancementType.TASK,
                                                true,
                                                true,
                                                false)
                                .addCriterion("placed_trade_stand",
                                                ItemUsedOnLocationTrigger.TriggerInstance
                                                                .placedBlock(RegistriesInit.TRADE_STAND_BLOCK.get()))
                                .addCriterion("placed_marketcrate",
                                                ItemUsedOnLocationTrigger.TriggerInstance
                                                                .placedBlock(RegistriesInit.MARKETCRATE_BLOCK.get()))
                                .requirements(AdvancementRequirements.Strategy.OR)
                                .save(saver, "marketblocks:marketblocks/first_shop");


                AdvancementHolder soldItemAdvancement = Advancement.Builder.advancement()
                                .parent(firstShopAdvancement)
                                .display(
                                                Items.EMERALD,
                                                Component.translatable("advancements.marketblocks.sold_item.title"),
                                                Component.translatable(
                                                                "advancements.marketblocks.sold_item.description"),
                                                null,
                                                AdvancementType.TASK,
                                                true,
                                                true,
                                                false)
                                .addCriterion("sold_item", ShopSellTrigger.TriggerInstance.soldItem())
                                .save(saver, "marketblocks:marketblocks/sold_item");

                AdvancementHolder outOfStockAdvancement = Advancement.Builder.advancement()
                                .parent(soldItemAdvancement)
                                .display(
                                                Items.BARRIER,
                                                Component.translatable("advancements.marketblocks.out_of_stock.title"),
                                                Component.translatable(
                                                                "advancements.marketblocks.out_of_stock.description"),
                                                null,
                                                AdvancementType.GOAL,
                                                true,
                                                true,
                                                false)
                                .addCriterion("went_out_of_stock",
                                                ShopOutOfStockTrigger.TriggerInstance.wentOutOfStock())
                                .save(saver, "marketblocks:marketblocks/out_of_stock");

                Advancement.Builder.advancement()
                                .parent(soldItemAdvancement)
                                .display(
                                                Items.CHEST_MINECART,
                                                Component.translatable("advancements.marketblocks.wholesaler.title"),
                                                Component.translatable(
                                                                "advancements.marketblocks.wholesaler.description"),
                                                null,
                                                AdvancementType.CHALLENGE,
                                                true,
                                                true,
                                                false)
                                .addCriterion("bought_bulk", ShopWholesalerTrigger.TriggerInstance.boughtBulk())
                                .save(saver, "marketblocks:marketblocks/wholesaler");

                Advancement.Builder.advancement()
                                .parent(soldItemAdvancement)
                                .display(
                                                Items.GOLDEN_APPLE,
                                                Component.translatable("advancements.marketblocks.tycoon.title"),
                                                Component.translatable("advancements.marketblocks.tycoon.description"),
                                                null,
                                                AdvancementType.CHALLENGE,
                                                true,
                                                true,
                                                true
                                )
                                .addCriterion("sold_many", ShopSellTrigger.TriggerInstance.soldItems(100))
                                .save(saver, "marketblocks:marketblocks/tycoon");


                AdvancementHolder showcaseAdvancement = Advancement.Builder.advancement()
                                .parent(firstShopAdvancement)
                                .display(
                                                Blocks.GLASS,
                                                Component.translatable("advancements.marketblocks.showcase.title"),
                                                Component.translatable(
                                                                "advancements.marketblocks.showcase.description"),
                                                null,
                                                AdvancementType.TASK,
                                                true,
                                                true,
                                                false)
                                .addCriterion("activated_showcase",
                                                ItemUsedOnLocationTrigger.TriggerInstance.itemUsedOnBlock(
                                                                LocationPredicate.Builder.location().setBlock(
                                                                                BlockPredicate.Builder.block().of(
                                                                                                RegistriesInit.TRADE_STAND_BLOCK
                                                                                                                .get())),
                                                                ItemPredicate.Builder.item().of(Items.GLASS)))
                                .save(saver, "marketblocks:marketblocks/showcase");

                AdvancementHolder hiringAdvancement = Advancement.Builder.advancement()
                                .parent(showcaseAdvancement)
                                .display(
                                                Items.VILLAGER_SPAWN_EGG,
                                                Component.translatable("advancements.marketblocks.hiring.title"),
                                                Component.translatable("advancements.marketblocks.hiring.description"),
                                                null,
                                                AdvancementType.TASK,
                                                true,
                                                true,
                                                false)
                                .addCriterion("hired_npc", ShopNpcTrigger.TriggerInstance.hiredNpc())
                                .save(saver, "marketblocks:marketblocks/hiring");

                Advancement.Builder.advancement()
                                .parent(hiringAdvancement)
                                .display(
                                                Items.NAME_TAG,
                                                Component.translatable("advancements.marketblocks.custom_npc.title"),
                                                Component.translatable(
                                                                "advancements.marketblocks.custom_npc.description"),
                                                null,
                                                AdvancementType.TASK,
                                                true,
                                                true,
                                                false)
                                .addCriterion("customized_npc", ShopNpcCustomizeTrigger.TriggerInstance.customizedNpc())
                                .save(saver, "marketblocks:marketblocks/custom_npc");


                AdvancementHolder jointVentureAdvancement = Advancement.Builder.advancement()
                                .parent(firstShopAdvancement)
                                .display(
                                                Items.WRITABLE_BOOK,
                                                Component.translatable("advancements.marketblocks.joint_venture.title"),
                                                Component.translatable(
                                                                "advancements.marketblocks.joint_venture.description"),
                                                null,
                                                AdvancementType.TASK,
                                                true,
                                                true,
                                                false)
                                .addCriterion("added_co_owner", ShopCoOwnerTrigger.TriggerInstance.addedCoOwner())
                                .save(saver, "marketblocks:marketblocks/joint_venture");

                AdvancementHolder wallStreetAdvancement = Advancement.Builder.advancement()
                                .parent(jointVentureAdvancement)
                                .display(
                                                Items.FILLED_MAP,
                                                Component.translatable("advancements.marketblocks.wall_street.title"),
                                                Component.translatable(
                                                                "advancements.marketblocks.wall_street.description"),
                                                null,
                                                AdvancementType.TASK,
                                                true,
                                                true,
                                                false)
                                .addCriterion("opened_marketplace",
                                                MarketplaceOpenTrigger.TriggerInstance.openedMarketplace())
                                .save(saver, "marketblocks:marketblocks/wall_street");

                Advancement.Builder.advancement()
                                .parent(wallStreetAdvancement)
                                .display(
                                                Items.BUNDLE,
                                                Component.translatable(
                                                                "advancements.marketblocks.marketplace_buy.title"),
                                                Component.translatable(
                                                                "advancements.marketblocks.marketplace_buy.description"),
                                                null,
                                                AdvancementType.TASK,
                                                true,
                                                true,
                                                false)
                                .addCriterion("bought_from_marketplace",
                                                MarketplaceBuyTrigger.TriggerInstance.boughtFromMarketplace())
                                .save(saver, "marketblocks:marketblocks/marketplace_buy");


                AdvancementHolder redstoneAdvancement = Advancement.Builder.advancement()
                                .parent(firstShopAdvancement)
                                .display(
                                                Items.REDSTONE,
                                                Component.translatable("advancements.marketblocks.redstone.title"),
                                                Component.translatable(
                                                                "advancements.marketblocks.redstone.description"),
                                                null,
                                                AdvancementType.TASK,
                                                true,
                                                true,
                                                false)
                                .addCriterion("enabled_redstone", ShopRedstoneTrigger.TriggerInstance.enabledRedstone())
                                .save(saver, "marketblocks:marketblocks/redstone");

                Advancement.Builder.advancement()
                                .parent(redstoneAdvancement)
                                .display(
                                                Items.HOPPER,
                                                Component.translatable("advancements.marketblocks.auto_io.title"),
                                                Component.translatable("advancements.marketblocks.auto_io.description"),
                                                null,
                                                AdvancementType.TASK,
                                                true,
                                                true,
                                                false)
                                .addCriterion("enabled_auto_io", ShopAutoIoTrigger.TriggerInstance.enabledAutoIo())
                                .save(saver, "marketblocks:marketblocks/auto_io");

                Advancement.Builder.advancement()
                                .parent(redstoneAdvancement)
                                .display(
                                                Items.COMMAND_BLOCK,
                                                Component.translatable("advancements.marketblocks.admin_shop.title"),
                                                Component.translatable(
                                                                "advancements.marketblocks.admin_shop.description"),
                                                null,
                                                AdvancementType.CHALLENGE,
                                                true,
                                                true,
                                                true
                                )
                                .addCriterion("enabled_admin_mode",
                                                ShopAdminModeTrigger.TriggerInstance.enabledAdminMode())
                                .save(saver, "marketblocks:marketblocks/admin_shop");
        }
}
