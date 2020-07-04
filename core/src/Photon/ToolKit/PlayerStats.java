package Photon.ToolKit;

import Atom.Beryllium.RateLimiter;
import arc.func.Cons;
import arc.struct.Array;
import mindustry.entities.type.Player;
import mindustry.gen.Call;
import mindustry.net.Administration.TraceInfo;
import mindustry.net.Packets.AdminAction;

import java.lang.ref.WeakReference;

import static mindustry.Vars.player;

public class PlayerStats {
    private final Array<Cons<TraceInfo>> traceListeners = new Array<>();
    // don't prevent garbage collection
    public WeakReference<Player> wrappedPlayer;
    public RateLimiter rotateRatelimit = new RateLimiter(50, 1000);
    public RateLimiter configureRatelimit = new RateLimiter(50, 1000);
    public boolean autoTraceRequested = false;
    public TraceInfo trace;
    public boolean gone = false;
    public int configureCount = 0;
    public int rotateCount = 0;
    public int blocksConstructed = 0;
    public int blocksBroken = 0;

    public PlayerStats(Player player) {
        this.wrappedPlayer = new WeakReference<>(player);
    }

    public boolean doTrace(Cons<TraceInfo> callback) {
        Player target = wrappedPlayer.get();
        if (target != null && player.isAdmin) {
            autoTraceRequested = true;
            if (callback != null) traceListeners.add(callback);
            Call.onAdminRequest(target, AdminAction.trace);
            return true;
        }
        return false;
    }

    public void handleTrace(TraceInfo info) {
        trace = info;
        autoTraceRequested = false;
        traceListeners.forEach(r -> r.get(info));
    }
}
