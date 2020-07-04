package Photon.ToolKit;

import Photon.Information.BreakingNews;
import Photon.Information.CommandsList;
import Photon.Information.Manipulator;
import Photon.ToolKit.Actions.Action;
import Photon.ToolKit.Actions.UndoResult;
import Photon.gae;
import arc.Core;
import arc.func.Cons;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.struct.Array;
import arc.struct.ObjectSet;
import arc.util.Log;
import arc.util.Time;
import mindustry.content.Blocks;
import mindustry.entities.traits.BuilderTrait;
import mindustry.entities.type.Player;
import mindustry.gen.Call;
import mindustry.net.Packets;
import mindustry.type.Item;
import mindustry.world.Block;
import mindustry.world.Pos;
import mindustry.world.Tile;
import mindustry.world.blocks.BlockPart;
import mindustry.world.blocks.distribution.BufferedItemBridge;
import mindustry.world.blocks.distribution.ExtendingItemBridge;
import mindustry.world.blocks.distribution.ItemBridge;
import mindustry.world.blocks.logic.MessageBlock;
import mindustry.world.meta.BlockFlag;
import org.mozilla.javascript.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import static Photon.gae.manipulator;
import static Photon.gae.rands;
import static mindustry.Vars.*;


// introducing the worst command system known to mankind
//lmao
public class CommandHandler {

    public ContextFactory scriptContextFactory = new ContextFactory();
    public Context scriptContext;
    public Scriptable scriptScope;
    public HashMap<String, Cons<CommandContext>> commands = new HashMap<>();

    public CommandHandler() {
        addCommand("Get Resource Status", this::getResource, 63544, "Return how many resource in nearby core", "Scan resource ");
        addCommand("Scan all ore", this::getOre, 63536, "Scan all ore in the world", "Scan all ore in world");
        addCommand("Teleport Aura", this::tpAura, 63550, "teleport randomly to another player", "teleport to random player");
        addCommand("Power Configure", this::configurePower, 63552, "Configure power node", "NULL",
                new String[]{"break", "fix", "mix", "break all", "fix all", "mix all"},
                new String[]{"Disconnect power node", "Reconnect power node", "Reconnect/Disconnect power node",
                        "Disconnect all power node", "Reconnect all power node", "Reconnect/Disconnect all power node"});
        addCommand("Sorter & Unloader Shuffle", this::SorterShuffle, 63632, "Shuffle all sorter around the world", "Shuffle all sorter");
        addCommand("Item Bridge Havoc", this::BridgeHavoc, 63634, "Randomly Connect and Disconnect Bridge Item", "Randomize bridge");
        addCommand("MassDriver Configurator", this::MassNullDrive, 63627, "MassDrive Configurator you can Disconnect/Reconnect massdrive", "Deactivate all massdrive",
                new String[]{"reconnect"},
                new String[]{"reconnect all massdriver to random massdriver"});
        addCommand("Conveyor Shuffle", this::ConveyorShuffle, 63637, "Rotate all conveyor randomly", "Rotate conveyor randomly");
        addCommand("tileinfo", this::tileInfo, 63568, "Print tile info in the log", "get tileinfo");
        addCommand("Source Shuffle", this::randomizeSource, 63550, "Randomize item and liquid source", "randomize source");
        addCommand("Message Block Configurator", this::messageBlockHandler, 63546, "Write/Read message block in the world", "NULL",
                new String[]{"read" , "obfuscate"},
                new String[]{"Read all messageBlock", "Obfuscate all messageBlock with random shit"}
                );



        // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        addCommand("code", this::code);
        addCommand("msg-write", this::writeToAllMessageBlock);
        addCommand("crash", this::crash);
        addCommand("drain", this::drain);
        addCommand("mine", this::mine);
        addCommand("send-help", this::sendHelp);
        addCommand("uuid", this::uuid);
        addCommand("uuids", this::ID);
        addCommand("auto-id", settingsToggle("change.uuid", "Change uuid on join", v -> aVoid(v)));
        addCommand("teleport", this::teleport);
        addCommand("help", this::help);
        addCommand("tileset", this::configureTile);
        addCommand("light", settingsToggle("state.rules.lighting", "light enviroment", v -> state.rules.lighting = v));
        addCommand("verbose", settingsToggle("verbose", "verbose logging", v -> griefWarnings.verbose = v));
        addCommand("debug", settingsToggle("debug", "debug logging", v -> griefWarnings.debug = v));
        addCommand("spam", this::spam);
        addCommand("broadcast", settingsToggle("broadcast", "broadcast of messages", v -> griefWarnings.broadcast = v));
        addCommand("players", this::players);
        addCommand("tileinfohud", settingsToggle("tileinfohud", "tile information hud", v -> griefWarnings.tileInfoHud = v));
        addCommand("autoban", settingsToggle("autoban", "automatic bans", v -> griefWarnings.autobahn = v));
        addCommand("autotrace", settingsToggle("autotrace", "automatic trace", v -> griefWarnings.autotrace = v));
        addCommand("auto", this::auto);
        addCommand("nextwave", this::nextwave, 63612, "Spam like a shit", "spam");
        addCommand("playerinfo", this::playerInfo);
        addCommand("pi", this::playerInfo); // playerinfo takes too long to type
        addCommand("eval", this::eval);
        addCommand("freecam", createToggle("freecam", "free movement of camera", v -> griefWarnings.auto.setFreecam(v)));
        addCommand("show", this::show);
        addCommand("logactions", settingsToggle("logactions", "log all actions captured by the action log", v -> griefWarnings.logActions = v));
        addCommand("getactions", this::getactions);
        addCommand("undoactions", this::undoactions);

        // mods context not yet initialized here
        scriptContext = scriptContextFactory.enterContext();
        scriptContext.setOptimizationLevel(9);
        scriptContext.getWrapFactory().setJavaPrimitiveWrap(false);
        scriptScope = new ImporterTopLevel(scriptContext);

        try {
            scriptContext.evaluateString(scriptScope, Core.files.internal("scripts/global.js").readString(), "global.js", 1, null);
        } catch (Throwable ex) {
            Log.err("global.js load failed", ex);
        } finally {
            Context.exit();
        }
    }

    public String runConsole(String text) {
        Context prevContext = Context.getCurrentContext();
        if (prevContext != null) Context.exit();
        Context ctx = scriptContextFactory.enterContext(scriptContext);
        try {
            Object o = ctx.evaluateString(scriptScope, text, "console.js", 1, null);
            if (o instanceof NativeJavaObject) {
                o = ((NativeJavaObject) o).unwrap();
            }
            if (o instanceof Undefined) {
                o = "undefined";
            }
            return String.valueOf(o);
        } catch (Throwable t) {
            Log.err("Script error", t);
            return t.toString();
        } finally {
            Context.exit();
            if (prevContext != null) platform.enterScriptContext(prevContext);
        }
    }

    public void addCommand(String name, Cons<CommandContext> handler) {
        commands.put(name, handler);
    }

    public void addCommand(String name, Cons<CommandContext> handler, int Icon, String Description, String Function) {

        commands.put(gae.hydrogen.commandsFormat(name), handler);
        CommandsList commandsList = new CommandsList();

        commandsList.addCommandList(name, Description, Icon, Function, null);
        gae.commandsLists.add(commandsList);
    }

    // This is mess
    public void addCommand(String name, Cons<CommandContext> handler, int Icon, String Description, String Function, String[] childCommandsName, String[] childMessageRunNotfication) {
        commands.put(gae.hydrogen.commandsFormat(name), handler);
        CommandsList commandsList = new CommandsList();
        Array<CommandsList.ChildCommandsList> childCommandsList = new Array<CommandsList.ChildCommandsList>();
        for (int i = 0; i < childCommandsName.length; ) {
            CommandsList.ChildCommandsList cml = new CommandsList.ChildCommandsList(name);
            cml.setChildCommandsName(childCommandsName[i]);
            cml.setChildCommandsRunPrompt(childMessageRunNotfication[i]);
            childCommandsList.add(cml);
            i++;
        }
        commandsList.addCommandList(name, Description, Icon, Function, childCommandsList);
        gae.commandsLists.add(commandsList);
    }

    public void reply(String message) {
        if (!ui.commandsGui.visible && !griefWarnings.broadcast)
            ui.chatfrag.addMessage(message, null);
        if (!ui.commandsGui.visible && griefWarnings.broadcast) {
            String[] messages;
            if (message.contains("\n")) {
                messages = gae.hydrogen.split(message, "\n");
                for (String s : messages) {
                    Call.sendChatMessage(s);
                }
            }
            Call.sendChatMessage(message);
        }
        gae.commandsNews.logMessage = message;
    }

    public boolean runCommand(String message) {

        if (!message.startsWith("/")) return false;
        String[] args = message.split(" ");
        args[0] = args[0].substring(1);
        Cons<CommandContext> command = commands.get(args[0].toLowerCase());
        if (command == null) {
            gae.commandsNews.CantBeExecuted();
            return false;
        }

        gae.commandsNews.Executed(gae.hydrogen.RemoveFirstChar(message));
        command.get(new CommandContext(Arrays.asList(args)));
        return true;
    }

    public void configureTile(CommandContext ctx) {
        try {
            if (ctx.args.size() < 1)
                return;
            if (!gae.hydrogen.ContainIntOnly(ctx.args.get(1)))
                return;
            int n = Integer.parseInt(ctx.args.get(1));
            Tile tile = getCursorTile();
            tile.configure(n);
        } catch (Throwable ignored) {
        }
    }

    public void ConveyorShuffle(CommandContext ctx) {
        Array<Block> block = new Array<>();
        block.add(Blocks.conveyor, Blocks.armoredConveyor, Blocks.titaniumConveyor);
        BuilderTrait.BuildRequest req = new BuilderTrait.BuildRequest();

        for (Tile tile : indexer.getAllied(player.getTeam(), BlockFlag.conveyor)) {
            if (tile == null)
                continue;
            Time.runTask(Mathf.random(20000F), () -> {
                rotate(req, block, tile);
            });
        }
    }

    public void rotate(BuilderTrait.BuildRequest req, Array<Block> block, Tile tile) {
        req.block = block.get(gae.rands.getRandomInt(block.size));
        req.x = tile.x;
        req.y = tile.y;
        req.rotation = gae.rands.getRandomInt(3);
        player.buildQueue().addFirst(req);
        player.buildQueue().addLast(req);
    }

    private void aVoid(boolean b) {

    }

    public void BridgeHavoc(CommandContext ctx) {
        Array<Tile> itemBridge = new Array<Tile>();
        Array<Tile> extendingItemBridge = new Array<Tile>();
        Array<Tile> bufferedItemBridge = new Array<Tile>();

        for (Tile tile : indexer.getAllied(player.getTeam(), BlockFlag.bridge)) {
            if (tile.block() instanceof ItemBridge)
                itemBridge.add(tile);
            if (tile.block() instanceof BufferedItemBridge)
                bufferedItemBridge.add(tile);
            if (tile.block() instanceof ExtendingItemBridge)
                extendingItemBridge.add(tile);
        }

        for (Tile tile : indexer.getAllied(player.getTeam(), BlockFlag.bridge)) {
            Tile other;
            if (tile.block() instanceof ItemBridge)
                other = itemBridge.get(gae.rands.getRandomInt(itemBridge.size));
            else if (tile.block() instanceof BufferedItemBridge)
                other = bufferedItemBridge.get(gae.rands.getRandomInt(bufferedItemBridge.size));
            else if (tile.block() instanceof ExtendingItemBridge)
                other = extendingItemBridge.get(gae.rands.getRandomInt(extendingItemBridge.size));
            else
                return;
            Blocks.itemBridge.onConfigureTileTapped(tile, other);
        }
    }

    public void crash(CommandContext ctx) {
        String args = "msg";
        if (ctx.args.size() > 1)
           args = ctx.args.get(1).toLowerCase();
        switch (args){
            case "sorter":
                forEachTile(BlockFlag.sorter, tile -> tile.configure(gae.rands.getRandomInt(content.items().size + 2)));
            case "message":
                forEachTile(BlockFlag.message, tile ->  writeBlock(tile,gae.rands.getRandomString(220)));
            default:
                reply("Method Invalid Use Method Below \n-sorter\n-message");
        }

    }

    public void SorterShuffle(CommandContext ctx) {
        for (Tile tile : indexer.getAllied(player.getTeam(), BlockFlag.sorter))
            Time.run(Mathf.random(10000F), ()->tile.configure(gae.rands.getRandomInt(content.items().size + 2)));
    }

    public void MassNullDrive(CommandContext ctx) {
        boolean reconnect = ctx.args.contains("reconnect");
        ObjectSet<Tile> massDrive = indexer.getAllied(player.getTeam(), BlockFlag.massDrive);
        Array<Tile> massDriveArray = new Array<Tile>();
        for (Tile tile : massDrive) {
            massDriveArray.add(tile);
        }
        for (Tile tile : massDrive) {
            Tile lel = massDriveArray.get(gae.rands.getRandomInt(massDriveArray.size - 1));
            if (!reconnect)
                tile.configure(Pos.invalid);
            else
                tile.configure((Pos.get(lel.x, lel.y)));
        }
    }


    private void messageBlockHandler(CommandContext ctx){
        if (ctx.args.size() < 1)
            return;
        String args = ctx.args.get(1).toLowerCase();
        switch (args) {
            case "obfuscate":
                forEachTile(BlockFlag.message, tile -> Time.run(Mathf.random(1000F),()-> writeBlock(tile,gae.rands.getRandomString(220))));
                  break;
            case "read":
                forEachTile(BlockFlag.message, tile -> {
                    MessageBlock.MessageBlockEntity ent = tile.ent();
                    String text = ent.message == null || ent.message.isEmpty() ? "[lightgray]" + Core.bundle.get("empty") : ent.message;
                    reply("At " + tile.x + ", " + tile.y);
                    reply(text);
                });
                break;
            default:
                return;
        }
        reply("[green]Done");
    }

    private void code(CommandContext ctx){
        Runnable runnable = ui.javaEditor::show;
        runnable.run();
    }

    private void writeToAllMessageBlock(CommandContext ctx){
        if (ctx.args.size() < 1)
            return;
        forEachTile(BlockFlag.message, tile -> Time.run(Mathf.random(100F),()-> writeBlock(tile,ctx.parseArg())));
    }

    private void writeBlock(Tile tile, String message){
        if(tile.ent() == null || message == null || !(tile.ent() instanceof MessageBlock.MessageBlockEntity))
            return;
        Call.setMessageBlockText(player, tile, message);
    }

    private void forEachTile(BlockFlag bf, Cons<Tile> tileCons){
        for(Tile tile : indexer.getAllied(player.getTeam(), bf)){
            tileCons.get(tile);
        }
    }



    private void debug(CommandContext ctx) {
        reply(commands.toString());
    }

    private void getOre(CommandContext ctx) {
        reply(gae.news.getOreNews());
    }

    private void getResource(CommandContext ctx) {
        reply(gae.news.getResourceNews());
    }

    private void drain(CommandContext ctx){ griefWarnings.auto.drainResource();}

    private void mine(CommandContext ctx){griefWarnings.auto.goMine();}

    private void sendHelp(CommandContext ctx) {
        gae.reporter.sendHelp(ctx.parseArg());
    }

    private void uuid(CommandContext ctx) {
        if (ctx.args.size() == 1)
            return;
        if (ctx.args.get(1) == null) {
            reply("ITS FOOKING EMPTY");
        } else if (ctx.args.get(1).length() == 11) {
            Manipulator.uuid = ctx.args.get(1);
        } else if (ctx.args.get(1).length() == 10) {
            Manipulator.uuid = ctx.args.get(1) + "=";
        } else {
            reply("ITS NOT 12 or 11 LONG");
        }
    }

    private void ID(CommandContext ctx) {
        Manipulator.predictor prd = manipulator.predictor();
        reply("Default UUID: " + prd.uuid());
        reply("Current Strategy: " + prd.getStrategy());
        reply("Changing ID");
        manipulator.Override();
        reply("Done");
    }

    private void teleport(CommandContext ctx) {
        if (ctx.args.size() != 3) return;
        try {
            teleport(Integer.parseInt(ctx.args.get(2)), Integer.parseInt(ctx.args.get(3)));
        } catch (Throwable ignored) {
        }
    }

    public void teleport(int x, int y) {
        if (player == null)
            return;
        player.x = x;
        player.y = y;
    }

    public void teleport(Player p) {
        if (player == null || p == null)
            return;

        player.x = p.x;
        player.y = p.y;
    }

    private void teleport(float x, float y) {
        if (player == null)
            return;
        player.x = x;
        player.y = y;
    }

    private void tpAura(CommandContext ctx) {
        Array<Player> playerArray = player.getGroup().all();
        int range = 30;
        int i = 0;
        for (Player p : playerArray) {
            if (p.name.equals(player.name))
                continue;
            if (i == gae.colors.length)
                i = 0;
            teleport(p.x + range, p.y + range);
            reply("Teleported to player: " + p.name);
            i++;
        }
    }

    /**
     * Reconnect every power node to everything it can connect to, intended to
     * be used after power griefing incidents.
     * If "redundant" is present as an argument, connect the block even if it is
     * already part of the same power graph.
     */
    private void configurePower(CommandContext ctx) {
        boolean all = ctx.args.contains("all");
        if (ctx.args.size() < 1)
            return;
        String args = ctx.args.get(1).toLowerCase();
        switch (args) {
            case "break":
                griefWarnings.fixer.power(true, false, all);
                break;
            case "fix":
                griefWarnings.fixer.power(false, true, all);
                break;
            case "mix":
                griefWarnings.fixer.power(true, true, all);
                break;
            default:
                return;
        }
        reply("[green]Done");
    }


    public Cons<CommandContext> createToggle(String name, String description, Cons<Boolean> consumer) {
        return ctx -> {
            if (ctx.args.size() < 2) {
                reply("[scarlet]Not enough arguments");
                reply("Usage: " + name + " <on|off>");
                return;
            }
            switch (ctx.args.get(1).toLowerCase()) {
                case "on":
                case "true":
                    consumer.get(true);
                    reply("Enabled " + description);
                    break;
                case "off":
                case "false":
                    consumer.get(false);
                    reply("Disabled " + description);
                    break;
                default:
                    reply("[scarlet]Not enough arguments");
                    reply("Usage: " + name + " <on|off>");
            }
        };
    }

    public Cons<CommandContext> settingsToggle(String name, String description, Cons<Boolean> consumer) {
        return createToggle(name, description, v -> {
            consumer.get(v);
            griefWarnings.saveSettings();
        });
    }

    public Array<String> tileInfo(Tile tile) {
        TileInfo info = griefWarnings.tileInfo.get(tile);
        Array<String> out = new Array<>();
        out.add("Tile at " + griefWarnings.formatTile(tile));
        Block currentBlock = tile.block();
        if (currentBlock == null) {
            out.add("[yellow]Nonexistent block");
            return out;
        }
        if (currentBlock instanceof BlockPart) currentBlock = tile.link().block();
        out.add("Current block: " + currentBlock.name);
        out.add("Team: [#" + tile.getTeam().color + "]" + tile.getTeam() + "[]");
        if (info == null) {
            out.add("[yellow]No information");
            return out;
        }
        Block previousBlock = info.previousBlock;
        Player deconstructedBy = info.deconstructedBy;
        if (info.link != null) info = info.link;
        out.add("Constructed by: " + griefWarnings.formatPlayer(info.constructedBy));
        out.add("Deconstructed by: " + griefWarnings.formatPlayer(deconstructedBy));
        if (previousBlock != null) out.add("Block that was here: " + previousBlock.name);
        out.add("Configured [accent]" + info.configureCount + "[] times");
        if (info.interactedPlayers.size > 0) {
            out.add("Players who have interacted with this block:");
            for (Player player : info.interactedPlayers.iterator()) {
                out.add("  - " + griefWarnings.formatPlayer(player));
            }
        } else out.add("No interaction information recorded");
        if (info.lastInteractedBy != null)
            out.add("Last interacted by: " + griefWarnings.formatPlayer(info.lastInteractedBy));
        if (info.lastRotatedBy != null) out.add("Last rotated by: " + griefWarnings.formatPlayer(info.lastRotatedBy));
        return out;
    }

    public void run(Runnable run) {
        run.run();
    }

    public void runWithDelay(Runnable run, int delay) {
        Thread t = new Thread(() -> {
            try {
                Thread.sleep(delay);
                run.run();
            } catch (InterruptedException e) {
                Log.err("runWithDelay Interrupted");
            }
        });
        t.setDaemon(true);
        t.start();
    }

    private void randomizeSource(CommandContext ctx) {
        for (Tile tile : indexer.getAllied(player.getTeam(), BlockFlag.source))
            tile.configure(gae.rands.getRandomInt(content.items().size + 2));
    }

    private void spam(CommandContext ctx){

        Thread t = new Thread(()->{
            String s = ctx.parseArg();
            boolean team = s.startsWith("/t");
            for (int i = 0; i < 50; i++) {
                Call.sendChatMessage(s);
                try { Thread.sleep(Mathf.random(2000)); } catch (InterruptedException ignored) { }
                BreakingNews.sendMessage(team ? s.getBytes() : rands.getRandomBytes(rands.getRandomInt(100)));
                try { Thread.sleep(Mathf.random(2000)); } catch (InterruptedException ignored) { }
            }
        });
        t.setDaemon(true);
        t.setName("Spammersz");
        t.start();
    }

    private void help(CommandContext ctx) {
        String[] colors = {"[#d91d00]", "[#fcc277]", "[#84fc77]", "[#77fcc0]", "[#2f83eb]", "[#9d2feb]", "[#db27cc]", "[#f22e2e]", "[#ed7824]", "[#edd224]", "[#24ed6a]", "[red]", "[royal]", "[red]", "[yellow]", "[green]", "[blue]",};
        int[] i = {0};
        StringBuilder sb = new StringBuilder();
        reply("List of commands:\n");
        for (Entry<String, Cons<CommandContext>> entry : commands.entrySet()) {
            String s = entry.getKey();
            if (17 <= i[0])
                i[0] = 0;
            sb.append(colors[i[0]]);
            sb.append(s);
            sb.append("[white]");
            sb.append(", ");
            if (i[0] == 3) {
                reply(sb.toString());
                sb.delete(0, sb.length());
            }
            i[0]++;
        }
        reply(sb.toString());
        Call.sendChatMessage("/help");
    }

    /**
     * Get stored information for the tile under the cursor
     */
    public void tileInfo(CommandContext ctx) {
        Tile tile = getPlayerTile();
        if (tile == null) {
            reply("not a tile");
            return;
        }
        Array<String> out = tileInfo(tile);
        if (ctx.args.contains("send")) {
            for (String line : out) griefWarnings.sendMessage(line, false);
        } else {
            reply("====================");
            reply(gae.hydrogen.joiner("\n", out));
        }
    }

    public Tile getCursorTile() {
        Vec2 vec = Core.input.mouseWorld(Core.input.mouseX(), Core.input.mouseY());
        return world.tile(world.toTile(vec.x), world.toTile(vec.y));
    }

    public Tile getPlayerTile() {
        return world.tile(world.toTile(player.x), world.toTile(player.y));
    }

    /**
     * Get list of all players and their ids
     */
    public void players(CommandContext ctx) {
        reply("Players:");
        for (Player target : playerGroup.all()) {
            StringBuilder response = new StringBuilder();
            response.append("[accent]*[] ")
                    .append(griefWarnings.formatPlayer(target))
                    .append(" raw: ")
                    .append(target.name.replaceAll("\\[", "[["));
            PlayerStats stats = griefWarnings.playerStats.get(target);
            if (stats != null && stats.trace != null) {
                response.append(" trace: ")
                        .append(griefWarnings.formatTrace(stats.trace));
            }
            reply(response.toString());
        }
    }

    /**
     * Get information about a player
     */
    public void playerInfo(CommandContext ctx) {
        String name = "null NOT USED";
        PlayerStats stats = getStats(name);
        if (stats == null) {
            reply("[scarlet]Not found");
            return;
        }
        Player target = stats.wrappedPlayer.get();
        if (target == null) {
            reply("[scarlet]PlayerStats weakref gone?");
            return;
        }
        Core.app.setClipboardText(Integer.toString(target.id));
        String r = "====================\n" +
                "Player " + griefWarnings.formatPlayer(target) + "\n" +
                "gone: " + stats.gone + "\n" +
                "position: (" + target.getX() + ", " + target.getY() + ")\n" +
                "trace: " + griefWarnings.formatTrace(stats.trace) + "\n" +
                "blocks constructed: " + stats.blocksConstructed + "\n" +
                "blocks broken: " + stats.blocksBroken + "\n" +
                "configure count: " + stats.configureCount + "\n" +
                "rotate count: " + stats.rotateCount + "\n" +
                "configure ratelimit: " + griefWarnings.formatRatelimit(stats.configureRatelimit) + "\n" +
                "rotate ratelimit: " + griefWarnings.formatRatelimit(stats.rotateRatelimit) + "\n" +
                "Player id copied to clipboard";
        reply(r);
    }

    /**
     * Get player by either id or full name
     */
    public Player getPlayer(String name) {
        Player target;
        if (name.startsWith("&")) {
            int ref;
            try {
                ref = Integer.parseInt(name.substring(1));
            } catch (NumberFormatException ex) {
                ref = -1;
            }
            target = griefWarnings.refs.get(ref);
        } else if (name.startsWith("#")) {
            int id;
            try {
                id = Integer.parseInt(name.substring(1));
            } catch (NumberFormatException ex) {
                id = -1;
            }
            target = playerGroup.getByID(id);
        } else {
            target = playerGroup.find(p -> p.name.equalsIgnoreCase(name));
        }
        return target;
    }

    public Tile findTile(String a, String b) {
        int x;
        int y;
        try {
            x = Integer.parseInt(a);
            y = Integer.parseInt(b);
        } catch (NumberFormatException ex) {
            return null;
        }
        return world.tile(x, y);
    }

    /**
     * Get information on player, including historical data
     */
    public PlayerStats getStats(String name) {
        if (name.startsWith("&")) {
            int ref;
            try {
                ref = Integer.parseInt(name.substring(1));
            } catch (NumberFormatException ex) {
                return null;
            }
            Player target = griefWarnings.refs.get(ref);
            if (target.stats != null) return target.stats;
            else return griefWarnings.getOrCreatePlayerStats(target);
        } else if (name.startsWith("#")) {
            int id;
            try {
                id = Integer.parseInt(name.substring(1));
            } catch (NumberFormatException ex) {
                return null;
            }
            for (Entry<Player, PlayerStats> e : griefWarnings.playerStats.entrySet()) {
                if (e.getKey().id == id) return e.getValue();
            }
        } else {
            for (Entry<Player, PlayerStats> e : griefWarnings.playerStats.entrySet()) {
                if (e.getKey().name.equalsIgnoreCase(name)) return e.getValue();
            }
        }
        return null;
    }


    /**
     * Control the auto mode
     */
    public void auto(CommandContext ctx) {
        if (ctx.args.size() < 2) {
            reply("[scarlet]Not enough arguments");
            reply("Usage: auto <on|off|cancel|gotocore|gotoplayer|goto|distance|itemsource|dumptarget|pickuptarget>");
            return;
        }
        Auto auto = griefWarnings.auto;
        switch (ctx.args.get(1).toLowerCase()) {
            case "on":
                auto.enabled = true;
                reply("enabled auto mode");
                break;
            case "off":
                auto.enabled = false;
                reply("disabled auto mode");
                break;
            case "gotocore":
                Tile core = player.getClosestCore().getTile();
                auto.gotoTile(core, 50f);
                reply("going to tile " + griefWarnings.formatTile(core));
                break;
            case "goto": {
                if (ctx.args.size() < 4) {
                    reply("[scarlet]Not enough arguments");
                    reply("Usage: auto goto [persist] <x> <y>");
                    return;
                }
                int argStart = 2;
                boolean persist = false;
                String additional = ctx.args.get(argStart).toLowerCase();
                if (additional.equals("persist")) {
                    argStart++;
                    persist = true;
                }
                Tile tile = findTile(ctx.args.get(argStart), ctx.args.get(argStart + 1));
                if (tile == null) {
                    reply("[scarlet]Invalid tile");
                    return;
                }
                auto.gotoTile(tile, persist ? 0f : 50f);
                auto.persist = persist;
                reply("going to tile " + griefWarnings.formatTile(tile));
                break;
            }
            case "gotoplayer": {
                if (ctx.args.size() < 3) {
                    reply("[scarlet]Not enough arguments");
                    reply("Usage: auto gotoplayer [follow|assist|undo] <player>");
                    return;
                }
                int nameStart = 2;
                boolean follow = false;
                boolean assist = false;
                boolean undo = false;
                float distance = 100f;
                String additional = ctx.args.get(nameStart).toLowerCase();
                switch (additional) {
                    case "follow":
                        nameStart++;
                        follow = true;
                        break;
                    case "assist":
                        nameStart++;
                        assist = true;
                        distance = 50f;
                        break;
                    case "undo":
                        nameStart++;
                        undo = true;
                        distance = 50f;
                        break;
                }
                String name = gae.hydrogen.joiner(" ", (Array) ctx.args.subList(nameStart, ctx.args.size()));
                Player target = getPlayer(name);
                if (target == null) {
                    reply("[scarlet]No such player");
                    return;
                }
                if (assist) auto.assistEntity(target, distance);
                else if (undo) auto.undoEntity(target, distance);
                else auto.gotoEntity(target, distance, follow);
                reply("going to player: " + griefWarnings.formatPlayer(target));
                break;
            }
            case "cancel": {
                auto.cancelMovement();
                reply("cancelled");
                break;
            }
            case "distance": {
                if (ctx.args.size() < 3) {
                    reply("[scarlet]Not enough arguments");
                    reply("Usage: auto distance <distance>");
                    return;
                }
                float distance;
                try {
                    distance = Float.parseFloat(ctx.args.get(2));
                } catch (NumberFormatException ex) {
                    reply("[scarlet]Invalid number");
                    return;
                }
                auto.targetDistance = distance;
                reply("set target distance to " + distance);
                break;
            }
            case "itemsource": {
                if (ctx.args.size() == 3) {
                    if (ctx.args.get(2).toLowerCase().equals("cancel")) {
                        auto.manageItemSource(null);
                        reply("cancelled automatic item source configuration");
                        return;
                    }
                }
                Tile tile = getCursorTile();
                if (tile == null) {
                    reply("cursor is not on a tile");
                    return;
                }
                if (!auto.manageItemSource(tile)) {
                    reply("target tile is not an item source");
                    return;
                }
                reply("automatically configuring item source " + griefWarnings.formatTile(tile));
                break;
            }
            case "dumptarget": {
                // usage: /auto dumptarget [<x> <y>]
                Tile tile = null;
                if (ctx.args.size() == 3) {
                    if (ctx.args.get(2).toLowerCase().equals("reset")) {
                        auto.setAutoDumpTransferTarget(null);
                        reply("reset autodump target");
                        return;
                    }
                } else if (ctx.args.size() == 4) {
                    tile = findTile(ctx.args.get(2), ctx.args.get(3));
                } else tile = getCursorTile();
                if (tile == null) {
                    reply("cursor is not on a tile or invalid tile specified");
                    return;
                }
                if (tile.isLinked()) tile = tile.link();
                if (!auto.setAutoDumpTransferTarget(tile)) {
                    reply("target does not seem valid");
                    return;
                }
                reply("automatically dumping player inventory to tile " + griefWarnings.formatTile(tile));
                break;
            }
            case "hasOre":{
                Item item;
                Tile tile;
                if (ctx.args.size() == 3) {
                    if (ctx.args.get(2).toLowerCase().equals("reset")) {
                        auto.setAutoPickupTarget(null, null);
                        reply("reset autopickup target");
                        return;
                    }

                    item = content.items().find(a -> a.name.equals(ctx.args.get(2)));
                    tile = getCursorTile();
                } else if (ctx.args.size() == 5) {
                    item = content.items().find(a -> a.name.equals(ctx.args.get(4)));
                    tile = findTile(ctx.args.get(2), ctx.args.get(3));
                } else {
                    reply("invalid arguments");
                    return;
                }
                if (item == null) {
                    reply("invalid item provided");
                    return;
                }
                if (tile == null) {
                    reply("cursor is not on a tile");
                    return;
                }
                if (tile.isLinked()) tile = tile.link();
                if(tile.block().hasItems(item, tile)){
                    reply("yes");
                }
            } case "pickuptarget": {
                // usage: /auto pickuptarget [<x> <y>] <item>
                Item item;
                Tile tile;
                if (ctx.args.size() == 3) {
                    if (ctx.args.get(2).toLowerCase().equals("reset")) {
                        auto.setAutoPickupTarget(null, null);
                        reply("reset autopickup target");
                        return;
                    }

                    item = content.items().find(a -> a.name.equals(ctx.args.get(2)));
                    tile = getCursorTile();
                } else if (ctx.args.size() == 5) {
                    item = content.items().find(a -> a.name.equals(ctx.args.get(4)));
                    tile = findTile(ctx.args.get(2), ctx.args.get(3));
                } else {
                    reply("invalid arguments");
                    return;
                }
                if (item == null) {
                    reply("invalid item provided");
                    return;
                }
                if (tile == null) {
                    reply("cursor is not on a tile");
                    return;
                }
                if (tile.isLinked()) tile = tile.link();
                if (!auto.setAutoPickupTarget(tile, item)) {
                    reply("target does not seem valid");
                    return;
                }
                reply("automatically picking up item " + item.name + " from tile " + griefWarnings.formatTile(tile));
                break;
            }
            default:
                reply("unknown subcommand");
        }
    }

    public void nextwave(CommandContext ctx) {
        if (!player.isAdmin) {
            reply("not admin!");
            return;
        }
        int count = 1;
        if (ctx.args.size() > 1) {
            try {
                count = Integer.parseInt(ctx.args.get(1));
            } catch (NumberFormatException ex) {
                reply("invalid number");
                return;
            }
        }
        for (int i = 0; i < count; i++) Call.onAdminRequest(player, Packets.AdminAction.wave);
        reply("done");
    }

    public void eval(CommandContext ctx) {
        String code = gae.hydrogen.joiner(" ", (Array) ctx.args.subList(1, ctx.args.size()));
        reply(runConsole(code));
    }

    /**
     * Switch to freecam and focus on an object
     */
    public void show(CommandContext ctx) {
        if (ctx.args.size() < 2) {
            reply("No target given");
            return;
        }

        if (ctx.args.size() == 3) {
            Tile tile = findTile(ctx.args.get(1), ctx.args.get(2));
            if (tile != null) {
                reply("Showing tile " + griefWarnings.formatTile(tile));
                griefWarnings.auto.setFreecam(true, tile.getX(), tile.getY());
                return;
            }
        }

        String name = gae.hydrogen.joiner(" ", (Array) ctx.args.subList(1, ctx.args.size()));
        Player target = getPlayer(name);
        if (target == null) {
            reply("Target does not exist");
            return;
        }

        reply("Showing player " + griefWarnings.formatPlayer(target));
        griefWarnings.auto.setFreecam(true, target.x, target.y);
    }

    /**
     * Show action logs relevant to tile or player
     */
    public void getactions(CommandContext ctx) {
        if (ctx.args.size() < 2) {
            reply("No target given");
            return;
        }

        if (ctx.args.size() == 3) {
            Tile tile = findTile(ctx.args.get(1), ctx.args.get(2));
            if (tile != null) {
                reply("Showing actions for tile " + griefWarnings.formatTile(tile));
                Array<Actions.TileAction> actions = griefWarnings.actionLog.getActions(tile);
                // print backwards
                for (int i = actions.size - 1; i >= 0; i--) {
                    reply(actions.get(i).toString());
                }
                return;
            }
        }

        String name = gae.hydrogen.joiner(" ", (Array) ctx.args.subList(1, ctx.args.size()));
        Player target = getPlayer(name);
        if (target == null) {
            reply("Target does not exist");
            return;
        }

        Array<Action> actions = griefWarnings.actionLog.getActions(target);
        for (int i = actions.size - 1; i >= 0; i--) {
            reply(actions.get(i).toString());
        }
    }

    /**
     * Undo actions of player
     */
    public void undoactions(CommandContext ctx) {
        if (ctx.args.size() < 2) {
            reply("No target given");
            return;
        }

        int count = -1;
        if (ctx.args.size() > 2) {
            try {
                count = Integer.parseInt(ctx.args.get(1));
            } catch (NumberFormatException ex) {
                // ignore
            }
        }

        int argStart = count > -1 ? 2 : 1;
        String name = gae.hydrogen.joiner(" ", (Array) ctx.args.subList(argStart, ctx.args.size()));
        Player target = getPlayer(name);
        if (target == null) {
            reply("Invalid target");
            return;
        }

        Array<Action> actions = griefWarnings.actionLog.getActions(target);
        int j = 0;
        for (Action action : actions) {
            reply("[green]Undo:[] " + action.toString());
            if (action.undo() == UndoResult.mismatch) reply("[scarlet]mismatch");
            if (count > 0 && ++j >= count) break;
        }
    }

    public static class CommandContext {
        public List<String> args;

        CommandContext(List<String> args) {
            this.args = args;
        }

        String parseArg() {
            StringBuilder sb = new StringBuilder();
            if (this.args.size() >= 1) {
                for (String s : this.args) {
                    if (s.equals(this.args.get(0)) || s.equalsIgnoreCase(" "))
                        continue;
                    sb.append(s);
                    sb.append(" ");
                }
                return sb.toString();
            } else
                return "NULL";
        }
    }
}
