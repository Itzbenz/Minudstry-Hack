package mindustry.maps.filters;

import mindustry.content.Blocks;
import mindustry.world.Block;

import static mindustry.maps.filters.FilterOption.BlockOption;
import static mindustry.maps.filters.FilterOption.wallsOnly;

public class ClearFilter extends GenerateFilter{
    protected Block block = Blocks.air;

    {
        options(
        new BlockOption("block", () -> block, b -> block = b, wallsOnly)
        );
    }

    @Override
    public void apply(){

        if(in.block == block){
            in.block = Blocks.air;
        }
    }
}
