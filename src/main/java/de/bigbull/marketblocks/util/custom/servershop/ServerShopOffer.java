package de.bigbull.marketblocks.util.custom.servershop;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.item.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Datenstruktur für ein Angebot innerhalb des Server-Shops.
 */
public final class ServerShopOffer {
    public static final Codec<ServerShopOffer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.optionalFieldOf("id").forGetter(offer -> Optional.of(offer.id)),
            ItemStack.CODEC.fieldOf("result").forGetter(ServerShopOffer::result),
            ItemStack.CODEC.listOf().fieldOf("payments").forGetter(ServerShopOffer::payments),
            OfferLimit.CODEC.fieldOf("limits").orElse(OfferLimit.unlimited()).forGetter(ServerShopOffer::limits),
            DemandPricing.CODEC.fieldOf("pricing").orElse(DemandPricing.disabled()).forGetter(ServerShopOffer::pricing)
    ).apply(instance, (idOpt, result, payments, limits, pricing) -> new ServerShopOffer(
            idOpt.orElse(null),
            result,
            payments,
            limits,
            pricing
    )));

    private final UUID id;
    private ItemStack result;
    private final List<ItemStack> payments;
    private OfferLimit limits;
    private DemandPricing pricing;

    public ServerShopOffer(UUID id, ItemStack result, List<ItemStack> payments, OfferLimit limits, DemandPricing pricing) {
        this.id = id == null ? UUID.randomUUID() : id;
        this.result = sanitiseStack(result);
        List<ItemStack> sanitized = payments == null ? Collections.emptyList() : payments;
        this.payments = new ArrayList<>(sanitized.stream().map(ServerShopOffer::sanitiseStack).collect(Collectors.toList()));
        while (this.payments.size() < 2) {
            this.payments.add(ItemStack.EMPTY);
        }
        this.limits = limits == null ? OfferLimit.unlimited() : limits;
        this.pricing = pricing == null ? DemandPricing.disabled() : pricing;
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
        return payments.stream().map(ItemStack::copy).collect(Collectors.toUnmodifiableList());
    }

    public OfferLimit limits() {
        return limits;
    }

    public DemandPricing pricing() {
        return pricing;
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

    public DataResult<Void> validate() {
        if (result.isEmpty()) {
            return DataResult.error(() -> "Server-Shop-Angebote benötigen ein Ergebnis-Item");
        }
        return DataResult.success(null);
    }

    public ServerShopOffer copy() {
        return new ServerShopOffer(id, result, payments, limits, pricing);
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
                && ItemStack.isSameItemSameComponents(result, other.result)
                && comparePayments(other)
                && Objects.equals(limits, other.limits)
                && Objects.equals(pricing, other.pricing);
    }

    private boolean comparePayments(ServerShopOffer other) {
        if (payments.size() != other.payments.size()) {
            return false;
        }
        for (int i = 0; i < payments.size(); i++) {
            if (!ItemStack.isSameItemSameComponents(payments.get(i), other.payments.get(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int resultHash = Objects.hash(id, limits, pricing);
        resultHash = 31 * resultHash + ItemStack.hashItemAndComponents(result);
        for (ItemStack payment : payments) {
            resultHash = 31 * resultHash + ItemStack.hashItemAndComponents(payment);
        }
        return resultHash;
    }
}