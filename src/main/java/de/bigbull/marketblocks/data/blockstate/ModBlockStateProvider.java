package de.bigbull.marketblocks.data.blockstate;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.util.RegistriesInit;
import de.bigbull.marketblocks.util.custom.block.SmallShopBlock;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
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
        ModelFile model = models().getExistingFile(modLoc("block/small_shop"));
        getVariantBuilder(RegistriesInit.SMALL_SHOP_BLOCK.get())
                .forAllStates(state -> {
                    Direction facing = state.getValue(SmallShopBlock.FACING);
                    int rotationY = switch (facing) {
                        case NORTH -> 180;
                        case EAST -> 90;
                        case WEST -> 270;
                        default -> 0;
                    };
                    return ConfiguredModel.builder()
                            .modelFile(model)
                            .rotationY(rotationY)
                            .build();
                });
    }
}
