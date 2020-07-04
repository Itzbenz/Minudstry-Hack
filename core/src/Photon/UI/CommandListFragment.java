package Photon.UI;

import Photon.Information.CommandsList;
import Photon.Information.Manipulator;
import Photon.gae;
import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.scene.Group;
import arc.scene.event.Touchable;
import arc.scene.ui.SettingsDialog;
import arc.scene.ui.TextField;
import arc.scene.ui.layout.Scl;
import arc.scene.ui.layout.Table;
import arc.util.Interval;
import arc.util.Log;
import mindustry.core.GameState.State;
import mindustry.gen.Call;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.ui.Styles;
import mindustry.ui.fragments.Fragment;

import static mindustry.Vars.*;

public class CommandListFragment extends Fragment {
    private final Table logs = new Table().marginRight(30f).marginLeft(20f);
    private final Table content = new Table().marginRight(30f).marginLeft(20f);
    private final SettingsDialog.SettingsTable settings = new SettingsDialog.SettingsTable();
    private final Interval timer = new Interval();
    public boolean limit;
    public int limiterInterval;
    private int interval;
    private boolean runOnce;
    private float h = 70F;
    public boolean visible = false;
    private boolean settingsMenu = false;
    public boolean logsVisible = false;
    private TextField sField;
    private String commandants = "NULL";

    public CommandListFragment(){
        if(Manipulator.change)
            gae.manipulator.Override();
       runOnce = Core.settings.getBool("commands.runOnce", false);
       interval = Core.settings.getInt("commands.interval", 20);
    }

    @Override
    public void build(Group parent) {
        parent.fill(cont -> {
            settings();
            cont.visible(() -> visible);
            cont.update(() -> {
                if (!(net.active() && !state.is(State.menu))) {
                    visible = false;
                    return;
                }
                if (visible && timer.get(interval) ) {// 20 a.k.a 2 second
                    content.pack();
                    content.act(Core.graphics.getDeltaTime());
                    Core.scene.act(0f);
                    if (sField.getText().length() > 0 || commandants.length() > 0) CommandsBar();
                }
            });

            cont.table(Tex.buttonTrans, setting ->{
                setting.visible(() -> settingsMenu);
                setting.pane(settings);
            });

            cont.table(Tex.buttonTrans, pane -> {
                pane.labelWrap(griefWarnings.commandHandler.commands.size() + " Commands in total").marginLeft(20);
                pane.row();
                sField = pane.addField(null, text -> rebuild()).grow().pad(8).get();
                sField.setMaxLength(maxNameLength);
                sField.setMessageText("Enter Custom Commands");
                pane.row();
                pane.pane(content).grow().get().setScrollingDisabled(true, false);
                pane.row();
                pane.table(menu -> {
                    menu.defaults().growX().height(50f).fillY();
                    menu.addImageButton(Icon.settings, this::settingsToggle);
                    menu.addImageButton(Icon.cancel, () -> commandants = "NULL");
                    menu.addButton("[red]Close", this::toggle);
                }).margin(0f).pad(15f).growX();

                cont.table(Tex.buttonTrans, log -> {
                    log.visible(() -> logsVisible);
                    log.pane(logs);
                });

            }).touchable(Touchable.enabled).margin(14f);
        });
        rebuild();
    }

    public void rebuild() {
        content.clear();
        int i = 0;
        for (CommandsList cl : gae.commandsLists) {
            if(i == gae.colors.length)
                i = 0;
            String name = cl.commandsName.replace("-", " ");
            Table button = new Table();
            button.left();
            button.margin(5).marginBottom(10);

            Table table = new Table() {
                @Override
                public void draw() {
                    super.draw();
                    Draw.color(Color.valueOf("#5e5f67"));
                    Draw.alpha(parentAlpha);
                    Lines.stroke(Scl.scl(3f));
                    Lines.rect(x, y, width, height);
                    Draw.reset();
                }
            };
            table.add(gae.obfuscate.AsciiToString(cl.commandsIconASCII)).fontScale(3.8f).center().grow();
            button.add(table).size(h);
            button.labelWrap(gae.colors[i] + name).width(170f).pad(10);
            try {
                button.row();
                button.addImageButton(Icon.info, Styles.clearPartiali, () -> ui.showInfo(cl.commandsDescription));
                if(gae.hydrogen.Declared(cl.commandsRunConfirmationMessage))
                    button.addImageButton(Icon.settings, Styles.clearPartiali, () -> ui.showConfirm(name,"Are you sure want to " + cl.commandsRunConfirmationMessage.toLowerCase(), () -> commandants = gae.hydrogen.commandsFormat(cl.commandsName)));
                else
                    button.addImageButton(Icon.settings, Styles.clearPartiali, () -> ui.showInfo("Use the book icon in the right you cunt"));
                int l = 0;
                if(gae.hydrogen.Declared(cl.childCommandsList))
                        for(CommandsList.ChildCommandsList ccl : cl.childCommandsList){
                            if(l == 3) {
                                button.row();
                                button.add();
                                button.add();
                                l = 0;
                            }
                            l++;
                            button.addImageButton(Icon.book, Styles.clearPartiali, () -> ui.showConfirm(name, ccl.childCommandsRunPrompt, () -> commandants = ccl.childCommandsName)); }

            }catch (Throwable a){ Log.err(a + " At" +  name) ; }
            content.add(button).padBottom(-6).width(350f).maxHeight(h + 14);
            content.row();
            content.addImage().height(4f).color(Pal.engine).growX();
            content.row();
            i++;
        }


        content.marginBottom(5);
    }

    private void settings(){
        settings.sliderPref("commands.interval", "Interval", 20, 1, 30, 3, s ->{
            interval = s;
            return interval*100 + " millisecond";
        });
        settings.sliderPref("commands.limit.interval", "Configuration Interval", 10, 1, 30, 1, s ->{
            limiterInterval = s;
            return "Max action: " + limiterInterval ;
        });
        settings.checkPref("commands.runOnce", "Run once", runOnce);
        settings.checkPref("commands.limit", "Limit action", limit);

    }

    private void settingsToggle(){
        settingsMenu = !settingsMenu;
        settings.visible(settingsMenu);

    }

    public void toggle() {
        visible = !visible;
        if (visible) {
            rebuild();
        } else {
            settingsMenu = false;
            logsVisible = false;
            logs.clear();
            gae.commandsNews.reset();
            sField.setMessageText("");
            Core.scene.setKeyboardFocus(null);
            commandants = "NULL";
        }
        gae.commandsNews.setShowed(visible);
        griefWarnings.tileInfoHud = !visible;
    }

    private void CommandsBar() {
        if(!gae.hydrogen.Declared(sField.getText()) && !gae.hydrogen.Declared(commandants))
            return;
        if(sField.getText().startsWith("/")) {
            Call.sendChatMessage("/" + sField.getText());
        }else if(griefWarnings.commandHandler.runCommand("/" + sField.getText())) {
            Log.debug("shut up intellj");
        }else if(griefWarnings.commandHandler.runCommand("/" + commandants)) {
            Log.debug("shut up intellj");
        }else {
            logsVisible = false;
            return;
        }
        logs.clear();
        logsVisible = true;
        if (gae.hydrogen.Declared(gae.commandsNews.commandName)) {
            String message = "[accent]Command [white]= [royal]/" + gae.commandsNews.commandName;
            String status = ("[white]Status = " + ((gae.commandsNews.isExecutable) ? "[green]Executed" : "[red]Command Not Found"));
            logs.add(message).padBottom(6).width(350f).maxHeight(h + 14);//Add text at bottom
            logs.row();//Add row separator
            logs.add(status).padBottom(6).width(350f).maxHeight(h + 14);//Add text at bottom
            logs.row();//Add row separator'
        }
        if (gae.hydrogen.Declared(gae.commandsNews.logMessage)) {
            logs.add("Log: " + gae.commandsNews.logMessage).padBottom(6).width(350f).maxHeight(h + 14);//Add text at bottom
            logs.row();//Add row separator
        }
        if (gae.hydrogen.Declared(gae.commandsNews.timesExecuted)) {
            logs.add("[royal]Times Executed:[accent] " + gae.commandsNews.timesExecuted).padBottom(6).width(350f).maxHeight(h + 14);//Add text at bottom
        }

    }

    private void showJavaScriptEditor(){
        visible = false;

    }
}
