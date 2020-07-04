package Photon.Information;

import Atom.Helium.Obfuscate;
import Atom.Hydrogen.Hydrogen;
import arc.math.Rand;
import arc.util.serialization.Base64Coder;
import mindustry.gen.Call;

import static Atom.Manifest.*;
import static mindustry.Vars.ui;

public class EndCrypt {

    private final Hydrogen hydrogen = new Hydrogen();
    private final Obfuscate obfuscate = new Obfuscate();

    public void Send(String message, boolean local) {
        if (local) ui.chatfrag.addMessage(message, null);
        if (!local) Call.sendChatMessage(message);
    }

    public String ClientId() {
        byte[] result = new byte[8];
        new Rand().nextBytes(result);
        return new String(Base64Coder.encode(result));
    }

    public boolean Invalid(String s) {
        String SubString = s.substring(0, 1);
        if (hydrogen.Implicit(EndCryptPrefix)) return true;
        if (!EndCryptPrefix.equals(SubString)) return true;
        return hydrogen.Implicit(s);
    }

    public boolean Encrypt(String s) {
        if (Invalid(s)) return false;
        String EndCrypted = obfuscate.Encrust(s, EveryPrintableAscii, EveryPrintableAsciiButItsFlipped);
        Sended = true;
        Send(EndCrypted, false);
        return true;
    }

    public boolean Decrypt(String s) {
        if (Sended) return false;
        if (Invalid(s)) return false;
        String EndCrypted = obfuscate.Encrust(s, EveryPrintableAscii, EveryPrintableAsciiButItsFlipped);
        Send(EndCrypted, true);
        return true;
    }
}
