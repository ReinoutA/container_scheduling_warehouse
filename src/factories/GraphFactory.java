package src.factories;

import src.instances.*;
import src.networkgraph.*;
import java.util.*;

public class GraphFactory {
    public Graph convertLocationsToGraph(List<Location> locations) {
        Graph graph = new Graph();
        int nodeId = 0;
        List<Node> stackNodes = new ArrayList<>();
        List<Node> bufferNodes = new ArrayList<>();

        for (Location location : locations) {
            Node currentNode = new Node(nodeId, location);
            graph.addNode(currentNode);

            if (location.isBufferPoint()) {
                bufferNodes.add(currentNode);
            } else if (location.isStorageStack()) {
                stackNodes.add(currentNode);
            }

            nodeId++;
        }

        // Sorteer de stackNodes op y-coördinaat en vervolgens op x-coördinaat
        stackNodes.sort((node1, node2) -> {
            double y1 = node1.getLocation().getY();
            double y2 = node2.getLocation().getY();

            if (y1 != y2) {
                return Double.compare(y1, y2);
            } else {
                double x1 = node1.getLocation().getX();
                double x2 = node2.getLocation().getX();
                return Double.compare(x1, x2);
            }
        });

        // Verbind elke stackNode met de dichtstbijzijnde op dezelfde y-waarde
        for (int i = 0; i < stackNodes.size() - 1; i++) {
            Node currentStackNode = stackNodes.get(i);
            Node nextStackNode = stackNodes.get(i + 1);
            if (currentStackNode.getLocation().getY() == nextStackNode.getLocation().getY()) {
                int distance = calculateDistance(currentStackNode.getLocation(), nextStackNode.getLocation());
                Edge edge = new Edge(currentStackNode, nextStackNode, distance);
                graph.addEdge(edge);
            }
        }

        // Verbind het bufferNode met de dichtstbijzijnde stackNode langs dezelfde
        // y-waarde
        if (!bufferNodes.isEmpty() && !stackNodes.isEmpty()) {
            for(Node bufferNode : bufferNodes) {
                Node nearestStackNode = stackNodes.get(0);
                int distance = calculateDistance(bufferNode.getLocation(), nearestStackNode.getLocation());
                Edge edge = new Edge(bufferNode, nearestStackNode, distance);
                graph.addEdge(edge);
            }
        }

        // Verbind de uiterst linker node van een andere y-waarde met de stackNode op
        // dezelfde x-waarde (boven of onder)
        for (int i = 0; i < stackNodes.size(); i++) {
            Node currentStackNode = stackNodes.get(i);

            if (i > 0 && currentStackNode.getLocation().getX() != stackNodes.get(i - 1).getLocation().getX()) {
                // Alleen de uiterst linkse node met dezelfde x-waarde verbinden
                continue;
            }

            for (int j = 0; j < stackNodes.size(); j++) {
                if (i != j) {
                    Node otherStackNode = stackNodes.get(j);
                    if (currentStackNode.getLocation().getX() == otherStackNode.getLocation().getX()) {
                        int distance = Math
                                .abs(currentStackNode.getLocation().getY() - otherStackNode.getLocation().getY());
                        //System.out.println("Distance: " + distance);

                        boolean edgeExists = false;
                        for (Edge existingEdge : graph.getEdges()) {
                            if ((existingEdge.getSourceNode() == currentStackNode
                                    && existingEdge.getDestinationNode() == otherStackNode) ||
                                    (existingEdge.getSourceNode() == otherStackNode
                                            && existingEdge.getDestinationNode() == currentStackNode)) {
                                edgeExists = true;
                                break;
                            }
                        }

                        if (!edgeExists) {
                            Edge edge = new Edge(currentStackNode, otherStackNode, distance);
                            graph.addEdge(edge);
                        }

                    }
                }
            }
        }

        return graph;
    }

    private int calculateDistance(Location location1, Location location2) {
        int x1 = location1.getX();
        int y1 = location1.getY();
        int x2 = location2.getX();
        int y2 = location2.getY();
        return (int) Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }
}
