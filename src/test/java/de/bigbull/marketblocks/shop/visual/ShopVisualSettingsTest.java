package de.bigbull.marketblocks.shop.visual;

import de.bigbull.marketblocks.feature.singleoffer.block.CrateLayoutMode;
import de.bigbull.marketblocks.feature.visual.npc.ShopVisualSettings;
import de.bigbull.marketblocks.feature.visual.npc.VillagerVisualProfession;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.Bootstrap;
import net.neoforged.fml.loading.LoadingModList;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ShopVisualSettingsTest {

    @BeforeAll
    public static void setup() {
        if (LoadingModList.get() == null) {
            LoadingModList.of(List.of(), List.of(), List.of(), List.of(), Map.of());
        }
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void sanitizeNameAndNbtRoundTrip() {
        ShopVisualSettings settings = new ShopVisualSettings(
                true,
                "  Handler!?@#  ",
                VillagerVisualProfession.LIBRARIAN,
                true,
                false,
                true,
                true,
                false,
                1.5f,
                3.0f,
                0.2f,
                false,
                5,
                45.0f,
                CrateLayoutMode.GESTAPELT,
                0.4f,
                0.2f,
                0.5f,
                true
        );

        assertEquals("Handler", settings.npcName());

        CompoundTag tag = settings.save();
        ShopVisualSettings loaded = ShopVisualSettings.load(tag);

        assertTrue(loaded.npcEnabled());
        assertEquals("Handler", loaded.npcName());
        assertEquals(VillagerVisualProfession.LIBRARIAN, loaded.profession());
        assertTrue(loaded.purchaseParticlesEnabled());
        assertFalse(loaded.purchaseSoundsEnabled());
        assertTrue(loaded.paymentSlotSoundsEnabled());

        assertTrue(loaded.offerItemVisible());
        assertFalse(loaded.offerItemFullbright());
        assertEquals(1.5f, loaded.offerItemScale());
        assertEquals(3.0f, loaded.offerItemSpeed());
        assertEquals(0.2f, loaded.offerItemHeightOffset());
        assertFalse(loaded.offerItemBobbing());
        assertEquals(5, loaded.offerItemCount());
        assertEquals(45.0f, loaded.offerItemRotation());
        assertEquals(CrateLayoutMode.GESTAPELT, loaded.offerItemLayoutMode());
        assertEquals(0.4f, loaded.offerItemSpacingXZ());
        assertEquals(0.2f, loaded.offerItemSpacingY());
        assertEquals(0.5f, loaded.offerItemChaosRotation());
        assertTrue(loaded.dynamicFillLevel());
    }

    @Test
    void clampsUnsafeVisualValues() {
        ShopVisualSettings settings = new ShopVisualSettings(
                false,
                "",
                VillagerVisualProfession.NONE,
                true,
                true,
                true,
                true,
                false,
                Float.NaN,
                Float.POSITIVE_INFINITY,
                -99.0f,
                true,
                -5,
                Float.NaN,
                CrateLayoutMode.LOSE,
                -1.0f,
                0.0f,
                99.0f,
                false
        );

        assertEquals(0.75f, settings.offerItemScale());
        assertEquals(2.0f, settings.offerItemSpeed());
        assertEquals(-2.0f, settings.offerItemHeightOffset());
        assertEquals(1, settings.offerItemCount());
        assertEquals(0.0f, settings.offerItemRotation());
        assertEquals(CrateLayoutMode.LOSE, settings.offerItemLayoutMode());
        assertEquals(-0.5f, settings.offerItemSpacingXZ());
        assertEquals(0.0f, settings.offerItemSpacingY());
        assertEquals(1.0f, settings.offerItemChaosRotation());
    }

    @Test
    void loadsLegacyRotationAsYaw() {
        CompoundTag tag = ShopVisualSettings.DEFAULT.save();
        tag.remove("OfferItemRotation");
        tag.putFloat("OfferItemRotation", 270.0f);

        ShopVisualSettings loaded = ShopVisualSettings.load(tag);

        assertEquals(270.0f, loaded.offerItemRotation());
        assertEquals(CrateLayoutMode.GESTAPELT, loaded.offerItemLayoutMode());
    }

    @Test
    void cycleProfessionWrapsAround() {
        assertEquals(VillagerVisualProfession.NONE, VillagerVisualProfession.WEAPONSMITH.next());
    }

    @Test
    void cycleLayoutModeWrapsAround() {
        assertEquals(CrateLayoutMode.LOSE, CrateLayoutMode.GESTAPELT.next());
    }
}