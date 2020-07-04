package Atom.Nitrogen;

import Atom.Beryllium.TimeKeeper;
import Atom.Hydrogen.Hydrogen;
import Atom.Lithium.Net;
import Atom.Manifest;
import arc.util.Log;
import mindustry.ClientLauncher;

import java.io.File;
import java.net.URISyntaxException;


public class Info extends Nitrogen {

    private final String Method;
    private final Hydrogen hydrogen = new Hydrogen();
    private final TimeKeeper tk = new TimeKeeper();
    private final Net net = new Net();

    public Info(String method) {
        this.Method = method;
        SatisfyInfo();
    }

    public File getCurrentJarPath(){
        try {
            return new File(ClientLauncher.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
        } catch (URISyntaxException e) {
            return null;
        }
    }

    public void SatisfyInfo() {
        try {
            net.getProvider();
        } catch (Exception ignored) {
        }
        tk.getDates(true);
    }

    public void err(Object v) {
        Log.err(Method + ": " + v);
    }

    public void debug(Object v) {
        if (Manifest.debugMode) Log.warn(Method + "-Debug: " + v);
    }

    protected void say(Object v) {
        Log.info(Method + ": " + v);
    }
}
