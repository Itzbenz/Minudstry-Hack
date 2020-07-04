package Photon.Information;

import Atom.Lithium.Client;
import Atom.Lithium.Servers;
import Atom.Manifest;
import arc.util.Log;
import mindustry.Vars;

import java.io.IOException;
import java.util.function.Consumer;

public class NetworkInterface {
    public static final int port = 15000;
    private Servers servers;
    private Client client;

    public NetworkInterface(){
        try{
            servers = new Servers(port);
            Manifest.isSlave = false;
            Log.info("Running as server at port: " + port);
            return;
        } catch (IOException e) {
            e.getMessage();
        }
        try {
            client = new Client(port);
            Manifest.isSlave = true;
            Log.info("Running as client at port: " + port);
            tellMaster("Online");
        } catch (IOException ioException) {
            ioException.getMessage();
        }
    }

    public void tellMaster(String s){
        if(Manifest.isSlave)
            client.output.println(Vars.player.name + ": " + s);
    }

    public void tellAllClient(String s){
        if(!Manifest.isSlave)
            servers.sendToEveryClient(s);
    }

    public void tellAllClient(String s, int delay){
        if(!Manifest.isSlave)
            servers.sendToEveryClientDelay(s, delay);
    }

    public void tellClient(String s){
        if(!Manifest.isSlave)
            servers.sendToClient(s);
    }

    public void addInputListenerClient(Consumer<String> stringConsumer){
        if(Manifest.isSlave)
       client.addInputListener(stringConsumer);
    }

    public void addInputListenerServer(Consumer<String> stringConsumer){
        if(!Manifest.isSlave)
        servers.addInputListener(stringConsumer);
    }
}
