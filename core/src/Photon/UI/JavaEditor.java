package Photon.UI;

import Atom.Nitrogen.RuntimeClass;
import arc.Core;
import arc.scene.ui.Label;
import arc.scene.ui.TextArea;
import arc.scene.ui.layout.Table;
import arc.struct.Array;
import arc.util.Log;
import com.google.googlejavaformat.java.Formatter;
import com.google.googlejavaformat.java.FormatterException;
import mindustry.Vars;
import mindustry.gen.Icon;
import mindustry.ui.Fonts;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.FloatingDialog;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

public class JavaEditor extends FloatingDialog {
    public static int pad = 8;
    private static RuntimeClass rc;
    public Array<String> messages = new Array<>();
    private HashMap<String, String> color = new HashMap<>();
    private TextArea a;
    private Label label;

    public JavaEditor() {
        super("Java Code Editor");
        shouldPause = true;
        addCloseButton();
        shown(this::setup);
        update(this::updateChat);
        onResize(this::setup);
        //update(this::updateColor);
        addColor();
    }

    void Compile() {
        //Array<String> s = Array.with(a.getText().replace("[", " ").replace("]", " ").split(" "));
       // StringBuilder sb = new StringBuilder();
        //for(String a : s) {
        //    if (color.containsValue(a))
        //        continue;
        //    sb.append(sb);
        //}
        rc.AssignSourceCode(a.getText().replace("\r", "\n"));
        rc.compile();
        setup();
    }

    void setup() {
        cont.clear();
        label = null;
        if (rc == null) {
            AtomicReference<String> name = new AtomicReference<>();
            AtomicReference<String> packages = new AtomicReference<>();
            packages.set("Atom.Test");
            name.set("Test");
            Table table = new Table();
            table.add("Class Name: ");
            table.addField(name.get(), w -> {
                if (w.isEmpty())
                    Vars.ui.showInfo("Name must not empty");
                else
                    name.set(w);
            });
            table.row();
            table.add("Package: ");
            table.addField(packages.get(), w -> {
                if (w.isEmpty())
                    Vars.ui.showInfo("Package must not empty");
                else if (w.contains(" "))
                    Vars.ui.showInfo("Package may not have space");
                else
                    packages.set(w);
            });
            table.addButton("save", Styles.clearPartialt, () -> {
                rc = new RuntimeClass(packages.get(), name.get());
                table.clear();
                setup();

            });
            cont.row();
            cont.add(table);
        } else {
            buttons.clear();
            addCloseButton();
            buttons.addImageTextButton("Reformat", Icon.book, this::setup);
            buttons.addImageTextButton("Compile", Icon.box, this::Compile);
            buttons.addImageTextButton("Save", Icon.save, () -> Vars.platform.showFileChooser(false, ".java", fi -> rc.save(fi.file())));
            buttons.addImageTextButton("Open", Icon.file, () -> Vars.platform.showFileChooser(true, ".java", fi -> rc = new RuntimeClass(fi)));
            if (a != null)
                try {
                    a = new TextArea(new Formatter().formatSource(a.getText()));
                } catch (FormatterException ignored) {
                    Log.err(ignored.getMessage());
                }
            else
                a = new TextArea(rc.toString().replace("\n", "\r"));
            label = new Label("");
            label.setStyle(new Label.LabelStyle(label.getStyle()));
            label.getStyle().font = Fonts.def;
            label.setStyle(label.getStyle());
            label.getText();
            cont.add(a).size(Core.graphics.getWidth(), Core.graphics.getHeight() - 256);
            cont.row();
            cont.add(label).size(Core.graphics.getWidth(), 156);
        }
    }

    public void updateChat() {
        if (label == null)
            return;
        StringBuilder sb = new StringBuilder();
        for (String s : messages) {
            sb.append(s);
            sb.append("\n");
        }
        label.setText(sb.toString());
    }

    public void updateColor(){
        if(a == null)
            return;
        Array<String> code = Array.with(a.getText().replace("\r", "\n").split(" "));
        StringBuilder sb = new StringBuilder();
        boolean contain = false;
        for(String s : code){
            if(color.containsKey(s)) {
                sb.append("[").append(color.get(s)).append("]");
                contain = true;
            }
            sb.append(s);
            sb.append("[white]");
            sb.append(" ");
        }
        if(!contain)
            return;
        try {
            a.setText(new Formatter().formatSource(sb.toString().replace("\n", "\r")));
            return;
        } catch (FormatterException ignored) { }
        a.setText(sb.toString().replace("\n", "\r"));
    }

    public void addColor(){
        color.clear();
        color.put("class", "purple");
        color.put("static", "purple");
        color.put("void", "purple");
        color.put("int", "purple");
        color.put("new", "purple");
        color.put("if", "purple");
        color.put("else", "purple");
        color.put("try", "purple");
        color.put("catch", "purple");
        color.put("return", "purple");
        color.put("for", "purple");
        color.put("throw", "purple");
        color.put("public", "orange");
        color.put("private", "orange");
        color.put("protected", "orange");
        color.put("import", "orange");
        color.put("package", "orange");
        color.put("true", "orange");
        color.put("false", "orange");
        color.put("null", "orange");
        color.put("gaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaae", "white");
    }

}
