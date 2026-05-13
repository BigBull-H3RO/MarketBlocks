package de.bigbull.marketblocks.shop.visual;

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
                false,
                1.2f,
                3.5f,
                0.2f,
                8,
                0.1f,
                1.5f,
                false,
                false
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

        assertFalse(loaded.offerItemVisualizationEnabled());
        assertEquals(1.2f, loaded.tradeStandOfferScaleMultiplier());
        assertEquals(3.5f, loaded.tradeStandOfferRotationSpeed());
        assertEquals(0.2f, loaded.tradeStandOfferHeightOffset());
        assertEquals(8, loaded.marketCrateDisplayCount());
        assertEquals(0.1f, loaded.marketCrateOfferHeightOffset());
        assertEquals(1.5f, loaded.marketCrateOfferRotationSpeed());
        assertFalse(loaded.marketCrateRandomPlacement());
        assertFalse(loaded.marketCrateStableRandom());
    }

    @Test
    void clampAndSanitizeInvalidFloats() {
        ShopVisualSettings settings = new ShopVisualSettings(
                true,
                "Test",
                VillagerVisualProfession.NONE,
                true, true, true, true,
                Float.NaN,
                Float.POSITIVE_INFINITY,
                -10.0f,
                100,
                10.0f,
                Float.NEGATIVE_INFINITY,
                true, true
        );

        assertEquals(ShopVisualSettings.DEFAULT_TRADE_STAND_OFFER_SCALE_MULTIPLIER, settings.tradeStandOfferScaleMultiplier());
        assertEquals(ShopVisualSettings.DEFAULT_TRADE_STAND_OFFER_ROTATION_SPEED, settings.tradeStandOfferRotationSpeed());
        assertEquals(ShopVisualSettings.MIN_OFFER_HEIGHT_OFFSET, settings.tradeStandOfferHeightOffset());
        assertEquals(ShopVisualSettings.MAX_MARKET_CRATE_DISPLAY_COUNT, settings.marketCrateDisplayCount());
        assertEquals(ShopVisualSettings.MAX_OFFER_HEIGHT_OFFSET, settings.marketCrateOfferHeightOffset());
        assertEquals(ShopVisualSettings.DEFAULT_TRADE_STAND_OFFER_ROTATION_SPEED, settings.marketCrateOfferRotationSpeed());
    }

    @Test
    void cycleProfessionWrapsAround() {
        assertEquals(VillagerVisualProfession.NONE, VillagerVisualProfession.WEAPONSMITH.next());
    }
}
