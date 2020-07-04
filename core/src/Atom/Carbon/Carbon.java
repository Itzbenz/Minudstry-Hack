package Atom.Carbon;

import Atom.Beryllium.TimeKeeper;
import Atom.Hydrogen.Hydrogen;
import arc.util.Log;

import java.util.Random;

public class Carbon {

    private final TimeKeeper timeKeeper = new TimeKeeper();
    private final Hydrogen hydrogen = new Hydrogen();

    public int randInt(int min, int max) {
        long seed = timeKeeper.getMilis();
        Random rnd = new Random();
        rnd.setSeed(seed);
        int ran = rnd.nextInt(max);
        if (ran <= 0) ran = 0;
        return ran + min;
    }

    public int iterativeSearch(int[] arrayToSearch, int element) {
        int lowIndex = 0;
        int highIndex = arrayToSearch.length - 1;

        // Holds the position in array for given element
        // Initial negative integer set to be returned if no match was found on array
        int elementPos = -1;

        // If lowIndex less than highIndex, there's still elements in the array
        while (lowIndex <= highIndex) {
            int midIndex = (lowIndex + highIndex) / 2;
            if (element == arrayToSearch[midIndex]) {
                elementPos = midIndex;
                break;
            } else if (element < arrayToSearch[midIndex]) {
                highIndex = midIndex - 1;
            } else if (element > arrayToSearch[midIndex]) {
                lowIndex = midIndex + 1;
            }
        }
        return elementPos;
    }

    public boolean SortingThreshold(String source, String target, int Threshold) {
        String[] sources = hydrogen.SliceString(source, "(?!^)");
        String[] targets = hydrogen.SliceString(source, "(?!^)");
        int[] prob = new int[(sources.length * targets.length)];
        int f = 0;
        for (int i = 0; i < target.length(); i++) {
            for (int n = 0; n < source.length(); n++) {
                if (targets[i].equals(sources[n])) {
                    prob[f] = 1;
                } else {
                    prob[f] = 0;
                }
                f++;
            }
        }
        int sum = 0;
        for (int i : prob)
            sum += i;
        //TODO make the right equation
        Log.info("Before " + sum + "   " + prob.length);
        sum = (sum / prob.length) * 100;
        Log.info(sum);
        return sum > Threshold;
    }
}
