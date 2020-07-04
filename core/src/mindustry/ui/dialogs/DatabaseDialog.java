package mindustry.ui.dialogs;

import arc.Core;
import arc.graphics.Color;
import arc.input.KeyCode;
import arc.scene.event.ClickListener;
import arc.scene.event.HandCursorListener;
import arc.scene.ui.Image;
import arc.scene.ui.ScrollPane;
import arc.scene.ui.Tooltip;
import arc.scene.ui.layout.Table;
import arc.struct.Array;
import arc.util.Time;
import mindustry.Vars;
import mindustry.core.GameState.State;
import mindustry.ctype.Content;
import mindustry.ctype.ContentType;
import mindustry.ctype.UnlockableContent;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.ui.Cicon;
import mindustry.ui.Fonts;

import static mindustry.Vars.ui;

public class DatabaseDialog extends FloatingDialog{

    public DatabaseDialog(){
        super("$database");

        shouldPause = true;
        addCloseButton();
        shown(this::rebuild);
        onResize(this::rebuild);
    }

    void rebuild(){
        cont.clear();

        Table table = new Table();
        table.margin(20);
        ScrollPane pane = new ScrollPane(table);

        Array<Content>[] allContent = Vars.content.getContentMap();

        for(int j = 0; j < allContent.length; j++){
            ContentType type = ContentType.values()[j];

            Array<Content> array = allContent[j].select(c -> c instanceof UnlockableContent && !((UnlockableContent)c).isHidden());
            if(array.size == 0) continue;

            table.add("$content." + type.name() + ".name").growX().left().color(Pal.accent);
            table.row();
            table.addImage().growX().pad(5).padLeft(0).padRight(0).height(3).color(Pal.accent);
            table.row();
            table.table(list -> {
                list.left();

                int maxWidth = Core.graphics.isPortrait() ? 7 : 13;

                int count = 0;

                for(int i = 0; i < array.size; i++){
                    UnlockableContent unlock = (UnlockableContent)array.get(i);

                    Image image = unlocked(unlock) ? new Image(unlock.icon(Cicon.medium)) : new Image(Icon.lockOpen, Pal.gray);
                    list.add(image).size(8*4).pad(3);
                    ClickListener listener = new ClickListener();
                    image.addListener(listener);
                    if(!Vars.mobile && unlocked(unlock)){
                        image.addListener(new HandCursorListener());
                        image.update(() -> image.getColor().lerp(!listener.isOver() ? Color.lightGray : Color.white, 0.4f * Time.delta()));
                    }

                    if(unlocked(unlock)){
                        image.clicked(() -> {
                            if(Core.input.keyDown(KeyCode.SHIFT_LEFT) && Fonts.getUnicode(unlock.name) != 0){
                                Core.app.setClipboardText((char)Fonts.getUnicode(unlock.name) + "");
                                ui.showInfoFade("$copied");
                            }else{
                                Vars.ui.content.show(unlock);
                            }
                        });
                        image.addListener(new Tooltip(t -> t.background(Tex.button).add(unlock.localizedName)));
                    }

                    if((++count) % maxWidth == 0){
                        list.row();
                    }
                }
            }).growX().left().padBottom(10);
            table.row();
        }

        cont.add(pane);
    }

    boolean unlocked(UnlockableContent content){
        return (!Vars.world.isZone() && !Vars.state.is(State.menu)) || content.unlocked();
    }
}
