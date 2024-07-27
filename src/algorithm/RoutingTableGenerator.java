package src.algorithm;

import src.instances.Vehicle;
import src.scheduler.*;
import java.util.*;

public class RoutingTableGenerator {
    List<RouteTable> routingTables;

    public RoutingTableGenerator() {
        this.routingTables = new ArrayList<>();
    }

    public List<RouteTable> generateRoutingTables(List<Route> routes) {
        Map<Vehicle, List<Route>> vehicleRoutesMap = new HashMap<>();

        for (Route route : routes) {
            Vehicle vehicle = route.getVehicle();
            if (!vehicleRoutesMap.containsKey(vehicle)) {
                vehicleRoutesMap.put(vehicle, new ArrayList<>());
            }
            vehicleRoutesMap.get(vehicle).add(route);
        }

        List<RouteTable> routingTables = new ArrayList<>();

        for (Map.Entry<Vehicle, List<Route>> entry : vehicleRoutesMap.entrySet()) {
            Vehicle vehicle = entry.getKey();
            List<Route> vehicleRoutes = entry.getValue();

            RouteTable routingTable = new RouteTable(vehicle, vehicleRoutes);
            routingTables.add(routingTable);
        }

        return routingTables;
    }

}
