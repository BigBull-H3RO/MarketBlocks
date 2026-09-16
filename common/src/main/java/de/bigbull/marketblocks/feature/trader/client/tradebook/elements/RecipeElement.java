package de.bigbull.marketblocks.feature.trader.client.tradebook.elements;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import de.bigbull.marketblocks.Constants;
import de.bigbull.marketblocks.core.init.RegistriesInit;
import de.bigbull.marketblocks.feature.trader.client.tradebook.ITradeBookElement;
import de.bigbull.marketblocks.feature.trader.client.tradebook.TradeBookLayoutUtils;
import de.bigbull.marketblocks.feature.trader.client.tradebook.TradeBookRenderContext;

public class RecipeElement implements ITradeBookElement {

    private static final ResourceLocation CRAFTING_GRID = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/tradebook/crafting_grid.png");
    private static final ResourceLocation CRAFTING_ARROW = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/tradebook/crafting_arrow.png");
    private static final ResourceLocation CRAFTING_RESULT = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/tradebook/crafting_result.png");

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

        if (recipeId.equals("TRADESTAND")) {
            ItemStack planks = new ItemStack(Items.OAK_PLANKS);
            ItemStack slab = new ItemStack(Items.SMOOTH_STONE_SLAB);
            ItemStack stoneBricks = new ItemStack(Items.STONE_BRICKS);
            ItemStack emerald = new ItemStack(Items.EMERALD);
            ItemStack sign = new ItemStack(Items.OAK_SIGN);
            ItemStack result = new ItemStack(RegistriesInit.TRADE_STAND_BLOCK.get());

            // Row 1: # D #
            renderSlot(graphics, planks, 5, 5, localMouseX, localMouseY, mouseX, mouseY, context);
            renderSlot(graphics, slab, 24, 5, localMouseX, localMouseY, mouseX, mouseY, context);
            renderSlot(graphics, planks, 43, 5, localMouseX, localMouseY, mouseX, mouseY, context);

            // Row 2: B E B
            renderSlot(graphics, stoneBricks, 5, 24, localMouseX, localMouseY, mouseX, mouseY, context);
            renderSlot(graphics, emerald, 24, 24, localMouseX, localMouseY, mouseX, mouseY, context);
            renderSlot(graphics, stoneBricks, 43, 24, localMouseX, localMouseY, mouseX, mouseY, context);

            // Row 3: # S #
            renderSlot(graphics, planks, 5, 43, localMouseX, localMouseY, mouseX, mouseY, context);
            renderSlot(graphics, sign, 24, 43, localMouseX, localMouseY, mouseX, mouseY, context);
            renderSlot(graphics, planks, 43, 43, localMouseX, localMouseY, mouseX, mouseY, context);

            // Result
            renderSlot(graphics, result, 96, 24, localMouseX, localMouseY, mouseX, mouseY, context);

        } else if (recipeId.equals("MARKETCRATE")) {
            ItemStack planks = new ItemStack(Items.OAK_PLANKS);
            ItemStack emerald = new ItemStack(Items.EMERALD);
            ItemStack chest = new ItemStack(Items.CHEST);
            ItemStack barrel = new ItemStack(Items.BARREL);
            ItemStack sign = new ItemStack(Items.OAK_SIGN);
            ItemStack result = new ItemStack(RegistriesInit.MARKETCRATE_BLOCK.get());

            // Row 1: # E #
            renderSlot(graphics, planks, 5, 5, localMouseX, localMouseY, mouseX, mouseY, context);
            renderSlot(graphics, emerald, 24, 5, localMouseX, localMouseY, mouseX, mouseY, context);
            renderSlot(graphics, planks, 43, 5, localMouseX, localMouseY, mouseX, mouseY, context);

            // Row 2: # C #
            renderSlot(graphics, planks, 5, 24, localMouseX, localMouseY, mouseX, mouseY, context);
            renderSlot(graphics, chest, 24, 24, localMouseX, localMouseY, mouseX, mouseY, context);
            renderSlot(graphics, planks, 43, 24, localMouseX, localMouseY, mouseX, mouseY, context);

            // Row 3: B S B
            renderSlot(graphics, barrel, 5, 43, localMouseX, localMouseY, mouseX, mouseY, context);
            renderSlot(graphics, sign, 24, 43, localMouseX, localMouseY, mouseX, mouseY, context);
            renderSlot(graphics, barrel, 43, 43, localMouseX, localMouseY, mouseX, mouseY, context);

            // Result
            renderSlot(graphics, result, 96, 24, localMouseX, localMouseY, mouseX, mouseY, context);
        }

        graphics.pose().popPose();
    }

    private void renderSlot(GuiGraphics graphics, ItemStack stack, int x, int y,
            double localMouseX, double localMouseY, int mouseX, int mouseY,
            TradeBookRenderContext context) {
        if (stack.isEmpty()) return;
        graphics.renderItem(stack, x, y);
        if (localMouseX >= x && localMouseX < x + 16 && localMouseY >= y && localMouseY < y + 16) {
            context.setNextHoveredObject("recipe_" + stack.getItem().toString());
            context.addTooltip(() -> graphics.renderTooltip(context.getFont(), stack, mouseX, mouseY));
        }
    }
}
