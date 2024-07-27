package src.scheduler;

import src.instances.*;
import src.networkgraph.*;

import java.util.*;


public class Route {
    private Vehicle vehicle;
    private final Path validPath;
    private final TransportRequest transportRequest;
    private final OperationType operationType;
    private int routeStartTime;
    private int routeEndTime;

    public Route(Vehicle vehicle, Path path, TransportRequest transportRequest, OperationType operationType, int routeStartTime, int routeEndTime) {
        this.vehicle = vehicle;
        this.validPath = path;
        this.transportRequest = transportRequest;
        this.operationType = operationType;
        this.routeEndTime = routeEndTime;
        this.routeStartTime = routeStartTime;
    }

    public void setRouteStartTime(int routeStartTime) {
        this.routeStartTime = routeStartTime;
    }

    public void setRouteEndTime(int routeEndTime) {
        this.routeEndTime = routeEndTime;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public Path getPath() {
        return validPath;
    }

    public TransportRequest getTransportRequest() {
        return transportRequest;
    }

    public OperationType getOperationType() {
        return operationType;
    }

    public int getRouteEndTime() {
        return routeEndTime;
    }

    public int getRouteStartTime() {
        return routeStartTime;
    }

    public String getCSVString() {
        StringBuilder sb = new StringBuilder();

        Vehicle vehicle = getVehicle();
        Location startLocation = validPath.getSourceNode().getLocation();
        Location endLocation = validPath.getDestinationNode().getLocation();

        sb.append(vehicle.getName()).append(";");
        sb.append(startLocation.getX()).append(";");
        sb.append(startLocation.getY()).append(";");
        sb.append(routeStartTime).append(";");
        sb.append(endLocation.getX()).append(";");
        sb.append(endLocation.getY()).append(";");
        sb.append(routeEndTime).append(";");
        sb.append(transportRequest.getBoxID()).append(";");
        if(operationType == OperationType.VEHICLE_INIT) sb.append("START");
        else if (operationType == OperationType.PICKUP) sb.append("PU");
        else if (operationType == OperationType.PLACEMENT) sb.append("PL");
        else if (operationType == OperationType.RELOCATION_HEEN) sb.append("PU");
        //else if (operationType == OperationType.RELOCATION_HEEN) sb.append("RELOCATION_HEEN");
        else if (operationType == OperationType.RELOCATION_TERUG) sb.append("PL");
        //else if (operationType == OperationType.RELOCATION_TERUG) sb.append("RELOCATION_TERUG");
//        sb.append(operationType);
        sb.append("\n");
        return sb.toString();
    }

    /*
    public List<Route> split() {
        List<Route> output = new ArrayList<>();
        List<String> boxIDs = transportRequest.getBoxIDs();
        int routeEndTimeLocal = routeEndTime - (boxIDs.size() * vehicle.getLoadDuration());

        for (int i = 0; i < boxIDs.size(); i++) {
            String boxID = boxIDs.get(i);

            if (operationType == OperationType.PICKUP || operationType == OperationType.RELOCATION_TERUG) {
                TransportRequest newRequest = new TransportRequest(transportRequest);
                newRequest.setBoxID(boxID);
                newRequest.setBoxIDs(new ArrayList<>());
                newRequest.addBoxID(boxID);
                if (i == 0) {
                    Route newRoute = new Route(vehicle, validPath, newRequest, operationType,
                            routeStartTime + (i * vehicle.getLoadDuration()),
                            routeStartTime + ((i + 1) * vehicle.getLoadDuration()));
                    output.add(newRoute);
                } else {
                    Path newPath = new Path(validPath.getDestinationNode(), validPath.getDestinationNode());
                    Route newRoute = new Route(vehicle, newPath, newRequest, operationType,
                            routeStartTime + (i * vehicle.getLoadDuration()), routeEndTime);
                    output.add(newRoute);
                }
            }
            else if (operationType == OperationType.PLACEMENT || operationType == OperationType.RELOCATION_HEEN) {
                TransportRequest newRequest = new TransportRequest(transportRequest);
                newRequest.setBoxID(boxID);
                newRequest.setBoxIDs(new ArrayList<>());
                newRequest.addBoxID(boxID);
                if (i == 0) {
                    Route newRoute = new Route(vehicle, validPath, newRequest, operationType,
                            routeStartTime, routeEndTimeLocal + vehicle.getLoadDuration());
                    output.add(newRoute);
                } else {
                    Path newPath = new Path(validPath.getDestinationNode(), validPath.getDestinationNode());
                    Route newRoute = new Route(vehicle, newPath, newRequest, operationType,
                            routeEndTimeLocal + (i * vehicle.getLoadDuration()),
                            routeEndTimeLocal + ((i + 1) * vehicle.getLoadDuration()));
                    output.add(newRoute);
                }
            }
        }
        return output;
    }
    */

    public List<Route> split() {
        List<Route> output = new ArrayList<>();
        List<String> boxIDs = transportRequest.getBoxIDs();
        int routeEndTimeLocal = routeEndTime - (boxIDs.size() * vehicle.getLoadDuration());

        for (int i = 0; i < boxIDs.size(); i++) {
            String boxID = boxIDs.get(i);
            TransportRequest newRequest = new TransportRequest(transportRequest);
            newRequest.setBoxID(boxID);
            newRequest.setBoxIDs(new ArrayList<>());
            if (i == 0) {
                Route newRoute = new Route(vehicle, validPath, newRequest, operationType,
                        routeStartTime, routeEndTimeLocal + vehicle.getLoadDuration());
                output.add(newRoute);
            } else {
                Path newPath = new Path(validPath.getDestinationNode(), validPath.getDestinationNode());
                Route newRoute = new Route(vehicle, newPath, newRequest, operationType,
                        routeEndTimeLocal + (i * vehicle.getLoadDuration()),
                        routeEndTimeLocal + ((i + 1) * vehicle.getLoadDuration()));
                output.add(newRoute);
            }
        }
        return output;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Route for ");
        sb.append(transportRequest);
        sb.append(" (Operation: ");
        sb.append(operationType);
        sb.append(")\n");

        if (validPath != null) {
            sb.append("Path:\n");
            for (Edge edge : validPath.getAdjacentEdgesFromSourceToDestination()) {
                sb.append(" - ");
                sb.append(edge);
                sb.append("\n");
            }
        } else {
            sb.append("Empty Route (0-Path)\n");
        }

        sb.append("Start Time: ");
        sb.append(routeStartTime);
        sb.append("\n");
        sb.append("End Time: ");
        sb.append(routeEndTime);
        sb.append("\n");

        return sb.toString();
    }



}
