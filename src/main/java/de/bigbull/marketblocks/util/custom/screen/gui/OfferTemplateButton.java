package de.bigbull.marketblocks.util.custom.screen.gui;

import de.bigbull.marketblocks.MarketBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * Button zum Anzeigen eines bestehenden Angebots.
 * Nutzt Supplier, um stets aktuelle ItemStacks und Pfeilstatus zu rendern.
 */
public class OfferTemplateButton extends AbstractWidget {
    private static final ResourceLocation TRADE_ARROW =
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/icon/trade_arrow.png");
    private static final ResourceLocation TRADE_ARROW_DISABLED =
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/icon/trade_arrow_disabled.png");

    private final Supplier<ItemStack> payment1Supplier;
    private final Supplier<ItemStack> payment2Supplier;
    private final Supplier<ItemStack> resultSupplier;
    private final BooleanSupplier arrowActiveSupplier;

    public OfferTemplateButton(int x, int y,
                               Supplier<ItemStack> payment1Supplier,
                               Supplier<ItemStack> payment2Supplier,
                               Supplier<ItemStack> resultSupplier,
                               BooleanSupplier arrowActiveSupplier) {
        super(x, y, 100, 20, Component.empty());
        this.payment1Supplier = payment1Supplier;
        this.payment2Supplier = payment2Supplier;
        this.resultSupplier = resultSupplier;
        this.arrowActiveSupplier = arrowActiveSupplier;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        ItemStack payment1 = payment1Supplier.get();
        ItemStack payment2 = payment2Supplier.get();
        ItemStack result = resultSupplier.get();
        boolean arrowActive = arrowActiveSupplier.getAsBoolean();

        // Hintergrund
        graphics.fill(getX() - 2, getY() - 2, getX() + 100, getY() + 20, 0x80000000);

        // Zahlungsitems
        if (!payment1.isEmpty()) {
            graphics.renderItem(payment1, getX(), getY());
            graphics.renderItemDecorations(Minecraft.getInstance().font, payment1, getX(), getY());
        }

        if (!payment2.isEmpty()) {
            graphics.renderItem(payment2, getX() + 18, getY());
            graphics.renderItemDecorations(Minecraft.getInstance().font, payment2, getX() + 18, getY());
        }

        // Pfeil
        ResourceLocation arrowTexture = arrowActive ? TRADE_ARROW : TRADE_ARROW_DISABLED;
        graphics.blit(arrowTexture, getX() + 44, getY() + 1, 0, 0, 12, 8, 24, 16);

        // Ergebnis
        if (!result.isEmpty()) {
            graphics.renderItem(result, getX() + 64, getY());
            graphics.renderItemDecorations(Minecraft.getInstance().font, result, getX() + 64, getY());
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }
}