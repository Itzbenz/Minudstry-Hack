package Photon.Information;

import Photon.gae;
import arc.Events;
import arc.struct.Array;
import mindustry.content.Items;
import mindustry.entities.type.TileEntity;
import mindustry.game.EventType;
import mindustry.type.Item;
import mindustry.type.ItemType;
import mindustry.world.Tile;
import mindustry.world.meta.BlockFlag;
import mindustry.world.modules.ItemModule;

import java.nio.ByteBuffer;

import static mindustry.Vars.*;


public class BreakingNews {
    private static final ByteBuffer TEMP_BUFFER = ByteBuffer.allocate(4096);
    public static final String[] AllOre = {"Copper", "Lead", "Coal", "Sand", "Scrap", "Titanium", "Thorium"};
    public static final Array<Item> allMinable = new Array<>();
    private final int[] AllOreIcon = {544, 543, 539, 540, 536, 538, 537};
    private final String[] Color = {"[#d91d00]", "[#fcc277]", "[#84fc77]", "[#77fcc0]", "[#2f83eb]", "[#9d2feb]", "[#db27cc]", "[#f22e2e]", "[#ed7824]", "[#edd224]", "[#24ed6a]", "[red]", "[royal]", "[red]", "[yellow]", "[green]", "[blue]",};
    private final int OreLength = AllOre.length;
    private TileEntity core;
    private ItemModule items;
    private String ExistingOre;

    public BreakingNews(){
       Events.on(EventType.LateInit.class, ()->{
           allMinable.add(Items.copper, Items.lead, Items.scrap, Items.thorium);
           allMinable.add(Items.titanium);
       });
        Events.on(EventType.WorldLoadEvent.class, event -> {
            if(player == null)
                return;
            core = player.getClosestCore();
        if(core != null)
            if(core.items != null)
                items = core.items;
            ExistingOre = indexer.allOres.toString();
        });
        Events.on(EventType.PlayerJoin.class, event ->{
            if(event.player.isAdmin)
                griefWarnings.commandHandler.reply("There is admin cyka \n Better you quick or be dead: " + player.name);
            if(gae.reporter.isPlayerReported(event.player))
                griefWarnings.commandHandler.reply("Reported player detected: " + player.name);
        });
    }

    public static void sendMessage(byte[] msg){
        if(mindustry.Vars.net.client()) {
            mindustry.net.Packets.InvokePacket packet = arc.util.pooling.Pools.obtain(mindustry.net.Packets.InvokePacket.class, mindustry.net.Packets.InvokePacket::new);
            packet.writeBuffer = TEMP_BUFFER;
            packet.priority = (byte)0;
            packet.type = (byte)52;
            TEMP_BUFFER.position(0);
            mindustry.io.TypeIO.writeBytes(TEMP_BUFFER, msg);
            packet.writeLength = TEMP_BUFFER.position();
            mindustry.Vars.net.send(packet, mindustry.net.Net.SendMode.tcp);
        }
    }

    public static Array<Tile> indexTile(BlockFlag bf){
        Array<Tile> tiles = new Array<>();
        for (Tile tile : indexer.getAllied(player.getTeam(), bf)){
            tiles.add(tile);
        }
        return tiles;
    }

    public void getSettingsNews(){

    }

    public String getResourceNews() {
        int f = 0;
        int icon = 63544;
        int count = Integer.MAX_VALUE;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < content.items().size; i++) {
            Item currentItem = content.item(i);
            if (currentItem.type != ItemType.material) continue;
            int currentCount = items.get(currentItem);
            if (currentCount < count) {
                if (currentCount > 0) f = 0;
                if (currentCount > 100) f = 7;
                if (currentCount > 500) f = 6;
                if (currentCount > 1000) f = 9;
                if (currentCount > 5000) f = 1;
                if (currentCount > 10000) f = 4;
                if (currentCount > 20000) f = 3;
            }
            if (currentCount < count) {
                sb.append("\n[white]");
                sb.append(gae.obfuscate.AsciiToString(icon));
                sb.append(" = ");
                sb.append(Color[f]);
                sb.append(currentCount);
                icon--;
            }
        }
        return new String(sb);
    }

    public Boolean isOreExists(Item n) { return indexer.allOres.contains(n); }

    public String getOreNews() {
        //TODO Rewrite
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < OreLength; i++) {
            boolean exists = (ExistingOre.contains(AllOre[i]));
            sb.append("\n[white]").append(gae.obfuscate.AsciiToString("63" + AllOreIcon[i])).append("[white] = [red]").append((exists) ? (Color[2] + "Exists") : (Color[0] + "Not Exists"));
        }
        return new String(sb);
    }

}
