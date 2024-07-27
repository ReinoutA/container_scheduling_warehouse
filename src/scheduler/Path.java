package src.scheduler;

import java.util.*;
import src.networkgraph.*;

import static src.instances.Vehicle.VEHICLE_SPEED;

public class Path {
    private Node sourceNode;
    private Node destinationNode;
    private List<Edge> adjacentEdgesFromSourceToDestination;

    public Path(Node sourceNode, Node destinationNode, List<Edge> adjacentEdgesFromSourceToDestination) {
        this.sourceNode = sourceNode;
        this.destinationNode = destinationNode;
        this.adjacentEdgesFromSourceToDestination = adjacentEdgesFromSourceToDestination;
    }

    public Path(Node sourceNode, Node destinationNode) {
        this.sourceNode = sourceNode;
        this.destinationNode = destinationNode;
        this.adjacentEdgesFromSourceToDestination = new ArrayList<>();
    }


    public Node getSourceNode() {
        return sourceNode;
    }

    public Node getDestinationNode() {
        return destinationNode;
    }

    public List<Edge> getAdjacentEdgesFromSourceToDestination() {
        return adjacentEdgesFromSourceToDestination;
    }

    public void setSourceNode(Node sourceNode) {
        this.sourceNode = sourceNode;
    }

    public void setDestinationNode(Node destinationNode) {
        this.destinationNode = destinationNode;
    }

    public void setAdjacentEdgesFromSourceToDestination(List<Edge> adjacentEdgesFromSourceToDestination) {
        this.adjacentEdgesFromSourceToDestination = adjacentEdgesFromSourceToDestination;
    }

    public void addAdjacentEdgesFromSourceToDestination(List<Edge> a) {
        adjacentEdgesFromSourceToDestination.addAll(a);
    }

    public int getTotalPathCost() {
        int totalPathCost = 0;
        for (Edge edge : adjacentEdgesFromSourceToDestination) {
            totalPathCost += edge.getDistance();
        }
        return totalPathCost / VEHICLE_SPEED;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Path from Node ").append(sourceNode.getNodeId()).append(" to Node ")
                .append(destinationNode.getNodeId()).append(":\n");

        if (!adjacentEdgesFromSourceToDestination.isEmpty()) {
            sb.append("Edges:\n");
            Node fromNode = sourceNode;

            for (Edge edge : adjacentEdgesFromSourceToDestination) {
                Node toNode = (edge.getSourceNode() == fromNode) ? edge.getDestinationNode() : edge.getSourceNode();
                sb.append(" - Edge from Node ").append(fromNode.getNodeId());
                sb.append(" to Node ").append(toNode.getNodeId());
                sb.append(", Cost: ").append(edge.getDistance()).append("\n");
                fromNode = toNode;
            }
        } else {
            sb.append("No edges in the path.\n");
        }

        sb.append("Total Cost: ").append(getTotalPathCost());

        return sb.toString();
    }

}
