package mindustry.ui.fragments;

import Atom.Manifest;
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
import arc.util.Time;
import mindustry.Vars;
import mindustry.gen.Call;
import mindustry.input.Binding;
import mindustry.ui.Fonts;

import java.util.HashMap;

import static arc.Core.input;
import static arc.Core.scene;
import static mindustry.Vars.*;

public class ChatFragment extends Table{
    private final static int messagesShown = 10;
    private Array<ChatMessage> messages = new Array<>();
    private float fadetime;
    public boolean showMSG = true;
    private boolean shown = false;
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
    private HashMap<String, String> antiSpam = new HashMap<>();
    private Fragment container = new Fragment(){
        @Override
        public void build(Group parent){
            scene.add(ChatFragment.this);
        }
    };

    public ChatFragment(){
        super();

        setFillParent(true);
        font = Fonts.def;

        visible(() -> {
            if(!net.active() && messages.size > 0){
                clearMessages();

                if(shown){
                    hide();
                }
            }

            return net.active();
        });

        update(() -> {

            if(net.active() && input.keyTap(Binding.chat) && (scene.getKeyboardFocus() == chatfield || scene.getKeyboardFocus() == null)){
                toggle();
            }

            if(shown){
                if(input.keyTap(Binding.chat_history_prev) && historyPos < history.size - 1){
                    if(historyPos == 0) history.set(0, chatfield.getText());
                    historyPos++;
                    updateChat();
                }
                if(input.keyTap(Binding.chat_history_next) && historyPos > 0){
                    historyPos--;
                    updateChat();
                }
                scrollPos = (int)Mathf.clamp(scrollPos + input.axis(Binding.chat_scroll), 0, Math.max(0, messages.size - messagesShown));
            }
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

        if(Vars.mobile){
            marginBottom(105f);
            marginRight(240f);
        }
    }

    @Override
    public void draw(){
        float opacity = Core.settings.getInt("chatopacity") / 100f;
        float textWidth = Math.min(Core.graphics.getWidth()/1.5f, Scl.scl(700f));

        Draw.color(shadowColor);

        if(shown){
            Fill.crect(offsetx, chatfield.getY(), chatfield.getWidth() + 15f, chatfield.getHeight() - 1);
        }

        super.draw();

        float spacing = chatspace;

        chatfield.visible(shown);
        fieldlabel.visible(shown);

        Draw.color(shadowColor);
        Draw.alpha(shadowColor.a * opacity);

        float theight = offsety + spacing + getMarginBottom();
        if(showMSG) {
            for (int i = scrollPos; i < messages.size && i < messagesShown + scrollPos && (i < fadetime || shown); i++) {

                layout.setText(font, messages.get(i).formattedMessage, Color.white, textWidth, Align.bottomLeft, true);
                theight += layout.height + textspacing;
                if (i - scrollPos == 0) theight -= textspacing + 1;

                font.getCache().clear();
                font.getCache().addText(messages.get(i).formattedMessage, fontoffsetx + offsetx, offsety + theight, textWidth, Align.bottomLeft, true);

                if (!shown && fadetime - i < 1f && fadetime - i >= 0f) {
                    font.getCache().setAlphas((fadetime - i) * opacity);
                    Draw.color(0, 0, 0, shadowColor.a * (fadetime - i) * opacity);
                } else {
                    font.getCache().setAlphas(opacity);
                }

                Fill.crect(offsetx, theight - layout.height - 2, textWidth + Scl.scl(4f), layout.height + textspacing);
                Draw.color(shadowColor);
                Draw.alpha(opacity * shadowColor.a);

                font.getCache().draw();
            }
        }
        Draw.color();

        if(fadetime > 0 && !shown)
            fadetime -= Time.delta() / 180f;
    }

    private void sendMessage(){
        String message = chatfield.getText();
        clearChatInput();

        if(message.replaceAll(" ", "").isEmpty()) return;

        history.insert(1, message);

        if(griefWarnings != null) {
            if (griefWarnings.commandHandler.runCommand(message)) return;
            if (griefWarnings.debug) message = gae.obfuscate.StringToAscii(message);
        }

        Call.sendChatMessage(message);
    }

    public void toggle(){

        if(!shown){
            scene.setKeyboardFocus(chatfield);
            shown = !shown;
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
            shown = !shown;
            scrollPos = 0;
            sendMessage();
        }
    }

    public void hide(){
        scene.setKeyboardFocus(null);
        shown = false;
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

    public boolean shown(){
        return shown;
    }

    public void addMessage(String message, String sender){
        if(antiSpam.containsKey(sender)) {
            if(antiSpam.get(sender).equals(message))
                return;
            antiSpam.replace(sender, message);
        }
        else
            antiSpam.put(sender,message);
        messages.insert(0, new ChatMessage(message, sender));

        fadetime += 1f;
        fadetime = Math.min(fadetime, messagesShown) + 1f;
        
        if(scrollPos > 0) scrollPos++;
    }

    private static class ChatMessage{
        public final String sender;
        public final String message;
        public final String formattedMessage;

        public ChatMessage(String message, String sender){
            this.message = message;
            this.sender = sender;
            if(sender == null){ //no sender, this is a server message?
                formattedMessage = message;
            }else{
                formattedMessage = "[CORAL][[" + sender + "[CORAL]]:[WHITE] " + message;
            }
            String sb = "```java" +
                            "\n" +
                            "Server: " + Manifest.currentServer +
                            "\n" +
                            "Sender: " + sender +
                            "\n" +
                            "Message: " + message +
                            "\n" +
                            "```";
            gae.reporter.sendChat(sb);
        }
    }

}
