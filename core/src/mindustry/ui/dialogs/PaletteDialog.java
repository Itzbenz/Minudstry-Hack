package mindustry.ui.dialogs;

import arc.func.Cons;
import arc.graphics.Color;
import arc.input.KeyCode;
import arc.scene.ui.Dialog;
import arc.scene.ui.ImageButton;
import arc.scene.ui.layout.Table;
import mindustry.gen.Tex;
import mindustry.ui.Styles;

import static mindustry.Vars.player;
import static mindustry.Vars.playerColors;

public class PaletteDialog extends Dialog{
    private Cons<Color> cons;

    public PaletteDialog(){
        super("");
        build();
    }

    private void build(){
        Table table = new Table();
        cont.add(table);

        for(int i = 0; i < playerColors.length; i++){
            Color color = playerColors[i];

            ImageButton button = table.addImageButton(Tex.whiteui, Styles.clearTogglei, 34, () -> {
                cons.get(color);
                hide();
            }).size(48).get();
            button.setChecked(player.color.equals(color));
            button.getStyle().imageUpColor = color;

            if(i % 4 == 3){
                table.row();
            }
        }

        keyDown(key -> {
            if(key == KeyCode.ESCAPE || key == KeyCode.BACK)
                hide();
        });

    }

    public void show(Cons<Color> cons){
        this.cons = cons;
        show();
    }
}
