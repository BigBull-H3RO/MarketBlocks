package de.bigbull.marketblocks.feature.trader.client.tradebook.elements;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.core.init.RegistriesInit;
import de.bigbull.marketblocks.feature.trader.client.tradebook.ITradeBookElement;
import de.bigbull.marketblocks.feature.trader.client.tradebook.TradeBookLayoutUtils;
import de.bigbull.marketblocks.feature.trader.client.tradebook.TradeBookRenderContext;

public class RecipeElement implements ITradeBookElement {

    private static final ResourceLocation CRAFTING_GRID = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/tradebook/crafting_grid.png");
    private static final ResourceLocation CRAFTING_ARROW = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/tradebook/crafting_arrow.png");
    private static final ResourceLocation CRAFTING_RESULT = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/tradebook/crafting_result.png");

    @Override
    public boolean canHandle(String insertion) {
        return insertion != null && insertion.startsWith("RECIPE:");
    }

    @Override
    public int getExtraHeight(String insertion) {
        return (int) (70 / TradeBookLayoutUtils.TEXT_SCALE);
    }

    @Override
    public void render(GuiGraphics graphics, String insertion, int x, int y, int mouseX, int mouseY, float scale, TradeBookRenderContext context) {
        String recipeId = insertion.substring(7);
        int baseY = y + 4;

        graphics.pose().pushPose();

        graphics.pose().translate(x - 3, baseY, 0);
        float inverseScale = 1.0f / TradeBookLayoutUtils.TEXT_SCALE;
        graphics.pose().scale(inverseScale, inverseScale, 1.0f);

        graphics.blit(CRAFTING_GRID, 0, 0, 0, 0, 64, 64, 64, 64);
        graphics.blit(CRAFTING_ARROW, 68, 24, 0, 0, 16, 16, 16, 16);
        graphics.blit(CRAFTING_RESULT, 88, 16, 0, 0, 32, 32, 32, 32);

        double localMouseX = ((mouseX / scale) - (x - 3)) / inverseScale;
        double localMouseY = ((mouseY / scale) - baseY) / inverseScale;

        java.util.function.BiConsumer<ItemStack, int[]> checkTooltip = (stack, pos) -> {
            if (!stack.isEmpty() && localMouseX >= pos[0] && localMouseX < pos[0] + 16 && localMouseY >= pos[1] && localMouseY < pos[1] + 16) {
                context.setNextHoveredObject("recipe_" + stack.getItem().toString());
                context.addTooltip(() -> graphics.renderTooltip(context.getFont(), stack, mouseX, mouseY));
            }
        };

        if (recipeId.equals("TRADESTAND")) {
            ItemStack slab = new ItemStack(Items.OAK_SLAB);
            ItemStack stick = new ItemStack(Items.STICK);
            ItemStack emerald = new ItemStack(Items.EMERALD);
            ItemStack log = new ItemStack(Items.OAK_LOG);
            ItemStack result = new ItemStack(RegistriesInit.TRADE_STAND_BLOCK.get());

            graphics.renderItem(slab, 5, 5);
            graphics.renderItem(slab, 24, 5);
            graphics.renderItem(slab, 43, 5);
            graphics.renderItem(stick, 5, 24);
            graphics.renderItem(emerald, 24, 24);
            graphics.renderItem(stick, 43, 24);
            graphics.renderItem(log, 5, 43);
            graphics.renderItem(log, 24, 43);
            graphics.renderItem(log, 43, 43);

            graphics.renderItem(result, 96, 24);

            checkTooltip.accept(slab, new int[] { 5, 5 });
            checkTooltip.accept(slab, new int[] { 24, 5 });
            checkTooltip.accept(slab, new int[] { 43, 5 });
            checkTooltip.accept(stick, new int[] { 5, 24 });
            checkTooltip.accept(emerald, new int[] { 24, 24 });
            checkTooltip.accept(stick, new int[] { 43, 24 });
            checkTooltip.accept(log, new int[] { 5, 43 });
            checkTooltip.accept(log, new int[] { 24, 43 });
            checkTooltip.accept(log, new int[] { 43, 43 });
            checkTooltip.accept(result, new int[] { 96, 24 });

        } else if (recipeId.equals("MARKETCRATE")) {
            ItemStack slab = new ItemStack(Items.OAK_SLAB);
            ItemStack stick = new ItemStack(Items.STICK);
            ItemStack emerald = new ItemStack(Items.EMERALD);
            ItemStack chest = new ItemStack(Items.CHEST);
            ItemStack result = new ItemStack(RegistriesInit.MARKETCRATE_BLOCK.get());

            graphics.renderItem(slab, 5, 5);
            graphics.renderItem(slab, 24, 5);
            graphics.renderItem(slab, 43, 5);
            graphics.renderItem(stick, 5, 24);
            graphics.renderItem(emerald, 24, 24);
            graphics.renderItem(stick, 43, 24);
            graphics.renderItem(chest, 5, 43);
            graphics.renderItem(chest, 24, 43);
            graphics.renderItem(chest, 43, 43);

            graphics.renderItem(result, 96, 24);

            checkTooltip.accept(slab, new int[] { 5, 5 });
            checkTooltip.accept(slab, new int[] { 24, 5 });
            checkTooltip.accept(slab, new int[] { 43, 5 });
            checkTooltip.accept(stick, new int[] { 5, 24 });
            checkTooltip.accept(emerald, new int[] { 24, 24 });
            checkTooltip.accept(stick, new int[] { 43, 24 });
            checkTooltip.accept(chest, new int[] { 5, 43 });
            checkTooltip.accept(chest, new int[] { 24, 43 });
            checkTooltip.accept(chest, new int[] { 43, 43 });
            checkTooltip.accept(result, new int[] { 96, 24 });
        }

        graphics.pose().popPose();
    }
}
