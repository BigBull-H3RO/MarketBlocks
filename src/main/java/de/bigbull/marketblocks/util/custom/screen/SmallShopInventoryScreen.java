package de.bigbull.marketblocks.util.custom.screen;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.data.lang.ModLang;
import de.bigbull.marketblocks.util.custom.menu.ShopTab;
import de.bigbull.marketblocks.util.custom.menu.SmallShopInventoryMenu;
import de.bigbull.marketblocks.util.custom.screen.gui.GuiConstants;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

/**
 * The screen for the "Inventory" tab of the Small Shop.
 * It displays the input and output buffers and is only fully interactive for the shop owner.
 */
public class SmallShopInventoryScreen extends AbstractSmallShopScreen<SmallShopInventoryMenu> {
    private static final ResourceLocation BACKGROUND = MarketBlocks.id("textures/gui/small_shop_inventory.png");
    private static final ResourceLocation INPUT_OUTPUT_ICON = MarketBlocks.id("textures/gui/icon/input_output.png");
    private static final int INFO_TEXT_COLOR = 0x808080;

    public SmallShopInventoryScreen(@NotNull SmallShopInventoryMenu menu, @NotNull Inventory inv, @NotNull Component title) {
        super(menu, inv, title);
        this.imageWidth = GuiConstants.IMAGE_WIDTH;
        this.imageHeight = GuiConstants.IMAGE_HEIGHT;
        this.inventoryLabelY = GuiConstants.PLAYER_INV_LABEL_Y;
    }

    @Override
    protected void init() {
        super.init();
        if (this.isOwner()) {
            createTabButtons(this.leftPos + this.imageWidth, this.topPos, ShopTab.INVENTORY);
        }
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(BACKGROUND, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
        graphics.blit(INPUT_OUTPUT_ICON, this.leftPos + 77, this.topPos + 33, 0, 0, 22, 22, 22, 22);
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {
        // Title
        graphics.drawString(this.font, Component.translatable(ModLang.GUI_INVENTORY_TITLE), 8, 6, 4210752, false);

        // Owner Info (shown for non-owners)
        renderOwnerInfo(graphics, this.menu.getBlockEntity());

        // "Owner only" message for non-owners
        if (!isOwner()) {
            Component infoText = Component.translatable(ModLang.GUI_INVENTORY_OWNER_ONLY);
            int infoWidth = this.font.width(infoText);
            graphics.drawString(this.font, infoText, (this.imageWidth - infoWidth) / 2, 42, INFO_TEXT_COLOR, false);
        }

        // Player Inventory Title
        graphics.drawString(this.font, this.playerInventoryTitle, 8, this.inventoryLabelY, 4210752, false);
    }
}