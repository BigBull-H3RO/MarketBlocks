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
                ShopVisualSettings.DEFAULT_OFFER_ITEM_VISUALIZATION_ENABLED,
                ShopVisualSettings.DEFAULT_TRADE_STAND_OFFER_SCALE_MULTIPLIER,
                ShopVisualSettings.DEFAULT_TRADE_STAND_OFFER_ROTATION_SPEED,
                ShopVisualSettings.DEFAULT_TRADE_STAND_OFFER_HEIGHT_OFFSET,
                ShopVisualSettings.DEFAULT_MARKET_CRATE_DISPLAY_COUNT,
                ShopVisualSettings.DEFAULT_MARKET_CRATE_OFFER_HEIGHT_OFFSET,
                ShopVisualSettings.DEFAULT_MARKET_CRATE_OFFER_ROTATION_SPEED,
                ShopVisualSettings.DEFAULT_MARKET_CRATE_RANDOM_PLACEMENT,
                ShopVisualSettings.DEFAULT_MARKET_CRATE_STABLE_RANDOM
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
    }

    @Test
    void cycleProfessionWrapsAround() {
        assertEquals(VillagerVisualProfession.NONE, VillagerVisualProfession.WEAPONSMITH.next());
    }
}
