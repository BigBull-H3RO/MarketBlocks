package de.bigbull.marketblocks.data.texture;

import de.bigbull.marketblocks.MarketBlocks;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, MarketBlocks.MODID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {

    }
}
