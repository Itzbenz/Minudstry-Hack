package mindustry.world.blocks.units;

import arc.Events;
import arc.graphics.g2d.Draw;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.struct.EnumSet;
import arc.util.ArcAnnotate.NonNull;
import arc.util.Time;
import mindustry.annotations.Annotations.Loc;
import mindustry.annotations.Annotations.Remote;
import mindustry.content.Fx;
import mindustry.content.Mechs;
import mindustry.entities.Effects;
import mindustry.entities.traits.SpawnerTrait;
import mindustry.entities.type.Player;
import mindustry.entities.type.TileEntity;
import mindustry.entities.type.Unit;
import mindustry.game.EventType.MechChangeEvent;
import mindustry.gen.Call;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.type.Mech;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.RespawnBlock;
import mindustry.world.meta.BlockFlag;
import mindustry.world.meta.BlockStat;
import mindustry.world.meta.StatUnit;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import static mindustry.Vars.mobile;
import static mindustry.Vars.tilesize;

public class MechPad extends Block{
    public @NonNull Mech mech;
    public float buildTime = 60 * 5;

    public MechPad(String name){
        super(name);
        update = true;
        solid = false;
        hasPower = true;
        layer = Layer.overlay;
        flags = EnumSet.of(BlockFlag.mechPad);
        entityType = MechFactoryEntity::new;
    }

    @Override
    public void setStats(){
        super.setStats();

        stats.add(BlockStat.productionTime, buildTime / 60f, StatUnit.seconds);
    }

    @Remote(targets = Loc.both, called = Loc.server)
    public static void onMechFactoryTap(Player player, Tile tile){
        if(player == null || tile == null || !(tile.block() instanceof MechPad) || !checkValidTap(tile, player)) return;

        MechFactoryEntity entity = tile.ent();

        if(!entity.cons.valid()) return;
        player.beginRespawning(entity);
        entity.sameMech = false;
    }

    @Remote(called = Loc.server)
    public static void onMechFactoryDone(Tile tile){
        if(!(tile.entity instanceof MechFactoryEntity)) return;

        MechFactoryEntity entity = tile.ent();

        Effects.effect(Fx.spawn, entity);

        if(entity.player == null) return;
        Mech mech = ((MechPad)tile.block()).mech;
        boolean resetSpawner = !entity.sameMech && entity.player.mech == mech;
        entity.player.mech = !entity.sameMech && entity.player.mech == mech ? Mechs.starter : mech;

        Player player = entity.player;

        entity.progress = 0;
        entity.player.onRespawn(tile);
        if(resetSpawner) entity.player.lastSpawner = null;
        entity.player = null;

        Events.fire(new MechChangeEvent(player, player.mech));
    }

    protected static boolean checkValidTap(Tile tile, Player player){
        MechFactoryEntity entity = tile.ent();
        return !player.isDead() && tile.interactable(player.getTeam()) && Math.abs(player.x - tile.drawx()) <= tile.block().size * tilesize &&
        Math.abs(player.y - tile.drawy()) <= tile.block().size * tilesize && entity.cons.valid() && entity.player == null;
    }

    @Override
    public void drawSelect(Tile tile){
        Draw.color(Pal.accent);
        for(int i = 0; i < 4; i++){
            float length = tilesize * size / 2f + 3 + Mathf.absin(Time.time(), 5f, 2f);
            Draw.rect("transfer-arrow", tile.drawx() + Geometry.d4[i].x * length, tile.drawy() + Geometry.d4[i].y * length, (i + 2) * 90);
        }
        Draw.color();
    }

    @Override
    public void tapped(Tile tile, Player player){
        MechFactoryEntity entity = tile.ent();

        if(checkValidTap(tile, player)){
            Call.onMechFactoryTap(player, tile);
        }else if(player.isLocal && mobile && !player.isDead() && entity.cons.valid() && entity.player == null){
            //deselect on double taps
            player.moveTarget = player.moveTarget == tile.entity ? null : tile.entity;
        }
    }

    @Override
    public void drawLayer(Tile tile){
        MechFactoryEntity entity = tile.ent();

        if(entity.player != null){
            RespawnBlock.drawRespawn(tile, entity.heat, entity.progress, entity.time, entity.player, (!entity.sameMech && entity.player.mech == mech ? Mechs.starter : mech));
        }
    }

    @Override
    public void update(Tile tile){
        MechFactoryEntity entity = tile.ent();

        if(entity.player != null){
            entity.player.set(tile.drawx(), tile.drawy());
            entity.heat = Mathf.lerpDelta(entity.heat, 1f, 0.1f);
            entity.progress += 1f / buildTime * entity.delta();

            entity.time += 0.5f * entity.delta();

            if(entity.progress >= 1f){
                Call.onMechFactoryDone(tile);
            }
        }else{
            entity.heat = Mathf.lerpDelta(entity.heat, 0f, 0.1f);
        }
    }

    public class MechFactoryEntity extends TileEntity implements SpawnerTrait{
        Player player;
        boolean sameMech;
        float progress;
        float time;
        float heat;

        @Override
        public boolean hasUnit(Unit unit){
            return unit == player;
        }

        @Override
        public void updateSpawning(Player unit){
            if(player == null){
                progress = 0f;
                player = unit;
                sameMech = true;

                player.beginRespawning(this);
            }
        }

        @Override
        public void write(DataOutput stream) throws IOException{
            super.write(stream);
            stream.writeFloat(progress);
            stream.writeFloat(time);
            stream.writeFloat(heat);
        }

        @Override
        public void read(DataInput stream, byte revision) throws IOException{
            super.read(stream, revision);
            progress = stream.readFloat();
            time = stream.readFloat();
            heat = stream.readFloat();
        }
    }
}
