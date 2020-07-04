package Atom.Carbon;

import Atom.Manifest;
import Photon.UI.UF;
import arc.ApplicationCore;
import arc.ApplicationListener;
import arc.Events;
import arc.assets.AssetManager;
import arc.assets.Loadable;
import arc.util.Log;
import mindustry.Vars;
import mindustry.core.Logic;
import mindustry.core.NetClient;
import mindustry.core.Platform;
import mindustry.game.EventType;
import mindustry.net.Net;

import static Atom.Manifest.isSlave;
import static arc.Core.assets;
import static mindustry.Vars.*;

public class BotLauncher extends ApplicationCore {

    @Override
    public void init(){
        Log.info( (isSlave ? "Slave " : "Master ")+ "Headless Mode");

        loadLocales = false;
        headless = true;

        Manifest.netBot = new NetBot();
        Vars.net = new Net(platform.getNet());
        Vars.platform = new Platform(){};
        assets = new AssetManager();

        Vars.loadSettings();
        Vars.init();
        Manifest.init();

        add(logic = new Logic());
        add(netClient = new NetClient());
        add(Vars.ui = new UF());
        add(new BotControl(Manifest.JarArgs));


        Events.fire(new EventType.BotLoadEvent());
    }

    @Override
    public void add(ApplicationListener module){
        super.add(module);

        //autoload modules when necessary
        if(module instanceof Loadable){
            assets.load((Loadable)module);
        }
    }

    @Override
    public void setup() {
        init();
    }

}