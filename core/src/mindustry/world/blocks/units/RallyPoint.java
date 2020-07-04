package mindustry.world.blocks.units;

import arc.struct.EnumSet;
import mindustry.world.Block;
import mindustry.world.meta.BlockFlag;

public class RallyPoint extends Block{

    public RallyPoint(String name){
        super(name);
        update = solid = true;
        flags = EnumSet.of(BlockFlag.rally);
    }
}
