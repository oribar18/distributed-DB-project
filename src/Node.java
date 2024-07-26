import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;

public class Node implements Runnable {
    private int id;
    private int color;
    private int numNodes;
    private int maxDeg;
    private ArrayList<Integer> neighborsColors;
    private ArrayList<Integer> portsOfNeighborsWithBiggerId;
    private ArrayList<Integer> portsOfNeighborsWithSmallerId;

    public Node(int id, int numNodes, int maxDeg, int[][] neighbors) {
        this.id = id;
        this.color = id;
        this.numNodes = numNodes;
        this.maxDeg = maxDeg;
        this.neighborsColors = new ArrayList<>();
        this.portsOfNeighborsWithBiggerId = new ArrayList<>();
        this.portsOfNeighborsWithSmallerId = new ArrayList<>();
        // Set neighbors ports by id - bigger and smaller
        for (int[] neighbor : neighbors) {
            if (neighbor[0] > id) {
                this.portsOfNeighborsWithBiggerId.add(neighbor[2]);
            } else {
                this.portsOfNeighborsWithSmallerId.add(neighbor[1]);
            }
        }
    }

    public void run() {
        try {
            // Receive messages from neighbors with bigger id
            receiveMessages();
            if (portsOfNeighborsWithBiggerId.size() > 0)
                this.color = setMinNonConflictingColor();
            else
                this.color = 0;
            // Send message to neighbors with smaller id
            for (int port : portsOfNeighborsWithSmallerId) {
                sendMessage(port, this.color);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(int port, int color) {
        try {
            Socket socket = new Socket("localhost", port);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println(color);
            out.close();
            socket.close();
            // to make sure the message is sent
            Thread.sleep(2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void receiveMessages() {
        // Create a thread for each neighbor with bigger id
        CountDownLatch latch = new CountDownLatch(portsOfNeighborsWithBiggerId.size());
        for (Integer port : portsOfNeighborsWithBiggerId) {
            Thread thread = new Thread(() -> {
                try {
                    receiveMessage(port);
                    latch.countDown();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            thread.start();
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void receiveMessage(int port) throws IOException {
        ServerSocket serverSocket;
        try {
            serverSocket = new ServerSocket();
            serverSocket.setReuseAddress(true);
            serverSocket.bind(new InetSocketAddress(port));

            Socket socket = serverSocket.accept();
            Scanner scanner = new Scanner(socket.getInputStream());
            // Receive color from neighbor and add it to the list
            int color = scanner.nextInt();
            this.neighborsColors.add(color);
            scanner.close();
            socket.close();
            serverSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getId() {
        return id;
    }

    public int getColor() {
        return color;
    }

    public int setMinNonConflictingColor() {
        Collections.sort(this.neighborsColors);
        int minColor = 0;
        // Find the min color that is not in the list
        for (int i = 0; i < this.neighborsColors.size(); i++) {
            if (this.neighborsColors.get(i) == minColor) {
                minColor++;
            }
        }
        return minColor;
    }
}
