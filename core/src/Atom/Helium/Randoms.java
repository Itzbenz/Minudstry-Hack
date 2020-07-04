package Atom.Helium;

import arc.struct.Array;

import java.util.Random;

public class Randoms {
    int r = 0;
    public String RandChar(int amount) {
        StringBuilder SB = new StringBuilder();
        Random r = new Random();
        char ObedCase = 'a';
        for (int i = 0; i < amount; i++) {
            if (getRandomBoolean()) ObedCase = 'A';
            char c = (char) (r.nextInt(26) + ObedCase);
            SB.append(c);
        }
        return String.valueOf(SB);
    }

    public boolean getRandomBoolean() {
        return Math.random() < 0.5;
    }

    public byte[] getRandomBytes(){
        byte[] bytes = new byte[8];
        new Random().nextBytes(bytes);
        return bytes;
    }

    public byte[] getRandomBytes(int length){
        byte[] bytes = new byte[length];
        new Random().nextBytes(bytes);
        return bytes;
    }

    public int getRandomInt(int range, long seed, boolean noZero) {
        int rands = 0;
        Random rand = new Random();
        if (seed < 10000) {
            rand.setSeed(seed);
        }
        rands = rand.nextInt(range);
        while (rands >= range){
           rands = getRandomInt(range, System.currentTimeMillis(), noZero);
        }

        r = rands;
        return rands;
    }

    public int getRandomInt(int range){
        return getRandomInt(range, System.currentTimeMillis(), false);
    }

    public int getRandomInt(){
        return getRandomInt(10, System.currentTimeMillis(), false);
    }

    public String shuffle(String input){
        return shuffle(input, System.currentTimeMillis());
    }

    public String getRandomString(){
        return getRandomString(6);
    }

    public String getRandomString(int howMany){
        byte[] chas = new byte[howMany];
        new Random().nextBytes(chas);
        return new String(chas);
    }

    public char selectAChar(String s){
        Random random = new Random();
        int index = random.nextInt(s.length());
        return s.charAt(index);
    }
    public String shuffle(String input, long seed) {
        Array<Character> string = new Array<>();
        for (char c : input.toCharArray())
            string.add(c);
        StringBuilder output = new StringBuilder();
        while (string.size != 0) {
            output.append(string.remove(getRandomInt(string.size)));
        }
        return (output.toString());
    }
}

