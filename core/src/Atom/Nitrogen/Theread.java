package Atom.Nitrogen;

import java.util.Set;

public class Theread {

    public static void printThreadStatus(){
        Set<Thread> threads = Thread.getAllStackTraces().keySet();
        System.out.println("\n----------------------------------------------");
        for (Thread t : threads) {
            String name = t.getName();
            Thread.State state = t.getState();
            int priority = t.getPriority();
            String type = t.isDaemon() ? "Daemon" : "Normal";
            System.out.printf("%-20s \t %s \t %d \t %s\n", name, state, priority, type);
        }
    }
}
