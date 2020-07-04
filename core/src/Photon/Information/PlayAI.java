package Photon.Information;

import Atom.Manifest;
import Photon.ToolKit.Auto;
import arc.Events;
import mindustry.Vars;
import mindustry.core.GameState;
import mindustry.entities.type.TileEntity;
import mindustry.game.EventType;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class PlayAI {
    public static Mode Todo = Mode.AFK;
    private static final ScheduledExecutorService Coroutine = Executors.newScheduledThreadPool(2);
    private static final boolean enabled = false;
    //Make Task
    public static void Init(){
        if (!enabled)
            return;
        Events.on(EventType.Trigger.update, PlayAI::Update);
        Coroutine.scheduleAtFixedRate(PlayAI::scheduleTask, 15,5, TimeUnit.SECONDS);
    }

    //Do every time
    public static void Update(){
        if( !Vars.state.getState().equals(GameState.State.playing) || !Manifest.autoMode)
            return;
        switch (Todo){
            case AFK:AFK();
            case DefendCore:DefendCore();
            case MineOre:MineOre();
        }
    }

    private static void DefendCore(){

    }

    private static void MineOre(){
        if(Vars.griefWarnings.auto.mode != Auto.Mode.mine)
            Vars.griefWarnings.auto.goMine();
    }

    private static void AFK(){
        if(Stats.canMine)
            Todo = Mode.MineOre;
        else
            Todo = Mode.DefendCore;
    }

    //What should be do every specific time
    private static void scheduleTask(){
        TileEntity core = Vars.player.getClosestCore();
        Stats.buildingThatCanBeBuild = buildingCanBeBuilded();
        Stats.coreEmptySpaceLeft = core.items.total() - core.block.itemCapacity;
        Stats.canBuild = Stats.buildingThatCanBeBuild != 0;
        Stats.canMine = Stats.coreEmptySpaceLeft != 0;
    }

    // Don't Do This Every Second, Its Really Expensive
    public static int buildingCanBeBuilded(){
        TileEntity core = Vars.player.getClosestCore();
        if(core == null)
            return 0;
        AtomicInteger canBuild = new AtomicInteger();
        Vars.content.blocks().each(b ->{
            if(!b.buildVisibility.visible())
                return;
            if(core.items.has(b.requirements, Vars.state.rules.buildCostMultiplier))
                canBuild.getAndIncrement();
        });
        return canBuild.get();
    }

    public enum Mode {Move, Attack, AFK, BuildDefense, BuildUnitFactory, BuildMine, Spam, MineOre, DefendCore, TerminateGriefer}
}
