package mindustry.editor;

import Photon.gae;
import arc.func.Intc2;
import arc.scene.ui.layout.Table;
import mindustry.Vars;
import mindustry.ui.dialogs.FloatingDialog;

public class MapResizeDialog extends FloatingDialog{
    int width, height;

    public MapResizeDialog(MapEditor editor, Intc2 cons){
        super("$editor.resizemap");
        shown(() -> {
            cont.clear();
            width = editor.width();
            height = editor.height();

            Table table = new Table();
            table.add("Width: ");
            table.addField("100", w ->{
                if(gae.hydrogen.ContainIntOnly(w))
                    width = Integer.parseInt(w);
                else
                    Vars.ui.showInfo("Not A Number");
            });
            table.row();
            table.add("Height: ");
            table.addField("50", w ->{
                if(gae.hydrogen.ContainIntOnly(w))
                    height = Integer.parseInt(w);
                else
                    Vars.ui.showInfo("Not A Number");
            });
            cont.row();
            cont.add(table);

        });

        buttons.defaults().size(200f, 50f);
        buttons.addButton("$cancel", this::hide);
        buttons.addButton("$ok", () -> {
            cons.get(width, height);
            hide();
        });
    }

}
