package mindustry.ui.dialogs;

import arc.input.KeyCode;
import arc.scene.ui.Image;
import arc.scene.ui.KeybindDialog;
import arc.util.Align;
import mindustry.gen.Icon;
import mindustry.graphics.Pal;

public class ControlsDialog extends KeybindDialog{

    public ControlsDialog(){
        setFillParent(true);
        title.setAlignment(Align.center);
        titleTable.row();
        titleTable.add(new Image()).growX().height(3f).pad(4f).get().setColor(Pal.accent);
    }

    @Override
    public void addCloseButton(){
        buttons.addImageTextButton("$back", Icon.left, this::hide).size(230f, 64f);

        keyDown(key -> {
            if(key == KeyCode.ESCAPE || key == KeyCode.BACK)
                hide();
        });
    }
}
