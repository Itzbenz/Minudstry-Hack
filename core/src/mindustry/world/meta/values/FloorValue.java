package mindustry.world.meta.values;

import arc.scene.ui.Image;
import arc.scene.ui.layout.Table;
import mindustry.ui.Cicon;
import mindustry.world.blocks.Floor;
import mindustry.world.meta.StatValue;

public class FloorValue implements StatValue{
    private final Floor floor;

    public FloorValue(Floor floor){
        this.floor = floor;
    }

    @Override
    public void display(Table table){
        table.add(new Image(floor.icon(Cicon.small))).padRight(3);
        table.add(floor.localizedName).padRight(3);
    }
}
