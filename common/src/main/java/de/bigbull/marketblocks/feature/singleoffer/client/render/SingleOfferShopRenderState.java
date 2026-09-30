package de.bigbull.marketblocks.feature.singleoffer.client.render;

import de.bigbull.marketblocks.feature.singleoffer.block.ShopRenderConfig;
import de.bigbull.marketblocks.feature.singleoffer.entity.SingleOfferShopBlockEntity;
import de.bigbull.marketblocks.feature.singleoffer.settings.OfferItemSettings;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

public class SingleOfferShopRenderState extends BlockEntityRenderState {
    public SingleOfferShopBlockEntity blockEntity;
    public float partialTick;
    public long gameTime;
    public Direction facing = Direction.NORTH;
    public boolean hasOffer;
    public ShopRenderConfig renderConfig;
    public OfferItemSettings offerSettings;
    public int actualPackedLightFront;

    public ItemStack result = ItemStack.EMPTY;
    public boolean renderOfferItem;
    public boolean isOffer3D;
    public float finalOfferScale;
    public int displayCount;

    public final ItemStackRenderState offerItemRenderState = new ItemStackRenderState();

    public boolean showFrontOffer;
    public final ItemStackRenderState frontOfferRenderState = new ItemStackRenderState();

    public boolean showTradeArrow;

    public ItemStack payment1 = ItemStack.EMPTY;
    public final ItemStackRenderState payment1RenderState = new ItemStackRenderState();
    public boolean payment1Is3D;
    public float payment1Scale;

    public ItemStack payment2 = ItemStack.EMPTY;
    public final ItemStackRenderState payment2RenderState = new ItemStackRenderState();
    public boolean payment2Is3D;
    public float payment2Scale;

    // NPC render state
    public EntityRenderState npcRenderState;
    public double npcX;
    public double npcY;
    public double npcZ;

    public void clear() {
        this.blockEntity = null;
        this.result = ItemStack.EMPTY;
        this.payment1 = ItemStack.EMPTY;
        this.payment2 = ItemStack.EMPTY;
        this.offerItemRenderState.clear();
        this.frontOfferRenderState.clear();
        this.payment1RenderState.clear();
        this.payment2RenderState.clear();
        this.npcRenderState = null;
    }
}
