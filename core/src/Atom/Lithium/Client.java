package Atom.Lithium;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.function.Consumer;

public class Client {
    public final PrintWriter output;
    private final Scanner input;
    private final ArrayList<Consumer<String>> inputListener = new ArrayList<>();
    private volatile boolean alive = true;
    private final Thread inputHandler = new Thread(this::inputHandler);

    public Client(Socket s) throws IOException {
        this.output = new PrintWriter(s.getOutputStream(), true);
        this.input = new Scanner(s.getInputStream());
        inputHandler.setName("Input Handler");
        inputHandler.setDaemon(true);
        inputHandler.start();
    }

    public Client(String ip, int port) throws IOException {
        this(new Socket(ip, port));
    }

    public Client(int port) throws IOException {
        this("127.0.0.1", port);
    }

    public void stop() {
        alive = false;
        inputHandler.interrupt();
        inputHandler.stop();
    }

    private void inputHandler() {
        String temp;
        while (alive) {
            while (input.hasNextLine()) {
                temp = input.nextLine();
                for (Consumer<String> s : inputListener)
                    s.accept(temp);
            }

        }
    }

    public void addInputListener(Consumer<String> s) {
        inputListener.add(s);
    }

}
