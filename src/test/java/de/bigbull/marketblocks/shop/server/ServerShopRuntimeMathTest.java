package de.bigbull.marketblocks.shop.server;

import de.bigbull.marketblocks.shop.marketplace.DemandPricing;
import de.bigbull.marketblocks.shop.marketplace.MarketplaceOfferRuntimeState;
import de.bigbull.marketblocks.shop.marketplace.MarketplaceRuntimeMath;
import de.bigbull.marketblocks.shop.marketplace.OfferLimit;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.neoforged.fml.loading.LoadingModList;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for pure server-shop runtime math.
 */
public class ServerShopRuntimeMathTest {

    @BeforeAll
    public static void setup() {
        if (LoadingModList.get() == null) {
            LoadingModList.of(List.of(), List.of(), List.of(), List.of(), Map.of());
        }
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void demandMultiplierIsClampedBetweenMinAndMax() {
        DemandPricing pricing = new DemandPricing(true, 1.0d, 0.5d, 0.75d, 2.0d);

        assertEquals(1.0d, MarketplaceRuntimeMath.computeDemandMultiplier(pricing, 0), 1.0E-9d);
        assertEquals(2.0d, MarketplaceRuntimeMath.computeDemandMultiplier(pricing, 10), 1.0E-9d);
        assertEquals(1, MarketplaceRuntimeMath.scalePaymentCount(1, 0.10d));
        assertEquals(5, MarketplaceRuntimeMath.scalePaymentCount(3, 1.5d));
    }

    @Test
    void remainingPurchasesRespectStockAndDailyLimitPerPlayer() {
        UUID playerId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        OfferLimit limit = new OfferLimit(false, 5, 8, null);
        MarketplaceOfferRuntimeState state = new MarketplaceOfferRuntimeState(3, 2, 4L, 0L, 0, 0L, Map.of(playerId, 2));

        assertEquals(3, MarketplaceRuntimeMath.computeRemainingPurchases(limit, state, playerId, false));
        assertEquals(3, MarketplaceRuntimeMath.computeRemainingDailyPurchases(limit, state, playerId, false));
    }

    @Test
    void remainingPurchasesRespectGlobalDailyLimit() {
        UUID playerId = UUID.fromString("00000000-0000-0000-0000-000000000002");
        OfferLimit limit = new OfferLimit(false, 5, 8, null);
        MarketplaceOfferRuntimeState state = new MarketplaceOfferRuntimeState(8, 4, 4L, 0L, 0, 0L, Map.of(playerId, 1));

        assertEquals(1, MarketplaceRuntimeMath.computeRemainingPurchases(limit, state, playerId, true));
        assertEquals(1, MarketplaceRuntimeMath.computeRemainingDailyPurchases(limit, state, playerId, true));
    }

    @Test
    void restockReplenishesOneUnitPerInterval() {
        MarketplaceRuntimeMath.RestockResult result = MarketplaceRuntimeMath.applyRestock(1, 5, 100L, 160L, 1);

        assertTrue(result.changed());
        assertEquals(4, result.stockRemaining());
        assertEquals(160L, result.lastRestockGameTime());
    }

    @Test
    void restockCapsAtConfiguredMaximumStock() {
        MarketplaceRuntimeMath.RestockResult result = MarketplaceRuntimeMath.applyRestock(4, 5, 100L, 300L, 1);

        assertEquals(5, result.stockRemaining());
        assertTrue(result.lastRestockGameTime() >= 120L);
    }

    @Test
    void secondsUntilNextRestockIsReported() {
        OfferLimit limit = new OfferLimit(false, null, 5, 10);
        MarketplaceOfferRuntimeState state = new MarketplaceOfferRuntimeState(2, 0, 0L, 100L, 0);

        assertEquals(7, MarketplaceRuntimeMath.computeSecondsUntilNextRestock(limit, state, 160L).orElseThrow());
    }

    @Test
    void timerFormattingUsesMinutesAndSeconds() {
        assertEquals("02:05", MarketplaceRuntimeMath.formatSecondsAsTimer(125));
    }

    @Test
    void demandPurchasesDecayAcrossElapsedDays() {
        assertEquals(4, MarketplaceRuntimeMath.computeDemandPurchasesAfterDailyDecay(10, 5L, 8L, 2));
        assertEquals(0, MarketplaceRuntimeMath.computeDemandPurchasesAfterDailyDecay(2, 3L, 10L, 2));
    }

    @Test
    void demandPurchasesDoNotDecayWhenLastDayUnknownOrDecayDisabled() {
        assertEquals(10, MarketplaceRuntimeMath.computeDemandPurchasesAfterDailyDecay(10, 0L, 8L, 2));
        assertEquals(10, MarketplaceRuntimeMath.computeDemandPurchasesAfterDailyDecay(10, 5L, 8L, 0));
    }

    public static void main(String[] args) {
        ServerShopRuntimeMathTest test = new ServerShopRuntimeMathTest();
        test.demandMultiplierIsClampedBetweenMinAndMax();
        test.remainingPurchasesRespectStockAndDailyLimitPerPlayer();
        test.remainingPurchasesRespectGlobalDailyLimit();
        test.restockReplenishesOneUnitPerInterval();
        test.restockCapsAtConfiguredMaximumStock();
        test.secondsUntilNextRestockIsReported();
        test.timerFormattingUsesMinutesAndSeconds();
        test.demandPurchasesDecayAcrossElapsedDays();
        test.demandPurchasesDoNotDecayWhenLastDayUnknownOrDecayDisabled();
        System.out.println("ServerShopRuntimeMathTest: OK");
    }
}
