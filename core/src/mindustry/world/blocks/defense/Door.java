package mindustry.world.blocks.defense;

import arc.Core;
import arc.Graphics.Cursor;
import arc.Graphics.Cursor.SystemCursor;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.geom.Rect;
import mindustry.annotations.Annotations.Loc;
import mindustry.annotations.Annotations.Remote;
import mindustry.content.Fx;
import mindustry.entities.Effects;
import mindustry.entities.Effects.Effect;
import mindustry.entities.Units;
import mindustry.entities.type.Player;
import mindustry.entities.type.TileEntity;
import mindustry.gen.Call;
import mindustry.gen.Sounds;
import mindustry.world.Tile;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import static mindustry.Vars.pathfinder;

public class Door extends Wall{
    protected final static Rect rect = new Rect();

    public final int timerToggle = timers++;
    public Effect openfx = Fx.dooropen;
    public Effect closefx = Fx.doorclose;

    protected TextureRegion openRegion;

    public Door(String name){
        super(name);
        solid = false;
        solidifes = true;
        consumesTap = true;
        entityType = DoorEntity::new;
    }

    @Remote(called = Loc.server)
    public static void onDoorToggle(Player player, Tile tile, boolean open){
        DoorEntity entity = tile.ent();
        if(entity != null){
            entity.open = open;
            Door door = (Door)tile.block();

            pathfinder.updateTile(tile);
            if(!entity.open){
                Effects.effect(door.openfx, tile.drawx(), tile.drawy());
            }else{
                Effects.effect(door.closefx, tile.drawx(), tile.drawy());
            }
            Sounds.door.at(tile);
        }
    }

    @Override
    public void load(){
        super.load();
        openRegion = Core.atlas.find(name + "-open");
    }

    @Override
    public void draw(Tile tile){
        DoorEntity entity = tile.ent();

        if(!entity.open){
            Draw.rect(region, tile.drawx(), tile.drawy());
        }else{
            Draw.rect(openRegion, tile.drawx(), tile.drawy());
        }
    }

    @Override
    public Cursor getCursor(Tile tile){
        return SystemCursor.hand;
    }

    @Override
    public boolean isSolidFor(Tile tile){
        DoorEntity entity = tile.ent();
        return !entity.open;
    }

    @Override
    public void tapped(Tile tile, Player player){
        DoorEntity entity = tile.ent();

        if((Units.anyEntities(tile) && entity.open) || !tile.entity.timer.get(timerToggle, 30f)){
            return;
        }

        Call.onDoorToggle(null, tile, !entity.open);
    }

    public class DoorEntity extends TileEntity{
        public boolean open = false;

        @Override
        public void write(DataOutput stream) throws IOException{
            super.write(stream);
            stream.writeBoolean(open);
        }

        @Override
        public void read(DataInput stream, byte revision) throws IOException{
            super.read(stream, revision);
            open = stream.readBoolean();
        }
    }

}
