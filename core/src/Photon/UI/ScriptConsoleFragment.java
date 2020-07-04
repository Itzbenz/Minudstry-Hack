package Photon.UI;

import Atom.Nitrogen.RuntimeClass;
import Photon.gae;
import arc.Core;
import arc.Input.TextInput;
import arc.graphics.Color;
import arc.graphics.g2d.BitmapFont;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.GlyphLayout;
import arc.math.Mathf;
import arc.scene.Group;
import arc.scene.ui.Label;
import arc.scene.ui.Label.LabelStyle;
import arc.scene.ui.TextField;
import arc.scene.ui.layout.Scl;
import arc.scene.ui.layout.Table;
import arc.struct.Array;
import arc.util.Align;
import mindustry.Vars;
import mindustry.input.Binding;
import mindustry.ui.Fonts;
import mindustry.ui.fragments.Fragment;

import static arc.Core.*;
import static mindustry.Vars.*;

public class ScriptConsoleFragment extends Table{
    private final static int messagesShown = 30;
    public Array<String> messages = new Array<>();
    private boolean open = false, shown;
    private TextField chatfield;
    private Label fieldlabel = new Label(">");
    private BitmapFont font;
    private GlyphLayout layout = new GlyphLayout();
    private float offsetx = Scl.scl(4), offsety = Scl.scl(4), fontoffsetx = Scl.scl(2), chatspace = Scl.scl(50);
    private Color shadowColor = new Color(0, 0, 0, 0.4f);
    private float textspacing = Scl.scl(10);
    private Array<String> history = new Array<>();
    private int historyPos = 0;
    private int scrollPos = 0;
    private Fragment container = new Fragment(){
        @Override
        public void build(Group parent){
            scene.add(ScriptConsoleFragment.this);
        }
    };

    public ScriptConsoleFragment(){

        setFillParent(true);
        font = Fonts.def;

        visible(() -> {
            if(input.keyTap(Binding.console)){
                shown = !shown;
                if(shown && !open && enableConsole){
                    toggle();
                }
                clearChatInput();
            }

            return shown;
        });

        update(() -> {
            if(input.keyTap(Binding.chat) && enableConsole && (scene.getKeyboardFocus() == chatfield || scene.getKeyboardFocus() == null)){
                toggle();
            }

            if(open){
                if(input.keyTap(Binding.chat_history_prev) && historyPos < history.size - 1){
                    if(historyPos == 0) history.set(0, chatfield.getText());
                    historyPos++;
                    updateChat();
                }
                if(input.keyTap(Binding.chat_history_next) && historyPos > 0){
                    historyPos--;
                    updateChat();
                }
            }

            scrollPos = (int)Mathf.clamp(scrollPos + input.axis(Binding.chat_scroll), 0, Math.max(0, messages.size - messagesShown));
        });

        history.insert(0, "");
        setup();
    }

    public Fragment container(){
        return container;
    }

    public void clearMessages(){
        messages.clear();
        history.clear();
        history.insert(0, "");
    }

    private void setup(){
        fieldlabel.setStyle(new LabelStyle(fieldlabel.getStyle()));
        fieldlabel.getStyle().font = font;
        fieldlabel.setStyle(fieldlabel.getStyle());

        chatfield = new TextField("", new TextField.TextFieldStyle(scene.getStyle(TextField.TextFieldStyle.class)));
        chatfield.setMaxLength(Vars.maxTextLength);
        chatfield.getStyle().background = null;
        chatfield.getStyle().font = Fonts.chat;
        chatfield.getStyle().fontColor = Color.white;
        chatfield.setStyle(chatfield.getStyle());

        bottom().left().marginBottom(offsety).marginLeft(offsetx * 2).add(fieldlabel).padBottom(6f);

        add(chatfield).padBottom(offsety).padLeft(offsetx).growX().padRight(offsetx).height(28);
    }

    @Override
    public void draw(){
        float opacity = 1f;
        float textWidth = graphics.getWidth() - offsetx*2f;

        Draw.color(shadowColor);

        if(open){
            Fill.crect(offsetx, chatfield.getY(), chatfield.getWidth() + 15f, chatfield.getHeight() - 1);
        }

        super.draw();

        float spacing = chatspace;

        chatfield.visible(open);
        fieldlabel.visible(open);

        Draw.color(shadowColor);
        Draw.alpha(shadowColor.a * opacity);

        float theight = offsety + spacing + getMarginBottom();
        for(int i = scrollPos; i < messages.size && i < messagesShown + scrollPos; i++){

            layout.setText(font, messages.get(i), Color.white, textWidth, Align.bottomLeft, true);
            theight += layout.height + textspacing;
            if(i - scrollPos == 0) theight -= textspacing + 1;

            font.getCache().clear();
            font.getCache().addText(messages.get(i), fontoffsetx + offsetx, offsety + theight, textWidth, Align.bottomLeft, true);

            if(!open){
                font.getCache().setAlphas(opacity);
                Draw.color(0, 0, 0, shadowColor.a * opacity);
            }else{
                font.getCache().setAlphas(opacity);
            }

            Fill.crect(offsetx, theight - layout.height - 2, textWidth + Scl.scl(4f), layout.height + textspacing);
            Draw.color(shadowColor);
            Draw.alpha(opacity * shadowColor.a);

            font.getCache().draw();
        }

        Draw.color();
    }

    public void addReply(String message){
        if(message == null)return;
        if(message.replaceAll(" ", "").isEmpty()) return;
        history.insert(1, message);
        addMessage("[lightgray]> " + message.replace("[", "[["));
        scene.setKeyboardFocus(chatfield);
    }

    private void sendMessage(){
        String message = chatfield.getText();
        clearChatInput();

        if(message.replaceAll(" ", "").isEmpty()) return;

        history.insert(1, message);

        addMessage("[lightgray]> " + message.replace("[", "[["));
        if(gae.commandCenter.handleConsole(message)) return;
        RuntimeClass.runLine(message);
    }

    public void toggle(){

        if(!open){
            scene.setKeyboardFocus(chatfield);
            open = !open;
            if(mobile){
                TextInput input = new TextInput();
                input.maxLength = maxTextLength;
                input.accepted = text -> {
                    chatfield.setText(text);
                    sendMessage();
                    hide();
                    Core.input.setOnscreenKeyboardVisible(false);
                };
                input.canceled = this::hide;
                Core.input.getTextInput(input);
            }else{
                chatfield.fireClick();
            }
        }else{
            scene.setKeyboardFocus(null);
            open = !open;
            scrollPos = 0;
            sendMessage();
        }
    }

    public void hide(){
        scene.setKeyboardFocus(null);
        open = false;
        clearChatInput();
    }

    public void updateChat(){
        chatfield.setText(history.get(historyPos));
        chatfield.setCursorPosition(chatfield.getText().length());
    }

    public void clearChatInput(){
        historyPos = 0;
        history.set(0, "");
        chatfield.setText("");
    }

    public boolean open(){
        return open;
    }

    public void addMessage(String message){
        messages.insert(0, message);
    }
}
