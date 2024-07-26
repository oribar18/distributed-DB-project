import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.net.*;
import java.lang.Math;
import java.util.*;
import java.io.*;

import static java.lang.Thread.sleep;


public class Manager {
    private int numNodes;
    private int maxDeg;
    private ArrayList<Node> nodes;
    private ArrayList<Thread> threads;
    private StringBuilder output;


    public Manager() {
        this.nodes = new ArrayList<>();
        this.threads = new ArrayList<>();
    }


    public void readInput(String path) {
        // Read input file
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            this.numNodes = Integer.parseInt(reader.readLine().trim());
            this.maxDeg = Integer.parseInt(reader.readLine().trim());

            for (int i = 0; i < this.numNodes; i++) {
                String line = reader.readLine().trim();
                int nodeId = Integer.parseInt(line.substring(0, line.indexOf(" ")));

                // Get neighbors
                String neighbors = line.substring(line.indexOf("[")+1, line.lastIndexOf("]"));
                // Split neighbors
                String[] neighborsDetails = neighbors.split("\\], \\[");
                int[][] neighborsArr = new int[neighborsDetails.length][3];

                for (int j = 0; j < neighborsDetails.length; j++) {
                    // Split neighbor details
                    String[] neighborParts = neighborsDetails[j].replaceAll("\\[", "").replaceAll("\\]", "").split(",");
                    for (int k = 0; k < neighborParts.length; k++) {
                        neighborParts[k] = neighborParts[k].trim();
                    }
                    int neighborID = Integer.parseInt(neighborParts[0]);
                    int writingPort = Integer.parseInt(neighborParts[1]);
                    int readingPort = Integer.parseInt(neighborParts[2]);
                    // Add neighbor to neighbors array
                    neighborsArr[j] = new int[]{neighborID, writingPort, readingPort};
                }
                // Create new node
                Node new_node = new Node(nodeId, this.numNodes, this.maxDeg, neighborsArr);
                this.nodes.add(new_node);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public String start() {
        // Start nodes
        for (Node node : this.nodes) {
            Thread thread = new Thread(node);
            this.threads.add(thread);
            thread.start();
            // Sleep for 1ms to make sure all nodes are ready
            try {
                sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        // Wait for all nodes to finish
        for (Thread thread : this.threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
            e.printStackTrace();
            }
        }
        output = new StringBuilder();
        // Build output
        for (Node node : this.nodes) {
            output.append(node.getId()).append(",").append(node.getColor()).append("\n");
        }
        return output.toString();
    }

    public synchronized String terminate() {
        output = new StringBuilder();
        for (Node node : this.nodes) {
            output.append(node.getId()).append(",").append(node.getColor()).append("\n");
        }
        return output.toString();
    }

}
