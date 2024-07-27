package src.networkgraph;

import src.instances.Location;

public class Node {

    private Location location;
    private final int nodeId;

    public Node(int nodeId, Location location) {
        this.nodeId = nodeId;
        this.location = location;
    }

    public Location getLocation() {
        return location;
    }

    public int getNodeId() {
        return nodeId;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    @Override
    public String toString() {
        return "Node ID: " + nodeId + " (X: " + location.getX() + ", Y: " + location.getY() + ")";
    }

}
