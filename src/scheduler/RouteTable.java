package src.scheduler;

import java.util.*;

import src.instances.Vehicle;

public class RouteTable {
    private Vehicle vehicle;
    private List<Route> routes;

    public RouteTable(Vehicle vehicle, List<Route> routes) {
        this.vehicle = vehicle;
        this.routes = routes;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public List<Route> getRoutes() {
        return routes;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public void setRoutes(List<Route> routes) {
        this.routes = routes;
    }

    public String toString() {
        int routeIndex = 1;
        StringBuilder sb = new StringBuilder();
        sb.append("RoutingTable: \n");
        sb.append(vehicle).append("\n");
        for (Route route : routes) {
            sb.append(routeIndex).append(") ");
            sb.append(route).append("\n");
            routeIndex++;
        }
        return sb.toString();
    }

}
