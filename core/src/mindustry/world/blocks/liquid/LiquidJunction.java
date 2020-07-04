package mindustry.world.blocks.liquid;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import mindustry.type.Liquid;
import mindustry.world.Tile;
import mindustry.world.blocks.LiquidBlock;
import mindustry.world.meta.BlockStat;

public class LiquidJunction extends LiquidBlock{

    public LiquidJunction(String name){
        super(name);
    }

    @Override
    public void setStats(){
        super.setStats();
        stats.remove(BlockStat.liquidCapacity);
    }

    @Override
    public void setBars(){
        super.setBars();
        bars.remove("liquid");
    }

    @Override
    public void draw(Tile tile){
        Draw.rect(name, tile.worldx(), tile.worldy());
    }

    @Override
    public TextureRegion[] generateIcons(){
        return new TextureRegion[]{Core.atlas.find(name)};
    }

    @Override
    public Tile getLiquidDestination(Tile tile, Tile source, Liquid liquid){
        int dir = source.relativeTo(tile.x, tile.y);
        dir = (dir + 4) % 4;
        Tile next = tile.getNearbyLink(dir);
        if(next == null || !next.block().acceptLiquid(next, tile, liquid, 0f) && !(next.block() instanceof LiquidJunction)){
            return tile;
        }
        return next.block().getLiquidDestination(next, tile, liquid);
    }
}
