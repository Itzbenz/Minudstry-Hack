package mindustry.entities.bullet;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.math.geom.Geometry;
import arc.math.geom.Point2;
import arc.util.ArcAnnotate.NonNull;
import arc.util.ArcAnnotate.Nullable;
import mindustry.content.Fx;
import mindustry.entities.Effects;
import mindustry.entities.effect.Fire;
import mindustry.entities.effect.Puddle;
import mindustry.entities.type.Bullet;
import mindustry.type.Liquid;
import mindustry.world.Tile;

import static mindustry.Vars.tilesize;
import static mindustry.Vars.world;

public class LiquidBulletType extends BulletType{
    public @NonNull Liquid liquid;
    public float puddleSize = 5f;

    public LiquidBulletType(@Nullable Liquid liquid){
        super(3.5f, 0);

        if(liquid != null){
            this.liquid = liquid;
            this.status = liquid.effect;
        }

        lifetime = 74f;
        statusDuration = 90f;
        despawnEffect = Fx.none;
        hitEffect = Fx.hitLiquid;
        smokeEffect = Fx.none;
        shootEffect = Fx.none;
        drag = 0.009f;
        knockback = 0.55f;
    }

    public LiquidBulletType(){
        this(null);
    }

    @Override
    public float range(){
        return speed * lifetime / 2f;
    }

    @Override
    public void update(Bullet b){
        super.update(b);

        if(liquid.canExtinguish()){
            Tile tile = world.tileWorld(b.x, b.y);
            if(tile != null && Fire.has(tile.x, tile.y)){
                Fire.extinguish(tile, 100f);
                b.remove();
                hit(b);
            }
        }
    }

    @Override
    public void draw(Bullet b){
        Draw.color(liquid.color, Color.white, b.fout() / 100f);

        Fill.circle(b.x, b.y, 0.5f + b.fout() * 2.5f);
    }

    @Override
    public void hit(Bullet b, float hitx, float hity){
        Effects.effect(hitEffect, liquid.color, hitx, hity);
        Puddle.deposit(world.tileWorld(hitx, hity), liquid, puddleSize);

        if(liquid.temperature <= 0.5f && liquid.flammability < 0.3f){
            float intensity = 400f;
            Fire.extinguish(world.tileWorld(hitx, hity), intensity);
            for(Point2 p : Geometry.d4){
                Fire.extinguish(world.tileWorld(hitx + p.x * tilesize, hity + p.y * tilesize), intensity);
            }
        }
    }
}
