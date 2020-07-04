package mindustry.net;

import Atom.Manifest;
import arc.Core;
import arc.Net.HttpMethod;
import arc.Net.HttpRequest;
import arc.Net.HttpResponse;
import arc.files.Fi;
import arc.func.Cons;
import arc.struct.ObjectMap;
import arc.util.Log;
import arc.util.NetJavaImpl;
import arc.util.OS;
import arc.util.Strings;
import arc.util.io.PropertiesUtils;
import arc.util.serialization.JsonValue;
import arc.util.serialization.JsonValue.ValueType;
import mindustry.Vars;
import mindustry.core.Version;

import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import static mindustry.Vars.net;

public class CrashSender{

    public static void log(Throwable exception){
        try{
            Core.settings.getDataDirectory().child("crashes").child("crash_" + System.currentTimeMillis() + ".txt").writeString(Strings.parseException(exception, true));
        }catch(Throwable ignored){
        }

        if(exception instanceof RuntimeException){
            throw (RuntimeException)exception;
        }
        throw new RuntimeException(exception);
    }

    public static void send(Throwable exception, Cons<File> writeListener){

        try{
            exception.printStackTrace();
            if(System.getProperty("user.name").equals("Itz"))
                return;

            //attempt to load version regardless
            if(Version.number == 0){
                try{
                    ObjectMap<String, String> map = new ObjectMap<>();
                    PropertiesUtils.load(map, new InputStreamReader(CrashSender.class.getResourceAsStream("/version.properties")));

                }catch(Throwable e){
                    e.printStackTrace();
                    Log.err("Failed to parse version.");
                }
            }

            try{
                File file = new File(OS.getAppDataDirectoryString(Vars.appName), "crashes/crash-report-" + new SimpleDateFormat("MM_dd_yyyy_HH_mm_ss").format(new Date()) + ".txt");
                new Fi(OS.getAppDataDirectoryString(Vars.appName)).child("crashes").mkdirs();
                new Fi(file).writeString(parseException(exception));
                writeListener.get(file);
            }catch(Throwable e){
                Log.err("Failed to save local crash report.", e);
            }





            boolean netActive = false, netServer = false;

            //attempt to close connections, if applicable
            try{
                netActive = net.active();
                netServer = net.server();
                net.dispose();
            }catch(Throwable ignored){
            }

            JsonValue value = new JsonValue(ValueType.object);

            boolean fn = netActive, fs = netServer;

            //add all relevant info, ignoring exceptions
            ex(() -> value.addChild("versionType", new JsonValue(Version.type)));
            ex(() -> value.addChild("versionNumber", new JsonValue(Version.number)));
            ex(() -> value.addChild("versionModifier", new JsonValue(Version.modifier)));
            ex(() -> value.addChild("build", new JsonValue(Version.build)));
            ex(() -> value.addChild("revision", new JsonValue(Version.revision)));
            ex(() -> value.addChild("net", new JsonValue(fn)));
            ex(() -> value.addChild("server", new JsonValue(fs)));
            ex(() -> value.addChild("players", new JsonValue(Vars.playerGroup.size())));
            ex(() -> value.addChild("state", new JsonValue(Vars.state.getState().name())));
            ex(() -> value.addChild("os", new JsonValue(System.getProperty("os.name") + "x" + (OS.is64Bit ? "64" : "32"))));
            ex(() -> value.addChild("trace", new JsonValue(parseException(exception))));
            ex(() -> value.addChild("javaVersion", new JsonValue(System.getProperty("java.version"))));
            ex(() -> value.addChild("javaArch", new JsonValue(System.getProperty("sun.arch.data.model"))));

            boolean[] sent = {false};
            httpPost(Manifest.crashReportURL, value.toString(), s ->{}, s ->{});
            Log.info("Sending crash report.");
            //post to crash report URL


            //sleep until report is sent
            try{
                while(!sent[0]){
                    Thread.sleep(30);
                }
            }catch(InterruptedException ignored){}
        }catch(Throwable death){
            death.printStackTrace();
        }

        ret();
    }

    private static void ret(){
        System.exit(1);
    }

    private static void httpPost(String url, String content, Cons<HttpResponse> success, Cons<Throwable> failure){
        new NetJavaImpl().http(new HttpRequest().method(HttpMethod.POST).content(content).url(url), success, failure);
    }

    private static String parseException(Throwable e){
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }

    private static void ex(Runnable r){
        try{
            r.run();
        }catch(Throwable ignored){
        }
    }
}
