package Atom.Helium;

import Atom.Hydrogen.Hydrogen;
import Atom.Nitrogen.Info;

public class Obfuscate {

    private final Hydrogen hydrogen = new Hydrogen();
    private final Info info = new Info("Obfuscator");

    public int hashString(String s, int hash, int hash2){
       for(char c : s.toCharArray()){
           hash = hash * hash2 + c;
       }
       return hash;
    }

    // Excuse me wtf
    public int hashInt(int s, int hash, int hash2){
        return s * hash * hash2;
    }

    public String AsciiToString(String a, String fix) {
        int Length = a.length();
        if (Length < 1) return AsciiToString(a);
        int[] b = hydrogen.SliceInt(a, fix);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Length; i++) {
            int d = b[i];
            char ch = (char) d;
            sb.append(d);
            sb.append(" ");
        }
        return new String(sb);
    }

    public String AsciiToString(String a) {
        try {
            int b = Integer.parseInt(a);
            char ch = (char) b;
            return String.valueOf(ch);
        } catch (NumberFormatException ignored) {
        }
        return "NULL";
    }

    public String AsciiToString(int b) {
        char ch = (char) b;
        return String.valueOf(ch);
    }

    public String StringToAscii(String a) {

        StringBuilder sb = new StringBuilder();
        for (char c : a.toCharArray()) {
            sb.append((int) c);
            sb.append(" ");
        }
        return sb.toString();
    }

    public String Encrust(String s, String source, String target) {

        if (hydrogen.Implicit(target)) return "NULL";
        if (hydrogen.Implicit(source)) return "NULL";
        if (hydrogen.Implicit(s)) return "NULL";
        info.debug(s + source + target);
        char[] result = new char[10];
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            int index = source.indexOf(c);
            result[i] = target.charAt(index);
        }
        String Encrusts = new String(result);
        info.debug(Encrusts);
        return Encrusts;
    }


}