package Photon.Information;


import Atom.Manifest;
import Photon.gae;
import arc.Core;
import arc.math.Mathf;
import arc.struct.Array;
import arc.util.Log;
import arc.util.OS;
import arc.util.Time;
import arc.util.serialization.Base64Coder;
import mindustry.content.Mechs;
import mindustry.ctype.ContentType;
import mindustry.entities.traits.BuilderTrait;
import mindustry.entities.type.Player;
import mindustry.gen.Call;
import mindustry.type.Mech;
import mindustry.world.Block;
import mindustry.world.Build;
import mindustry.world.Tile;
import mindustry.world.blocks.units.MechPad;
import mindustry.world.meta.BlockFlag;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static mindustry.Vars.*;

public class CommandCenter {
    private final ExecutorService executor = Executors.newFixedThreadPool(1);
    private final HashMap<String, Runnable> consoleArray = new HashMap<>();
    private Array<String> args = new Array<>();

    public CommandCenter() {
        consoleArray.put("makeSlave", this::makeSlave);
        consoleArray.put("pingSlave", this::pingSlave);
        consoleArray.put("destroySlave", this::destroySlave);
        consoleArray.put("help", this::help);
        consoleArray.put("exit", this::exit);
        consoleArray.put("leave", this::leaveServer);
        consoleArray.put("join", this::joinSever);
        consoleArray.put("mech", this::changeSlaveMech);
        consoleArray.put("assist", this::assist);
        consoleArray.put("sendChat", this::sendChat);
        gae.ni.addInputListenerClient(this::handleCOMM);
        gae.ni.addInputListenerClient(Log::info);
        gae.ni.addInputListenerServer(CommandCenter::reply);
    }

    public static void reply(String message) {
        if (headless || loadedLogger)
            Log.info(message);
        else if (ui.scriptfrag != null)
            ui.scriptfrag.addReply(message);

    }

    public static void wait(int milis) {
        try {
            Thread.sleep(milis);
        } catch (InterruptedException e) {
        }
    }

    public static void runOffThread(Runnable run) {
        Thread t = new Thread(run);
        t.setDaemon(true);
        t.start();
    }

    // ----------------------------------- Master -------------------------------------------------
    public boolean handleConsole(String message) {
        if (message == null)
            return false;
        if (!message.startsWith("/") || message.length() <= 1) return false;
        message = gae.hydrogen.RemoveFirstChar(message);
        args = Array.with(message.split(" "));
        if (consoleArray.containsKey(args.get(0))) {
            consoleArray.get(args.get(0)).run();
            return true;
        } else
            return false;
    }

    private void help() {
        StringBuilder sb = new StringBuilder();
        consoleArray.forEach((s, r) -> {
            sb.append(s);
            sb.append(", ");
        });
        reply(sb.toString());
    }

    private void sendChat() {
        if (args.size == 1) {
            reply("Not Enough Argument");
            return;
        }
        gae.ni.tellAllClient("CHAT " + args.get(1));
    }

    private void destroySlave() {
        gae.ni.tellAllClient("EXIT");
    }

    private void changeSlaveMech() {
        if (args.size == 1) {
            reply("Not Enough Argument");
            return;
        }
        Array<String> mechList = new Array<>();
        mechList.add(Mechs.alpha.name, Mechs.dart.name, Mechs.delta.name, Mechs.javelin.name);
        mechList.add(Mechs.glaive.name, Mechs.omega.name, Mechs.tau.name, Mechs.trident.name);
        if (mechList.contains(args.get(1).toLowerCase()))
            tellSlave("MECH " + args.get(1).toLowerCase());
        else if (args.get(1).equalsIgnoreCase("random")) {
            gae.ni.tellAllClient("MECH " + "RANDOM", Mathf.random(15000));
        } else {
            reply("Non Existent Mech Use Name Below: ");
            reply("random, ");
            reply(gae.hydrogen.joiner(", ", mechList));
        }

    }

    public void joinSever() {
        if (args.size == 1) {
            reply("Not Enough Argument");
            return;
        }
        String[] ips = args.get(1).split(":");
        if (ips.length == 2) {
            connectToServer(ips[0], ips[1]);
        } else {
            reply("Invalid IP: " + gae.hydrogen.joiner(":", Array.with(ips)));
        }
    }

    public void slaveConnectToServer(String ip) {
        Log.info("gae connecting: " + ip);
        String[] ips = ip.split(":");
        if (ips.length == 2)
            tellAllSlave("CONNECT " + ip);
    }

    //FIX THIS
    public void tellSlave(String comm) {
        gae.ni.tellClient(comm);
    }

    public void tellAllSlave(String comm) {
        gae.ni.tellAllClient(comm);
    }

    private void assist() {
        if (args.size == 1) {
            reply("Not Enough Argument");
            return;
        }
        String p = args.get(1);
        Player ps = playerGroup.find(b -> b.name.equalsIgnoreCase(p));
        if (player == null) {
            reply("Player not found");
        } else {
            tellAllSlave("ASSIST " + ps.name);
        }

    }

    private void makeSlave() {
        if (args.size <= 1) {
            reply("Not Enough Argument");
            return;
        }
        int howMany = Integer.parseInt(args.get(1));

        String arg = "";
        args.removeRange(0, 1);
        if (args.size != 0)
            arg = gae.hydrogen.joiner(" ", args);
        if (!OS.isAndroid) {
            for (int i = Manifest.processes.size; i < howMany; i++) {
                int finalI = i;
                String finalArg = arg;
                Time.runTask(Mathf.random(1000), () ->
                        makeProcess("java -jar " + gae.info.getCurrentJarPath().getAbsolutePath()
                        + " -slave " + " -name " + player.name + Mathf.random(1000) +
                        (net.active() ? " -server " + Manifest.currentServer : "") + finalArg));
            }
        }
    }

    public void pingSlave() {
        tellAllSlave("PING");
    }

    public void handleBuildRequest(BuilderTrait.BuildRequest buildRequest) {
        if(buildRequest.breaking)
            slaveRemoveBlock(buildRequest.x, buildRequest.y);
        else
            slaveBuildBlock(buildRequest.x, buildRequest.y, buildRequest.rotation, buildRequest.block);
    }


    public void slaveRemoveBlock(int x, int y) {
        tellSlave("BUILDREMOVE " + Base64Coder.encodeString(x + " " + y));
    }

    public void slaveBuildBlock(int x, int y, int rotation, Block block) {
        tellSlave("BUILDREQUEST " + Base64Coder.encodeString(x + " " + y + " " + rotation + " " + block.name));
    }

    // ---------------------------------------------- Neutral ------------------------------------------------------

    public void makeProcess(String cmd) {
        try {
            Runtime.getRuntime().exec(cmd);
        } catch (IOException ex) {
            reply(ex.getMessage());
        }
    }


    // ------------------------------------------- Slave -----------------------------------------------------------


    private void handleCOMM(String comm) {
        Array<String> arg = Array.with(comm.split(" "));
        if (comm.startsWith("PING")) {
            tellMaster("PING!");
        } else if (comm.startsWith("CONNECT")) {
            String[] ips = arg.get(arg.indexOf("CONNECT") + 1).split(":");
            if (ips.length == 2)
                connectToServer(ips[0], ips[1]);
            else
                tellMaster("Invalid IP");
        } else if (comm.startsWith("BUILDREQUEST")) {
            if (arg.size == 1) {
                tellMaster("Not Enough Argument");
            } else {
                buildBlock(arg.get(1));
            }
        } else if (comm.startsWith("BUILDREMOVE")) {
            if (arg.size == 1) {
                tellMaster("Not Enough Argument");
            } else {
                removeBlock(arg.get(1));
            }
        } else if (comm.startsWith("ASSIST")) {
            if (arg.size == 1) {
                tellMaster("Not Enough Argument");
            } else {
                griefWarnings.auto.assistEntity(griefWarnings.commandHandler.getPlayer(comm.replaceFirst("ASSIST ", "")), Mathf.random(20f));
            }
        } else if (comm.startsWith("DRAIN")) {
            griefWarnings.auto.drainResource();
        } else if (comm.startsWith("MINE")) {
            griefWarnings.auto.goMine();
        } else if (comm.startsWith("MECH")) {
            if (arg.size == 1) {
                tellMaster("Not Enough Argument");
            } else {
                changeMech(arg.get(1));
            }
        } else if (comm.startsWith("CHAT")) {
            arg.remove("CHAT");
            Call.sendChatMessage(gae.hydrogen.joiner(" ", arg));
        } else if (comm.startsWith("LEAVE")) {
            leaveServer();
        } else if (comm.startsWith("EXIT")) {
            exit();
        } else {
            tellMaster("No Match");
        }
    }

    public void changeMech(String mechName) {
        Mech old = player.mech;
        Array<Tile> array = BreakingNews.indexTile(BlockFlag.mechPad);
        Tile target = null;
        if (!mechName.equalsIgnoreCase("random")) {
            for (Tile tile : array) {
                Mech mech = ((MechPad) tile.block()).mech;
                if (mech.name.equalsIgnoreCase(mechName)) {
                    target = tile;
                    break;
                }
            }
        } else
            target = array.random();
        if (target == null)
            return;
        griefWarnings.auto.gotoTile(target, 1f);
        target.block().tapped(target, player);

    }

    public void connectToServer(String ip, String port) {
        ui.join.connect(ip, Integer.parseInt(port));
    }


    public void tellMaster(String msg) {
        gae.ni.tellMaster(msg);
    }


    public void leaveServer() {
        netClient.disconnectQuietly();
    }

    private void exit() {
        tellAllSlave("EXIT");
        Core.app.exit();
    }

    public void buildBlock(String base64) {
        Array<String> arg = Array.with((Base64Coder.decodeString(base64).split(" ")));
        if (arg.size != 4)
            return;
        player.isBuilding = true;
        player.buildWasAutoPaused = false;
        buildBlock(Integer.parseInt(arg.get(0)), Integer.parseInt(arg.get(1)), Integer.parseInt(arg.get(2)), stringToBlock(arg.get(3)));
    }

    public void removeBlock(String base64) {
        Array<String> arg = Array.with((Base64Coder.decodeString(base64).split(" ")));
        if (arg.size != 2)
            return;
        player.isBuilding = true;
        player.buildWasAutoPaused = false;
        breakBlock(Integer.parseInt(arg.get(0)), Integer.parseInt(arg.get(1)));
    }

    public void buildBlock(int x, int y, int rotation, Block block) {
        if(world.tile(x, y).block() == null || world.tile(x, y).block() != block)
            player.addBuildRequest(new BuilderTrait.BuildRequest(x, y, rotation, block));
    }

    public void breakBlock(int x, int y) {
        if (validBreak(x, y)) {
            Tile tile = world.ltile(x, y);
            player.addBuildRequest(new BuilderTrait.BuildRequest(tile.x, tile.y));
        }
    }

    private Block stringToBlock(String name) {
        if (name == null) return null;
        if (name.isEmpty()) return null;
        return content.getByName(ContentType.block, name);
    }

    public boolean validBreak(int x, int y) {
        return Build.validBreak(player.getTeam(), x, y);
    }
}
