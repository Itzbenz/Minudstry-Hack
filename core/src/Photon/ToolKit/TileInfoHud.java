package Photon.ToolKit;

import Photon.gae;
import arc.scene.event.Touchable;
import arc.scene.ui.layout.Table;
import mindustry.gen.Tex;
import mindustry.world.Tile;

import static mindustry.Vars.griefWarnings;

public class TileInfoHud extends Table {
    private Tile lastTile = null;
    private String lastOutput = "No data";

    public TileInfoHud() {
        touchable(Touchable.disabled);
        background(Tex.pane);
        label(this::hudInfo);
    }

    public String hudInfo() {
        Tile tile = griefWarnings.commandHandler.getCursorTile();
        if (tile == null) return lastOutput = "No data";
        if (tile == lastTile) return lastOutput;
        return lastOutput = gae.hydrogen.joiner("\n", griefWarnings.commandHandler.tileInfo(tile));
    }
}
