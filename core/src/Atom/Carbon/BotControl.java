package Atom.Carbon;

import Photon.gae;
import arc.ApplicationListener;
import arc.Core;
import arc.Events;
import arc.struct.Array;
import arc.util.Log;
import mindustry.core.GameState;
import mindustry.entities.type.Player;
import mindustry.game.EventType;
import mindustry.gen.Call;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static Atom.Manifest.tags;
import static arc.util.Log.format;
import static mindustry.Vars.*;

public class BotControl implements ApplicationListener {
    private BufferedReader br;
    public BotControl(Array<String> args){
        createPlayer();
        Log.setLogger((level, text) -> {
            String result = "[" + "o7" + "] " + format(tags[level.ordinal()] + " " + text + "&fr");
            System.out.println(result);
        });
        Events.fire(EventType.LateInit.class);
        Events.on(EventType.PlayerChatEvent.class, c-> System.out.println(c.player.name + ": " + c.message));
        Events.on(EventType.PlayerConnect.class, p -> System.out.println("Player Joined: " + p.player.name));
        Events.on(EventType.PlayerLeave.class, l -> System.out.println("Player Leave: " + l.player.name));
        while (true)
        try {
            br = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("Enter Code: ");
            String b = br.readLine();
            if(gae.commandCenter.handleConsole(b))
                continue;
            else if(state.is(GameState.State.playing)) {
                Call.sendChatMessage(b);
                System.out.println(player.name + ": " + b);
            }else
                Log.err("Invalid Commands, use /help");
            } catch(NumberFormatException | IOException nfe) {
                System.err.println("Invalid Format!");
        }
    }

    void createPlayer() {
        player = new Player();
        player.name = Core.settings.getString("name");
        player.color.set(Core.settings.getInt("color-0"));
        player.isLocal = true;
        player.isMobile = mobile;

        if (!state.is(GameState.State.menu)) {
            player.add();
        }
    }
}
