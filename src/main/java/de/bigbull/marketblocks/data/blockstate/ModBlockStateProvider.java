package de.bigbull.marketblocks.data.blockstate;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.util.block.BaseShopBlock;
import de.bigbull.marketblocks.util.RegistriesInit;
import de.bigbull.marketblocks.util.custom.block.TradeStandBlock;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, MarketBlocks.MODID, existingFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        registerShopBlock(RegistriesInit.SHOP_BLOCK_TEST.get(), "shop_block_test", 0);
        registerTradeStandBlock(RegistriesInit.TRADE_STAND_BLOCK.get(), 0);

        ModelFile topModel = models().getExistingFile(modLoc("block/trade_stand_block_top"));
        simpleBlock(RegistriesInit.TRADE_STAND_BLOCK_TOP.get(), topModel);
    }

    private void registerShopBlock(Block block, String modelName, int rotationOffset) {
        ModelFile model = models().getExistingFile(modLoc("block/" + modelName));
        getVariantBuilder(block)
                .forAllStates(state -> {
                    Direction facing = state.getValue(BaseShopBlock.FACING);
                    int baseRotation = switch (facing) {
                        case NORTH -> 180;
                        case EAST -> 90;
                        case WEST -> 270;
                        default -> 0;
                    };
                    int rotationY = Math.floorMod(baseRotation + rotationOffset, 360);
                    return ConfiguredModel.builder()
                            .modelFile(model)
                            .rotationY(rotationY)
                            .build();
                });
    }

    private void registerTradeStandBlock(Block block, int rotationOffset) {
        ModelFile withoutShowcase = models().getExistingFile(modLoc("block/trade_stand_block_base"));
        ModelFile withShowcase = models().getExistingFile(modLoc("block/trade_stand_block_showcase"));

        getVariantBuilder(block)
                .forAllStates(state -> {
                    Direction facing = state.getValue(BaseShopBlock.FACING);
                    boolean hasShowcase = state.getValue(TradeStandBlock.HAS_SHOWCASE);

                    int baseRotation = switch (facing) {
                        case NORTH -> 0;
                        case EAST -> 90;
                        case SOUTH -> 180;
                        case WEST -> 270;
                        default -> 0;
                    };
                    int rotationY = Math.floorMod(baseRotation + rotationOffset, 360);

                    return ConfiguredModel.builder()
                            .modelFile(hasShowcase ? withShowcase : withoutShowcase)
                            .rotationY(rotationY)
                            .build();
                });
    }
}
