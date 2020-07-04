package Photon;

import Atom.Beryllium.TimeKeeper;
import Atom.Helium.Obfuscate;
import Atom.Helium.Randoms;
import Atom.Hydrogen.Hydrogen;
import Atom.Lithium.Net;
import Atom.Manifest;
import Atom.Nitrogen.Info;
import Photon.Information.*;
import arc.Events;
import arc.math.Mathf;
import arc.struct.Array;
import mindustry.Vars;
import mindustry.game.EventType;

public class gae {
    public static long key = 34143525324572352L;
    public static String[] colors = {"[#d91d00]", "[#fcc277]", "[#84fc77]", "[#77fcc0]", "[#2f83eb]", "[#9d2feb]", "[#db27cc]", "[#f22e2e]", "[#ed7824]", "[#edd224]", "[#24ed6a]", "[red]", "[royal]", "[red]", "[yellow]", "[green]", "[blue]",};
    public static Net                    net ;
    public static TimeKeeper             timeKeeper ;
    public static Array<CommandsList>    commandsLists ;
    public static Randoms                rands ;
    public static Hydrogen               hydrogen ;
    public static Obfuscate              obfuscate ;
    public static BreakingNews           news ;
    public static Commands               commandsNews ;
    public static Reporter               reporter;
    public static Manipulator            manipulator;
    public static Info                   info;
    public static NetworkInterface ni;
    public static CommandCenter commandCenter;


    public static void init(){
        net =               new Net();
        timeKeeper =        new TimeKeeper();
        commandsLists =     new Array<>();
        rands =             new Randoms();
        hydrogen =          new Hydrogen();
        obfuscate =         new Obfuscate();
        news =              new BreakingNews();
        commandsNews =      new Commands();
        reporter =          new Reporter();
        manipulator =       new Manipulator();
        info =              new Info("Manifest");

    }

    public static void lateInit(){
        if(Vars.android)
            return;
        Events.on(EventType.LateInit.class, () -> {
            if(!Manifest.pendingServer.equalsIgnoreCase("null"))
                gae.commandCenter.slaveConnectToServer(Manifest.pendingServer);
        });
        Events.on(EventType.Trigger.update, ()-> Manifest.mainThreadAlive = true);
        ni = new NetworkInterface();
        commandCenter = new CommandCenter();
        Stats.Init();
        PlayAI.Init();

    }

    public void test(){
        Events.on(EventType.Trigger.update, ()->{
            Vars.player.color.set(Mathf.random(255), Mathf.random(255), Mathf.random(255));
        });
    }
}
