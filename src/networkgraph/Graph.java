package src.networkgraph;

import src.instances.*;
import src.scheduler.*;

import java.util.*;

public class Graph {
    private final List<Node> nodes;
    private final List<Edge> edges;

    public Graph() {
        nodes = new ArrayList<>();
        edges = new ArrayList<>();
    }

    public void addNode(Node node) {
        nodes.add(node);
    }

    public void addEdge(Edge edge) {
        edges.add(edge);
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public Node getNodeById(int nodeId) {
        for (Node node : nodes) {
            if (node.getNodeId() == nodeId) {
                return node;
            }
        }
        return null;
    }

    public List<Edge> getEdgesConnectedToNode(Node node) {
        List<Edge> connectedEdges = new ArrayList<>();
        for (Edge edge : edges) {
            if (edge.getSourceNode() == node || edge.getDestinationNode() == node) {
                connectedEdges.add(edge);
            }
        }
        return connectedEdges;
    }

    public Edge getEdgeBetweenNodes(Node source, Node destination) {
        for (Edge edge : edges) {
            if ((edge.getSourceNode() == source && edge.getDestinationNode() == destination)
                    || (edge.getSourceNode() == destination && edge.getDestinationNode() == source)) {
                return edge;
            }
        }
        return null;
    }

    public Node getNodeWithLocationName(String locationName) {
        for (Node n : nodes) {
            if (n.getLocation().getName().equals(locationName)) {
                return n;
            }
        }
        return null;
    }

    public Node getNodeWithLocation(Location location) {
        for (Node n : nodes) {
            if (n.getLocation() == location) {
                return n;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("GRAPH: \n");
        sb.append(" - Nodes:\n");
        for (Node node : nodes) {
            sb.append("  - Node ID: ").append(node.getNodeId()).append(" (").append(node.getLocation().getX())
                    .append(", ").append(node.getLocation().getY()).append(")").append("\n");
        }

        sb.append(" \n - Edges:\n");
        for (Edge edge : edges) {
            sb.append("  - Edge from Node ").append(edge.getSourceNode().getNodeId()).append(" (")
                    .append(edge.getSourceNode().getLocation().getX()).append(", ")
                    .append(edge.getSourceNode().getLocation().getY()).append(")");
            // sb.append(", Distance: ").append(edge.getDistance()).append("\n");
            sb.append(" to Node ").append(edge.getDestinationNode().getNodeId()).append(" (")
                    .append(edge.getDestinationNode().getLocation().getX()).append(", ")
                    .append(edge.getDestinationNode().getLocation().getY()).append(")");
            // sb.append(", Distance: ").append(edge.getDistance()).append("\n");
            sb.append("\n");
        }

        return sb.toString();
    }

    public List<Node> shortestPathWithCost(Node source, Node destination) {
        Map<Node, Double> shortestDistances = new HashMap<>();
        Map<Node, Node> predecessorNodes = new HashMap<>();
        PriorityQueue<Node> priorityQueue = new PriorityQueue<>(Comparator.comparingDouble(shortestDistances::get));

        for (Node node : nodes) {
            shortestDistances.put(node, Double.MAX_VALUE);
        }

        shortestDistances.put(source, 0.0);
        priorityQueue.add(source);

        while (!priorityQueue.isEmpty()) {
            Node currentNode = priorityQueue.poll();

            if (currentNode.equals(destination)) {
                return reconstructPath(predecessorNodes, currentNode);
            }

            for (Edge edge : getEdgesConnectedToNode(currentNode)) {
                Node neighbor = (edge.getSourceNode() == currentNode) ? edge.getDestinationNode()
                        : edge.getSourceNode();
                double tentativeDistance = shortestDistances.get(currentNode) + edge.getDistance();

                // Controleer of de waarde in de map niet null is voordat je deze omzet naar
                // double
                Double neighborDistance = shortestDistances.get(neighbor);
                if (neighborDistance == null || tentativeDistance < neighborDistance) {
                    shortestDistances.put(neighbor, tentativeDistance);
                    predecessorNodes.put(neighbor, currentNode);
                    priorityQueue.add(neighbor);
                }
            }
        }
        // No path found
        return Collections.emptyList();
    }

    private List<Node> reconstructPath(Map<Node, Node> predecessors, Node current) {
        List<Node> path = new ArrayList<>();

        while (predecessors.containsKey(current)) {
            path.add(current);
            current = predecessors.get(current);
        }

        path.add(current);
        Collections.reverse(path);

        return path;
    }

    public Path findShortestPath(Node source, Node destination) {
        List<Node> pathNodes = shortestPathWithCost(source, destination);

        if (!pathNodes.isEmpty()) {
            List<Edge> pathEdges = new ArrayList<>();
            Node fromNode = source;

            for (Node toNode : pathNodes) {
                if (fromNode != toNode) {
                    Edge edge = getEdgeBetweenNodes(fromNode, toNode);

                    if (edge != null) {
                        pathEdges.add(edge);
                    }
                }
                fromNode = toNode;
            }

            return new Path(source, destination, pathEdges);
        } else {
            return null; // Geen pad gevonden
        }
    }

    public Path findShortestPath(Location sourceLocation, Location destinationLocation) {
        Node source = getNodeWithLocation(sourceLocation);
        Node destination = getNodeWithLocation(destinationLocation);
        List<Node> pathNodes = shortestPathWithCost(source, destination);

        if (!pathNodes.isEmpty()) {
            List<Edge> pathEdges = new ArrayList<>();
            Node fromNode = source;

            for (Node toNode : pathNodes) {
                if (fromNode != toNode) {
                    Edge edge = getEdgeBetweenNodes(fromNode, toNode);

                    if (edge != null) {
                        pathEdges.add(edge);
                    }
                }
                fromNode = toNode;
            }

            return new Path(source, destination, pathEdges);
        } else {
            //System.out.println("No path found between " + sourceLocation.getName() + " and " + destinationLocation.getName());
            return null; // Geen pad gevonden
        }
    }

    public Path findShortestPathToNearestNodeOnY(Location vehicleLocation, List<Node> nodes) {
        Node nearestNode = null;
        double shortestXDistance = Double.MAX_VALUE;

        // Zoek de dichtstbijzijnde knoop met dezelfde y-waarde als de voertuiglocatie
        for (Node node : nodes) {
            if (node.getLocation().getY() == vehicleLocation.getY()) {
                double xDistance = Math.abs(node.getLocation().getX() - vehicleLocation.getX());
                if (xDistance < shortestXDistance) {
                    nearestNode = node;
                    shortestXDistance = xDistance;
                }
            }
        }

        if (nearestNode == null) {
            //System.out.println("Geen knoop gevonden.");
            return null; // Geen knoop gevonden
        }

        // Maak een nieuwe edge tussen de node van het voertuig en de dichtstbijzijnde
        // node
        Node vehicleNode = new Node(999, vehicleLocation);
        Edge tempEdge = new Edge(vehicleNode, nearestNode,
                Math.abs(vehicleLocation.getX() - nearestNode.getLocation().getX()));
        edges.add(tempEdge);
        // System.out.println("Tempedge: " + tempEdge);
        List<Edge> adjEdges = new ArrayList<>();
        adjEdges.add(tempEdge);

        Path path = new Path(vehicleNode, nearestNode, adjEdges);

        return path;
    }

}
