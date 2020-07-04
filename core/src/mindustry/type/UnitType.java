package mindustry.type;

import arc.Core;
import arc.audio.Sound;
import arc.func.Prov;
import arc.graphics.g2d.TextureRegion;
import arc.scene.ui.layout.Table;
import arc.struct.ObjectSet;
import arc.util.ArcAnnotate.NonNull;
import mindustry.content.Items;
import mindustry.ctype.ContentType;
import mindustry.ctype.UnlockableContent;
import mindustry.entities.type.BaseUnit;
import mindustry.game.Team;
import mindustry.gen.Sounds;
import mindustry.ui.ContentDisplay;

public class UnitType extends UnlockableContent{
    public @NonNull TypeID typeID;
    public @NonNull Prov<? extends BaseUnit> constructor;

    public float health = 60;
    public float hitsize = 7f;
    public float hitsizeTile = 4f;
    public float speed = 0.4f;
    public float range = 0, attackLength = 150f;
    public float rotatespeed = 0.2f;
    public float baseRotateSpeed = 0.1f;
    public float shootCone = 15f;
    public float mass = 1f;
    public boolean flying;
    public boolean targetAir = true;
    public boolean rotateWeapon = false;
    public float drag = 0.1f;
    public float maxVelocity = 5f;
    public float retreatPercent = 0.6f;
    public int itemCapacity = 30;
    public ObjectSet<Item> toMine = ObjectSet.with(Items.lead, Items.copper);
    public float buildPower = 0.3f, minePower = 0.7f;
    public @NonNull Weapon weapon;
    public float weaponOffsetY, engineOffset = 6f, engineSize = 2f;
    public ObjectSet<StatusEffect> immunities = new ObjectSet<>();
    public Sound deathSound = Sounds.bang;

    public TextureRegion legRegion, baseRegion, region;

    public <T extends BaseUnit> UnitType(String name, Prov<T> mainConstructor){
        this(name);
        create(mainConstructor);
    }

    public UnitType(String name){
        super(name);
    }

    public <T extends BaseUnit> void create(Prov<T> mainConstructor){
        this.constructor = mainConstructor;
        this.description = Core.bundle.getOrNull("unit." + name + ".description");
        this.typeID = new TypeID(name, mainConstructor);
    }

    @Override
    public void displayInfo(Table table){
        ContentDisplay.displayUnit(table, this);
    }

    @Override
    public void load(){
        weapon.load();
        region = Core.atlas.find(name);
        legRegion = Core.atlas.find(name + "-leg");
        baseRegion = Core.atlas.find(name + "-base");
    }

    @Override
    public ContentType getContentType(){
        return ContentType.unit;
    }

    public BaseUnit create(Team team){
        BaseUnit unit = constructor.get();
        unit.init(this, team);
        return unit;
    }
}
