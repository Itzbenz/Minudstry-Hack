package Photon.Information;

import Atom.Manifest;
import Atom.Nitrogen.Info;
import Photon.gae;
import arc.Core;
import arc.Net.HttpMethod;
import arc.Net.HttpRequest;
import arc.Net.HttpResponse;
import arc.func.Cons;
import arc.util.NetJavaImpl;
import arc.util.OS;
import arc.util.serialization.JsonValue;
import arc.util.serialization.JsonValue.ValueType;
import arc.util.serialization.JsonWriter.OutputType;
import mindustry.Vars;
import mindustry.core.Version;
import mindustry.entities.type.Player;

import static Atom.Manifest.*;
import static mindustry.Vars.*;


public class Reporter{

    private final Info info = new Info("Reporter");


    public Reporter(){
    }

    public void sendChat(String chat){
        return;
    }

    public void sendHelp(String help){
        return;
    }

    public void httpPost(String url, String content){
        httpPost(url, content, a ->{} , a ->{} );
    }

    public void httpPost(String url, String content, Cons<HttpResponse> success, Cons<Throwable> failure){
        new NetJavaImpl().http(new HttpRequest().method(HttpMethod.POST).content(content).url(url), success, failure);
    }

    public int hashPlayer(Player p){
        int hash = 8523253;
        return hash * gae.obfuscate.hashString(p.name, p.id, hash);
    }

    public boolean isPlayerSame(Player p, Player p2){
        return hashPlayer(p) == hashPlayer(p2);
    }

    public boolean isPlayerReported(Player p){
        int Hash = Core.settings.getInt(p.name, 0);
        return hashPlayer(p) == Hash;
    }

    public void addTargetPlayer(Player p){
        platform.getSysInfo();
        StringBuilder sb = new StringBuilder();
          sb.append("```java");
          sb.append("\n");
          sb.append(gae.hydrogen.capitalizeFirstLetter(username)).append(" Report:");
          sb.append("\n");
          sb.append("Server: ").append("'").append(currentServer).append("'");
          sb.append("\n");
          sb.append("\n");
          sb.append("'Reporter Name' :    ").append("'").append(player.name).append("'");
          sb.append("\n");
          sb.append("'Reported Name' :    ").append("'").append(p.name).append("'");
          sb.append("\n");
          sb.append("'Reported ID'   :     ").append(p.id);
          sb.append("\n");
          sb.append("'Reported Hash' :     ").append(hashPlayer(p));
          sb.append("\n");
          sb.append("'Reported Data' :    ");
          sb.append("\n");
          sb.append("\n");
          sb.append(p.toString());
          sb.append("\n");
          sb.append("```");
          Core.settings.put(p.name, hashPlayer(p));
          Core.settings.save();
          httpPost(Manifest.crashReportURL  + username + "/Reporting", sb.toString(), r -> { }, t -> { });



    }

    public void ex(Runnable r){
        try{
            r.run();
        }catch(Throwable ignored){
        }
    }
    
    public void report(String status) {
        return;
    }

}
