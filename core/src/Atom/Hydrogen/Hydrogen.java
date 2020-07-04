package Atom.Hydrogen;

import arc.struct.Array;
import arc.struct.CharArray;

import java.util.ArrayList;
import java.util.List;

public class Hydrogen {

    public boolean ContainIntOnly(String s) {
        return (s.matches("[0-9]+"));
    }

    public String RemoveFirstChar(String s, int howMany) {
        return s.substring(howMany);
    }

    public String RemoveFirstChar(String s) {
        return s.substring(1);
    }

    public CharArray getCharArray(String a){
        CharArray c = new CharArray();
        for (int i = 0;i < a.length(); i++){
            c.add(a.charAt(i));
        }
        return c;
    }

    public String removeLastChar(String str) {

            if (str != null && str.length() > 0 && str.charAt(str.length() - 1) == 'x') {
                str = str.substring(0, str.length() - 1);
            }
            return str;

    }

    public String[] SliceString(String s, String fix) {
        return s.split(fix, -1);
    }

    public int[] SliceInt(String s, String fix) {
        String[] strArray = s.split(fix);
        int[] intArray = new int[strArray.length];
        for (int i = 0; i < strArray.length; i++) {
            intArray[i] = Integer.parseInt(strArray[i]);
        }
        return intArray;
    }

    public String commandsFormat(String s){
        return s.toLowerCase().replaceAll(" ", "-");
    }

    public String split(String s, int whichOne, String regex) {
        if (s.startsWith(" ")) s = s.replaceFirst(" ", "");
        String[] arr = s.split(regex, 2);
        whichOne = whichOne - 1;
        return arr[whichOne];
    }

    public String[] split(String s, String regex) {
        if (s.startsWith(" ")) s = s.replaceFirst(" ", "");
        return s.split(regex, 1000);
    }

    public String joiner(String delimiter, Array join) {
        StringBuilder sb = new StringBuilder();
        try {
            for (Object s : join) {
                String str = (String) s;
                sb.append(str);
                sb.append(delimiter);
            }
            return sb.toString();
        }catch (Throwable ignored){
            return "String Joiner Failed";
        }
    }

    public String shuffle(String input) {
        List<Character> characters = new ArrayList<Character>();
        for (char c : input.toCharArray()) {
            characters.add(c);
        }
        StringBuilder output = new StringBuilder(input.length());
        while (characters.size() != 0) {
            int randPicker = (int) (Math.random() * characters.size());
            output.append(characters.remove(randPicker));
        }
        return output.toString();
    }

    public String capitalizeFirstLetter(String s){
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    public String lowerFirstLetter(String s){
        return s.substring(0, 1).toLowerCase() + s.substring(1);
    }

    public boolean Declared(String gae) {
        if (gae == null) return false;
        if (gae.length() < 1) return false;
        return !gae.equals("NULL");
    }

    public boolean Declared(Object object){ return !(object == null); }

    public boolean Declared(int gae) {
        return gae != 0;
    }

    public boolean Implicit(String gae) {
        if (gae == null) return true;
        if (gae.length() < 1) return true;
        return gae.equals("NULL");
    }

    public boolean Even(int n) {
        return (n % 2) == 0;
    }
}
