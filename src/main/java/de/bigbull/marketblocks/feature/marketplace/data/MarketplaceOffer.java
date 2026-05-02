package de.bigbull.marketblocks.feature.marketplace.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.item.ItemStack;

import java.util.*;

/**
 * Represents a single trade offer within the marketplace.
 */
public final class MarketplaceOffer {
    public static final Codec<MarketplaceOffer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.optionalFieldOf("id").forGetter(offer -> Optional.of(offer.id)),
            ItemStack.CODEC.fieldOf("result").forGetter(MarketplaceOffer::result),
            ItemStack.CODEC.listOf().fieldOf("payments").forGetter(MarketplaceOffer::paymentsForSerialization),
            OfferLimit.CODEC.fieldOf("limits").orElse(OfferLimit.unlimited()).forGetter(MarketplaceOffer::limits),
            DemandPricing.CODEC.fieldOf("pricing").orElse(DemandPricing.disabled()).forGetter(MarketplaceOffer::pricing),
            MarketplaceOfferRuntimeState.CODEC.optionalFieldOf("runtime_state", MarketplaceOfferRuntimeState.empty()).forGetter(MarketplaceOffer::runtimeState)
    ).apply(instance, (idOpt, result, payments, limits, pricing, runtimeState) -> new MarketplaceOffer(
            idOpt.orElse(null),
            result,
            payments,
            limits,
            pricing,
            runtimeState
    )));

    private final UUID id;
    private ItemStack result;
    private final List<ItemStack> payments;
    private OfferLimit limits;
    private DemandPricing pricing;
    private MarketplaceOfferRuntimeState runtimeState;

    public MarketplaceOffer(UUID id, ItemStack result, List<ItemStack> payments, OfferLimit limits, DemandPricing pricing) {
        this(id, result, payments, limits, pricing, MarketplaceOfferRuntimeState.empty());
    }

    public MarketplaceOffer(UUID id, ItemStack result, List<ItemStack> payments, OfferLimit limits, DemandPricing pricing, MarketplaceOfferRuntimeState runtimeState) {
        this.id = id == null ? UUID.randomUUID() : id;
        this.result = sanitiseStack(result);
        List<ItemStack> sanitized = payments == null ? Collections.emptyList() : payments;
        this.payments = new ArrayList<>(sanitized.stream().map(MarketplaceOffer::sanitiseStack).toList());
        while (this.payments.size() < 2) {
            this.payments.add(ItemStack.EMPTY);
        }
        this.limits = limits == null ? OfferLimit.unlimited() : limits;
        this.pricing = pricing == null ? DemandPricing.disabled() : pricing;
        this.runtimeState = runtimeState == null ? MarketplaceOfferRuntimeState.empty() : runtimeState;
    }

    private static ItemStack sanitiseStack(ItemStack stack) {
        return stack == null ? ItemStack.EMPTY : stack.copy();
    }

    public UUID id() {
        return id;
    }

    public ItemStack result() {
        return result.copy();
    }

    public List<ItemStack> payments() {
        return payments.stream().map(ItemStack::copy).toList();
    }

    private List<ItemStack> paymentsForSerialization() {
        // ItemStack.CODEC rejects empty stacks (air/count 0), so persist only real payment entries.
        return payments.stream()
                .filter(stack -> !stack.isEmpty())
                .map(ItemStack::copy)
                .toList();
    }

    /**
     * Returns the payment items with the current demand multiplier applied to each item's count.
     * Empty (air) slots are preserved as-is.
     */
    public List<ItemStack> effectivePayments() {
        double multiplier = MarketplaceRuntimeMath.computeDemandMultiplier(pricing, runtimeState.demandPurchases());
        List<ItemStack> effective = new ArrayList<>(payments.size());
        for (ItemStack payment : payments) {
            if (payment.isEmpty()) {
                effective.add(ItemStack.EMPTY);
                continue;
            }
            ItemStack adjusted = payment.copy();
            adjusted.setCount(MarketplaceRuntimeMath.scalePaymentCount(payment.getCount(), multiplier));
            effective.add(adjusted);
        }
        return Collections.unmodifiableList(effective);
    }

    /** Returns the current demand-based price multiplier (1.0 when demand pricing is disabled). */
    public double currentPriceMultiplier() {
        return MarketplaceRuntimeMath.computeDemandMultiplier(pricing, runtimeState.demandPurchases());
    }

    public OfferLimit limits() {
        return limits;
    }

    public DemandPricing pricing() {
        return pricing;
    }

    public MarketplaceOfferRuntimeState runtimeState() {
        return runtimeState;
    }

    public void setResult(ItemStack result) {
        this.result = sanitiseStack(result);
    }

    public void setPayment(int index, ItemStack payment) {
        if (index < 0 || index >= payments.size()) {
            throw new IndexOutOfBoundsException("Invalid payment index: " + index);
        }
        payments.set(index, sanitiseStack(payment));
    }

    public void setLimits(OfferLimit limits) {
        this.limits = limits == null ? OfferLimit.unlimited() : limits;
    }

    public void setPricing(DemandPricing pricing) {
        this.pricing = pricing == null ? DemandPricing.disabled() : pricing;
    }

    public void setRuntimeState(MarketplaceOfferRuntimeState runtimeState) {
        this.runtimeState = runtimeState == null ? MarketplaceOfferRuntimeState.empty() : runtimeState;
    }

    /**
     * Validates that this offer has a non-empty result item and at least one non-empty payment item.
     *
     * @return success if valid, or an error {@link com.mojang.serialization.DataResult} otherwise
     */
    public DataResult<Void> validate() {
        if (result.isEmpty()) {
            return DataResult.error(() -> "A marketplace offer must have a result item");
        }
        if (payments.stream().allMatch(ItemStack::isEmpty)) {
            return DataResult.error(() -> "A marketplace offer must have at least one payment item");
        }
        return DataResult.success(null);
    }

    public MarketplaceOffer copy() {
        return new MarketplaceOffer(id, result, payments, limits, pricing, runtimeState);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof MarketplaceOffer other)) {
            return false;
        }
        return Objects.equals(id, other.id)
                && itemStacksEqualIncludingCount(result, other.result)
                && comparePayments(other)
                && Objects.equals(limits, other.limits)
                && Objects.equals(pricing, other.pricing)
                && Objects.equals(runtimeState, other.runtimeState);
    }

    private boolean comparePayments(MarketplaceOffer other) {
        if (payments.size() != other.payments.size()) {
            return false;
        }
        for (int i = 0; i < payments.size(); i++) {
            if (!itemStacksEqualIncludingCount(payments.get(i), other.payments.get(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int resultHash = Objects.hash(id, limits, pricing, runtimeState);
        resultHash = 31 * resultHash + stackHashIncludingCount(result);
        for (ItemStack payment : payments) {
            resultHash = 31 * resultHash + stackHashIncludingCount(payment);
        }
        return resultHash;
    }

    private static boolean itemStacksEqualIncludingCount(ItemStack first, ItemStack second) {
        return ItemStack.isSameItemSameComponents(first, second)
                && first.getCount() == second.getCount();
    }

    private static int stackHashIncludingCount(ItemStack stack) {
        return 31 * ItemStack.hashItemAndComponents(stack) + stack.getCount();
    }
}
