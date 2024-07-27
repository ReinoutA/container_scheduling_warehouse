package src.networkgraph;

public class Edge {
    private Node source;
    private Node destination;
    private int distance;

    public Edge(Node source, Node destination, int distance) {
        this.source = source;
        this.destination = destination;
        this.distance = distance;
    }

    public Node getSourceNode() {
        return source;
    }

    public Node getDestinationNode() {
        return destination;
    }

    public int getDistance() {
        return distance;
    }

    public void setSourceNode(Node source) {
        this.source = source;
    }

    public void setDestinationNode(Node destination) {
        this.destination = destination;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public String toString() {
        return "Edge: " + source + " " + destination + " distance: " + distance;
    }
}
