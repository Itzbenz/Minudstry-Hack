package mindustry.world.consumers;

import arc.scene.ui.layout.Table;
import arc.struct.Bits;
import arc.util.ArcAnnotate.NonNull;
import mindustry.entities.type.TileEntity;
import mindustry.type.ItemStack;
import mindustry.ui.Cicon;
import mindustry.ui.ItemImage;
import mindustry.ui.ReqImage;
import mindustry.world.Tile;
import mindustry.world.meta.BlockStat;
import mindustry.world.meta.BlockStats;
import mindustry.world.meta.values.ItemListValue;

public class ConsumeItems extends Consume{
    public final @NonNull ItemStack[] items;

    public ConsumeItems(ItemStack[] items){
        this.items = items;
    }

    /** Mods.*/
    protected ConsumeItems(){
        this(new ItemStack[]{});
    }

    @Override
    public void applyItemFilter(Bits filter){
        for(ItemStack stack : items){
            filter.set(stack.item.id);
        }
    }

    @Override
    public ConsumeType type(){
        return ConsumeType.item;
    }

    @Override
    public void build(Tile tile, Table table){
        for(ItemStack stack : items){
            table.add(new ReqImage(new ItemImage(stack.item.icon(Cicon.medium), stack.amount), () -> tile.entity != null && tile.entity.items != null && tile.entity.items.has(stack.item, stack.amount))).size(8 * 4).padRight(5);
        }
    }

    @Override
    public String getIcon(){
        return "icon-item";
    }

    @Override
    public void update(TileEntity entity){

    }

    @Override
    public void trigger(TileEntity entity){
        for(ItemStack stack : items){
            entity.items.remove(stack);
        }
    }

    @Override
    public boolean valid(TileEntity entity){
        return entity.items != null && entity.items.has(items);
    }

    @Override
    public void display(BlockStats stats){
        stats.add(booster ? BlockStat.booster : BlockStat.input, new ItemListValue(items));
    }
}
