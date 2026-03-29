package de.bigbull.marketblocks.util.custom.servershop;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.item.ItemStack;

import java.util.*;

/**
 * Datenstruktur für ein Angebot innerhalb des Server-Shops.
 */
public final class ServerShopOffer {
    public static final Codec<ServerShopOffer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.optionalFieldOf("id").forGetter(offer -> Optional.of(offer.id)),
            ItemStack.CODEC.fieldOf("result").forGetter(ServerShopOffer::result),
            ItemStack.CODEC.listOf().fieldOf("payments").forGetter(ServerShopOffer::paymentsForSerialization),
            OfferLimit.CODEC.fieldOf("limits").orElse(OfferLimit.unlimited()).forGetter(ServerShopOffer::limits),
            DemandPricing.CODEC.fieldOf("pricing").orElse(DemandPricing.disabled()).forGetter(ServerShopOffer::pricing),
            ServerShopOfferRuntimeState.CODEC.optionalFieldOf("runtime_state", ServerShopOfferRuntimeState.empty()).forGetter(ServerShopOffer::runtimeState)
    ).apply(instance, (idOpt, result, payments, limits, pricing, runtimeState) -> new ServerShopOffer(
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
    private ServerShopOfferRuntimeState runtimeState;

    public ServerShopOffer(UUID id, ItemStack result, List<ItemStack> payments, OfferLimit limits, DemandPricing pricing) {
        this(id, result, payments, limits, pricing, ServerShopOfferRuntimeState.empty());
    }

    public ServerShopOffer(UUID id, ItemStack result, List<ItemStack> payments, OfferLimit limits, DemandPricing pricing, ServerShopOfferRuntimeState runtimeState) {
        this.id = id == null ? UUID.randomUUID() : id;
        this.result = sanitiseStack(result);
        List<ItemStack> sanitized = payments == null ? Collections.emptyList() : payments;
        this.payments = new ArrayList<>(sanitized.stream().map(ServerShopOffer::sanitiseStack).toList());
        while (this.payments.size() < 2) {
            this.payments.add(ItemStack.EMPTY);
        }
        this.limits = limits == null ? OfferLimit.unlimited() : limits;
        this.pricing = pricing == null ? DemandPricing.disabled() : pricing;
        this.runtimeState = runtimeState == null ? ServerShopOfferRuntimeState.empty() : runtimeState;
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

    public List<ItemStack> effectivePayments() {
        double multiplier = ServerShopRuntimeMath.computeDemandMultiplier(pricing, runtimeState.demandPurchases());
        List<ItemStack> effective = new ArrayList<>(payments.size());
        for (ItemStack payment : payments) {
            if (payment.isEmpty()) {
                effective.add(ItemStack.EMPTY);
                continue;
            }
            ItemStack adjusted = payment.copy();
            adjusted.setCount(ServerShopRuntimeMath.scalePaymentCount(payment.getCount(), multiplier));
            effective.add(adjusted);
        }
        return Collections.unmodifiableList(effective);
    }

    public double currentPriceMultiplier() {
        return ServerShopRuntimeMath.computeDemandMultiplier(pricing, runtimeState.demandPurchases());
    }

    public OfferLimit limits() {
        return limits;
    }

    public DemandPricing pricing() {
        return pricing;
    }

    public ServerShopOfferRuntimeState runtimeState() {
        return runtimeState;
    }

    public void setResult(ItemStack result) {
        this.result = sanitiseStack(result);
    }

    public void setPayment(int index, ItemStack payment) {
        if (index < 0 || index >= payments.size()) {
            throw new IndexOutOfBoundsException("Ungültiger Zahlungsindex: " + index);
        }
        payments.set(index, sanitiseStack(payment));
    }

    public void setLimits(OfferLimit limits) {
        this.limits = limits == null ? OfferLimit.unlimited() : limits;
    }

    public void setPricing(DemandPricing pricing) {
        this.pricing = pricing == null ? DemandPricing.disabled() : pricing;
    }

    public void setRuntimeState(ServerShopOfferRuntimeState runtimeState) {
        this.runtimeState = runtimeState == null ? ServerShopOfferRuntimeState.empty() : runtimeState;
    }

    public DataResult<Void> validate() {
        if (result.isEmpty()) {
            return DataResult.error(() -> "Server-Shop-Angebote benötigen ein Ergebnis-Item");
        }
        if (payments.stream().allMatch(ItemStack::isEmpty)) {
            return DataResult.error(() -> "Server-Shop-Angebote benötigen mindestens ein Zahlungsitem");
        }
        return DataResult.success(null);
    }

    public ServerShopOffer copy() {
        return new ServerShopOffer(id, result, payments, limits, pricing, runtimeState);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ServerShopOffer other)) {
            return false;
        }
        return Objects.equals(id, other.id)
                && itemStacksEqualIncludingCount(result, other.result)
                && comparePayments(other)
                && Objects.equals(limits, other.limits)
                && Objects.equals(pricing, other.pricing)
                && Objects.equals(runtimeState, other.runtimeState);
    }

    private boolean comparePayments(ServerShopOffer other) {
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
