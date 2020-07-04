package Atom.Nitrogen;

import Photon.gae;
import arc.files.Fi;
import arc.struct.Array;
import arc.struct.CharArray;
import arc.util.Log;
import com.google.googlejavaformat.java.Formatter;
import com.google.googlejavaformat.java.FormatterException;
import mindustry.Vars;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class RuntimeClass {
    public String packages, name, code;
    public Array<String> imports = new Array<>();
    public Class<?> clazz;
    public Object instance;
    public File root, sourceFile;
    private ByteArrayOutputStream err;

    public RuntimeClass(Fi f){
        this(f.file());
    }

    public RuntimeClass(File f){
        String s = "";
        try { s = Files.readAllLines(f.toPath()).toString(); } catch (IOException ignored) { }
        if(s.isEmpty()) {
            this.packages = "Test";
            this.name = "Tests";
            this.code = ("\n  public " + "Tests" + "(){\n\n  }\n");
        }else
            AssignSourceCode(s);
    }

    public RuntimeClass(String source) {
        AssignSourceCode(source);
    }

    public RuntimeClass(String packages, String name) {
        this(packages,name,("\n  public " + name + "(){\n\n  }\n"));
    }

    public RuntimeClass(){
        this("Test", "Tests", ("\n  public " + "Tests" + "(){\n\n  }\n"));
    }

    public RuntimeClass(String packages, String name, String code) {
        this.packages = packages;
        this.name = name;
        this.code = code;
    }

    public static void runLine(String line){
        line = "public Gay(){" + line + "}";
       RuntimeClass rc = new RuntimeClass("Atom.Test", "Gay", line);
       rc.compile();
    }

    public void AssignSourceCode(String source){
        Array<String> total = Array.with(source.replace("\n", " ").split(" "));
        Array<String> totals = Array.with(source.split(" "));
        //get Packages name
        packages = total.get(total.indexOf("package") + 1).replace(";", "");
        //get Import List
        while (total.contains("import")) {
            int index = total.indexOf("import");
            total.remove("import");
            imports.add(total.remove(index).replace(";", ""));
        }
        //get Class Name
        name = total.get(total.indexOf("class") + 1).replace("{", "");

        //Get all method
        CharArray c = gae.hydrogen.getCharArray(source);
        StringBuilder sb = new StringBuilder();
        int codeStartIndex = c.indexOf('{') + 1;
        int codeEndIndex = c.size - 1;
        for (int i = codeStartIndex; i < c.size; i++)
            if(c.get(i) == '}')
                codeEndIndex = i;
        for (int i = codeStartIndex; i < codeEndIndex; i++)
            sb.append(c.get(i));
        code = sb.toString();
    }

    public void compile() {
        try {
            sourceFile = null;
            saveCode();
            err = null;
            err = new ByteArrayOutputStream();
            // Compile source file.
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            compiler.run(null, null, err, sourceFile.getAbsolutePath());

            loadCode();
        } catch (Throwable t) {
            assert err != null;
            Vars.ui.showInfoText("Compiler Error", err.toString());
            Log.err("Compiler Error");
        }
    }

    private void loadCode() throws ClassNotFoundException, IllegalAccessException, InstantiationException, MalformedURLException {

        // Load and instantiate compiled class.
        URLClassLoader classLoader = URLClassLoader.newInstance(new URL[]{root.toURI().toURL()});
        clazz = Class.forName(this.packages +"." + this.name, true, classLoader); // Init pre-class
            instance = clazz.newInstance(); // Init class

    }

    public void save(File save){
        try { Files.write(save.toPath(), this.toString().getBytes(StandardCharsets.UTF_8)); } catch (IOException a) {Vars.ui.showException(a); }
    }

    private void saveCode() throws IOException {
        // Write code to file
        if(root == null)
            root = new File("Compiler/");
        sourceFile = new File(root, this.packages.replace(".", "/") + "/" + this.name + ".java");
        sourceFile.getParentFile().mkdirs();
        Files.write(sourceFile.toPath(), this.toString().getBytes(StandardCharsets.UTF_8));
    }

    public Method getMethod(String name) {
        try {
            return clazz.getMethod(name);
        } catch (NoSuchMethodException e) {
            Log.err(e);
        }
        return null;
    }

    public String getPath() {
        return packages + "." + name;
    }

    public String getPackages() {
        return "package " + packages + ";";
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("package ").append(packages).append(";\n");
        for (String s : imports)
            sb.append("import ").append(s).append(";\n");
        sb.append("\n").append("public").append(" class ").append(name).append("{");
        sb.append(code);
        sb.append("}");
        try {
            return new Formatter().formatSource(sb.toString());
        } catch (FormatterException e) {
            Log.err("Error Formatting Code");
            return sb.toString();
        }

    }

}