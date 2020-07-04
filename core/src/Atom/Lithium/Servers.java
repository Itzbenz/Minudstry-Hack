package Atom.Lithium;

import arc.math.Mathf;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class Servers {
    // Local server
    public final int port;
    private final ServerSocket serverSocket;
    private final ArrayList<Consumer<String>> inputListener = new ArrayList<>();
    private final ArrayList<Consumer<Socket>> onConnectListener = new ArrayList<>();
    private final ArrayList<PrintWriter> outputClientList = new ArrayList<>();
    private final ScheduledExecutorService service = Executors.newScheduledThreadPool(2);
    private volatile boolean run = true;
    private final Thread connectionHandler = new Thread(this::handleConnection);
    private int currentClientID = 0;

    public Servers(int port) throws IOException {
        this.port = port;
        this.serverSocket = new ServerSocket(port, 100);
        connectionHandler.setName("Connection Handler");
        connectionHandler.setDaemon(true);
        connectionHandler.start();
    }

    public void sendToEveryClientDelay(String s, int delay) {
        for (PrintWriter printWriter : outputClientList)
            service.schedule(() -> printWriter.println(s), Mathf.random(delay), TimeUnit.MICROSECONDS);
    }

    public void sendToEveryClientDelay(String s) {
        for (PrintWriter printWriter : outputClientList)
            service.schedule(() -> printWriter.println(s), Mathf.random(2000), TimeUnit.MICROSECONDS);
    }

    public void sendToClientDelay(String s) {
            service.schedule(() -> sendToClient(s),  Mathf.random(2000), TimeUnit.MICROSECONDS);
    }

    public void sendToClientDelay(String s, int ID) {
        service.schedule(() -> sendToClient(s, ID),  Mathf.random(2000), TimeUnit.MICROSECONDS);
    }

    public void sendToClient(String s) {
        if(currentClientID >= outputClientList.size())
            currentClientID = 0;
        sendToClient(s, currentClientID);
        currentClientID++;
    }

    public void sendToClient(String s, int ID) {
        if(outputClientList.isEmpty())
            return;
        outputClientList.get(ID).println(s);
    }

    public void sendToEveryClient(String s) {
        for (PrintWriter printWriter : outputClientList)
            printWriter.println(s);
    }

    public void addInputListener(Consumer<String> s) {
        inputListener.add(s);
    }
    public void addOnConnectListener(Consumer<Socket> s) {
        onConnectListener.add(s);
    }
    public void closeServer() {
        run = false;
        connectionHandler.interrupt();
        connectionHandler.stop();
    }

    private void handleConnection() {
        try {
            while (run) {
                handleClients(serverSocket.accept());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void handleClients(Socket s) {
        Thread t = new Thread(() -> handleClient(s));
        t.setDaemon(true);
        t.start();
    }

    private void handleClient(Socket socket) {
        InputStream inp;
        BufferedReader brinp;
        try {
            inp = socket.getInputStream();
            for(Consumer<Socket> s : onConnectListener)
                s.accept(socket);
            brinp = new BufferedReader(new InputStreamReader(inp));
            outputClientList.add(new PrintWriter(socket.getOutputStream(), true));
        } catch (IOException e) {
            return;
        }
        String line;
        while (run) {
            try {
                line = brinp.readLine();
                if ((line == null) || line.equalsIgnoreCase("QUIT")) {
                    socket.close();
                    return;
                } else {
                    for (Consumer<String> s : inputListener)
                        s.accept(line);
                }
            } catch (IOException e) {
                System.out.println(e.getMessage());
                return;
            }
        }
    }
}
