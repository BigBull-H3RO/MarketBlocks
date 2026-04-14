package de.bigbull.marketblocks.data.blockstate;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.util.block.BaseShopBlock;
import de.bigbull.marketblocks.util.RegistriesInit;
import de.bigbull.marketblocks.util.custom.block.SmallShopBlockNeu;
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
        registerShopBlock(RegistriesInit.SMALL_SHOP_BLOCK_TEST.get(), "small_shop_test", 0);
        registerSmallShopNeuBlock(RegistriesInit.SMALL_SHOP_BLOCK.get(), 0);
        simpleBlock(RegistriesInit.SMALL_SHOP_BLOCK_TOP.get(), models().getExistingFile(modLoc("block/small_shop_neu_top")));
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

    private void registerSmallShopNeuBlock(Block block, int rotationOffset) {
        ModelFile withoutShowcase = models().getExistingFile(modLoc("block/small_shop_neu_no_showcase"));
        ModelFile withShowcase = models().getExistingFile(modLoc("block/small_shop_neu"));

        getVariantBuilder(block)
                .forAllStates(state -> {
                    Direction facing = state.getValue(BaseShopBlock.FACING);
                    boolean hasShowcase = state.getValue(SmallShopBlockNeu.HAS_SHOWCASE);

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
