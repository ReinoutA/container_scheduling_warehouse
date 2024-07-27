package src.scheduler;

import src.instances.Location;
import src.instances.LocationType;
import src.instances.Vehicle;

import java.util.List;

public class RoutingSchedule2 {

    private List<RouteTable> routingTables;

    public RoutingSchedule2(List<RouteTable> routingTables) {
        this.routingTables = routingTables;
    }

    public void printRoutingSchedule() {
        int routingTableIndex = 1;
        StringBuilder sb = new StringBuilder();
        sb.append("ROUTING SCHEDULE: \n");
        for (RouteTable routingTable : routingTables) {
            sb.append(routingTableIndex).append(") ").append(routingTable);
            sb.append("\n");
            routingTableIndex++;
        }
        //System.out.println(sb.toString());
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("%vehicle;startx;starty;starttime;endx;endy;endtime;box;rel-boxes;operation;possible-conflict-interval\n");

        for (RouteTable routingTable : routingTables) {
            List<Route> routes = routingTable.getRoutes();
            for (Route route : routes) {
                Vehicle vehicle = route.getVehicle();
                Location startLocation = route.getPath().getSourceNode().getLocation();
                Location endLocation = route.getPath().getDestinationNode().getLocation();

                String box = route.getTransportRequest().getBoxID();
                List<String> boxIDs = route.getTransportRequest().getBoxIDs();
                OperationType operation = route.getOperationType();

                sb.append(vehicle.getName()).append(";");
                sb.append(startLocation.getX()).append(";");
                sb.append(startLocation.getY()).append(";");
                sb.append(route.getRouteStartTime()).append(";");
                sb.append(endLocation.getX()).append(";");
                sb.append(endLocation.getY()).append(";");
                sb.append(route.getRouteEndTime()).append(";");
                sb.append(box).append(";");
                sb.append(boxIDs).append(";");
                sb.append(operation);
                if (operation != OperationType.VEHICLE_INIT && route.getPath().getDestinationNode().getLocation()
                        .getLocationType() != LocationType.BUFFERPOINT) {
                    int lowerBound = route.getRouteEndTime() - vehicle.getLoadDuration() * route.getTransportRequest().getBoxIDs().size();
                    sb.append(";[").append(lowerBound).append(",").append(route.getRouteEndTime()).append("]");
                }
                sb.append("\n");
            }
        }

        return sb.toString();
    }
}
