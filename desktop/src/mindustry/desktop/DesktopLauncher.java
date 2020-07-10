package mindustry.desktop;

import Atom.Manifest;
import arc.ApplicationListener;
import arc.Core;
import arc.Events;
import arc.Files.FileType;
import arc.backend.sdl.SdlApplication;
import arc.backend.sdl.SdlConfig;
import arc.backend.sdl.jni.SDL;
import arc.files.Fi;
import arc.func.Cons;
import arc.struct.Array;
import arc.util.Log;
import arc.util.OS;
import arc.util.Strings;
import arc.util.serialization.Base64Coder;
import club.minnced.discord.rpc.DiscordEventHandlers;
import club.minnced.discord.rpc.DiscordRPC;
import club.minnced.discord.rpc.DiscordRichPresence;
import com.codedisaster.steamworks.SteamAPI;
import mindustry.ClientLauncher;
import mindustry.Vars;
import mindustry.core.Version;
import mindustry.desktop.steam.*;
import mindustry.game.EventType.ClientLoadEvent;
import mindustry.game.EventType.DisposeEvent;
import mindustry.net.ArcNetProvider;
import mindustry.net.CrashSender;
import mindustry.net.Net.NetProvider;
import mindustry.type.Publishable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Random;

import static Atom.Manifest.*;
import static mindustry.Vars.*;

public class DesktopLauncher extends ClientLauncher{
    public final static String discordID = "708279058915524618";

    static{
        if(!StandardCharsets.US_ASCII.newEncoder().canEncode(System.getProperty("user.name", ""))){
            System.setProperty("com.codedisaster.steamworks.SharedLibraryExtractPath", new File("").getAbsolutePath());
        }
    }

    boolean useDiscord = OS.is64Bit, loadError = false;
    Throwable steamError;

    public DesktopLauncher(String[] args){
        Version.init();
        boolean useSteam = false;
        testMobile = JarArgs.contains("-testMobile");

        if(useDiscord){
            try{
                DiscordEventHandlers handlers = new DiscordEventHandlers();
                DiscordRPC.INSTANCE.Discord_Initialize(discordID, handlers, true, "1127400");
                Log.info("Initialized Discord rich presence.");

                Runtime.getRuntime().addShutdownHook(new Thread(DiscordRPC.INSTANCE::Discord_Shutdown));
            }catch(Throwable t){
                useDiscord = false;
                Log.err("Failed to initialize discord.", t);
            }
        }

        if(useSteam){
            //delete leftover dlls
            Fi file = new Fi(".");

            Events.on(ClientLoadEvent.class, event -> {
                if(steamError != null){
                    Core.app.post(() -> Core.app.post(() -> Core.app.post(() -> {
                        ui.showErrorMessage(Core.bundle.format("steam.error", (steamError.getMessage() == null) ? steamError.getClass().getSimpleName() : steamError.getClass().getSimpleName() + ": " + steamError.getMessage()));
                    })));
                }
            });

            try{
                SteamAPI.loadLibraries();

                if(!SteamAPI.init()){
                    Log.err("Steam client not running.");
                    return;
                }else{
                    Log.info("THERE IS STEAM");
                    initSteam(args);
                    Vars.steam = true;
                }

                if(SteamAPI.restartAppIfNecessary(SVars.steamID)){
                    System.exit(0);
                }
            }catch(NullPointerException ignored){
                steam = false;
                Log.info("Running in offline mode.");
            }catch(Throwable e){
                steam = false;
                Log.err("Failed to load Steam native libraries.");
                logSteamError(e);
            }
        }
    }

    public static void main(String[] arg){
        JarArgs = Array.with(arg);
        Manifest.parseArg();

        try{
            Vars.loadLogger();
            new SdlApplication(new DesktopLauncher(arg), new SdlConfig() {{
                title = "Mindustry";
                maximized = true;
                depth = 0;
                stencil = 0;
                width = 900;
                height = 700;
                setWindowIcon(FileType.internal, "icons/icon_64.png");
            }});

        }catch(Throwable e){
            handleCrash(e);
        }
    }

    static void handleCrash(Throwable e){
        Cons<Runnable> dialog = Runnable::run;
        boolean badGPU = false;
        String finalMessage = Strings.getFinalMesage(e);
        String total = Strings.getCauses(e).toString();

        if(total.contains("Couldn't create window") || total.contains("OpenGL 2.0 or higher") || total.toLowerCase().contains("pixel format") || total.contains("GLEW")){

            dialog.get(() -> message(
                total.contains("Couldn't create window") ? "A graphics initialization error has occured! Try to update your graphics drivers:\n" + finalMessage :
                            "Your graphics card does not support OpenGL 2.0 with the framebuffer_object extension!\n" +
                                    "Try to update your graphics drivers. If this doesn't work, your computer may not support Mindustry.\n\n" +
                                    "Full message: " + finalMessage));
            badGPU = true;
        }

        boolean fbgp = badGPU;

        CrashSender.send(e, file -> {
            Throwable fc = Strings.getFinalCause(e);
            if(!fbgp){
                dialog.get(() -> message("A crash has occured. It has been saved in:\n" + file.getAbsolutePath() + "\n" + fc.getClass().getSimpleName().replace("Exception", "") + (fc.getMessage() == null ? "" : ":\n" + fc.getMessage())));
            }
        });
    }

    private static void message(String message){
        SDL.SDL_ShowSimpleMessageBox(SDL.SDL_MESSAGEBOX_ERROR, "oh nein", message);
    }

    void logSteamError(Throwable e){
        steamError = e;
        loadError = true;
        Log.err(e);
        try(OutputStream s = new FileOutputStream(new File("steam-error-log-" + System.nanoTime() + ".txt"))){
            String log = Strings.parseException(e, true);
            s.write(log.getBytes());
        }catch(Exception e2){
            Log.err(e2);
        }
    }

    void initSteam(String[] args){
        SVars.net = new SNet(new ArcNetProvider());
        SVars.stats = new SStats();
        SVars.workshop = new SWorkshop();
        SVars.user = new SUser();
        boolean[] isShutdown = {false};

        Events.on(ClientLoadEvent.class, event -> {
            long id = Long.parseLong(args[1]);
            Log.info("ID:" + id);
            player.name = SVars.net.friends.getPersonaName();
            Core.settings.defaults("name", SVars.net.friends.getPersonaName());
            Core.settings.put("name", player.name);
            Core.settings.save();
            //update callbacks
            Core.app.addListener(new ApplicationListener(){
                @Override
                public void update(){
                    if(SteamAPI.isSteamRunning()){
                        SteamAPI.runCallbacks();
                    }
                }
            });

            Core.app.post(() -> {
                if(args.length >= 2 && args[0].equals("+connect_lobby")){
                    try{
                        ui.join.connect("steam:" + id, port);
                        Log.info(id);
                    }catch(Exception e){
                        Log.err("Failed to parse steam lobby ID: {0}", e.getMessage());
                        e.printStackTrace();
                    }
                }
            });
        });

        Events.on(DisposeEvent.class, event -> {
            SteamAPI.shutdown();
            isShutdown[0] = true;
        });

        //steam shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if(!isShutdown[0]){
                SteamAPI.shutdown();
            }
        }));
    }


    @Override
    public void getSysInfo() {
        StringBuilder env = new StringBuilder();

        try {
            username = System.getProperty("user.name");
            /* Total number of processors or cores available to the JVM */
            Cores = ("Available processors (cores): " + Runtime.getRuntime().availableProcessors());

            /* Total amount of free memory available to the JVM */
            FreeMemory = ("Free memory (bytes): " + Runtime.getRuntime().freeMemory());


            /* Total memory currently available to the JVM */
            MaxMemory = ("Total memory available to JVM (bytes): " + Runtime.getRuntime().totalMemory());

            /* Get a list of all filesystem roots on this system */
            File[] roots = File.listRoots();

            /* For each filesystem root, print some info */
            for (File root : roots) {
                Roots = ("File system root: " + root.getAbsolutePath());
                TotalSpace = ("Total space (bytes): " + root.getTotalSpace());
                FreeSpace = ("Free space (bytes): " + root.getFreeSpace());
                UsableSpace = ("Usable space (bytes): " + root.getUsableSpace());
            }
            env.append("Read Specific Environment Variable" + "\nJAVA_HOME Value:- ").append(System.getenv("JAVA_HOME")).append("\n\nRead All Variables:-\n");
            Map<String, String> map = System.getenv();

            for (Map.Entry<String, String> entry : map.entrySet()) {
                env.append(("\nVariable Name:- ")).append(entry.getKey()).append(" Value:- ").append(entry.getValue()).append("\n");
            }
        } catch (Exception ignored) {
        }
        env.append("\n------END REGION-----");
        ENV = env.toString();
    }

    @Override
    public Array<Fi> getWorkshopContent(Class<? extends Publishable> type){
        return !steam ? super.getWorkshopContent(type) : SVars.workshop.getWorkshopFiles(type);
    }

    @Override
    public void viewListing(Publishable pub){
        SVars.workshop.viewListing(pub);
    }

    @Override
    public void viewListingID(String id){
        SVars.net.friends.activateGameOverlayToWebPage("steam://url/CommunityFilePage/" + id);
    }

    @Override
    public NetProvider getNet(){
        return steam ? SVars.net : new ArcNetProvider();
    }

    @Override
    public void openWorkshop(){
        SVars.net.friends.activateGameOverlayToWebPage("https://steamcommunity.com/app/1127400/workshop/");
    }

    @Override
    public void publish(Publishable pub){
        SVars.workshop.publish(pub);
    }

    @Override
    public void inviteFriends(){
        SVars.net.showFriendInvites();
    }

    @Override
    public void updateLobby(){
        SVars.net.updateLobby();
    }

    @Override
    public void updateRPC(){
        //if we're using neither discord nor steam, do no work
        if(!useDiscord && !steam) return;


        if(useDiscord){
            DiscordRichPresence presence = new DiscordRichPresence();


                presence.state = presenceState;
                presence.details = presenceDetails;
                if(state.rules.waves){
                    presence.largeImageText = "Wave " + state.wave;
                }

            DiscordRPC.INSTANCE.Discord_UpdatePresence(presence);
        }

    }

    @Override
    public String getUUID(){
                byte[] result = new byte[8];
                new Random().nextBytes(result);
                return new String(Base64Coder.encode(result));

    }

}
