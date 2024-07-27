package src.algorithm;

import src.instances.*;
import src.networkgraph.*;
import src.scheduler.*;

import java.util.*;
import java.util.stream.*;

public class RoutingAlgorithm {
    private final LocationManager locationManager;
    private final List<Vehicle> vehicles;
    private final List<Pair> intervals;
    private final Graph graph;
    private final List<Route> routes = new ArrayList<>();
    private int currentTime = 0;
    private final Preprocessor preprocessor;


    public RoutingAlgorithm(LocationManager locationManager, Graph graph, List<Vehicle> vehicles){
        this.locationManager = locationManager;
        this.intervals = new ArrayList<>();
        this.graph = graph;
        this.preprocessor = new Preprocessor(locationManager);
        this.vehicles = vehicles;
    }

    public List<Route> calculateRoutes(List<TransportRequest> transportRequests){
        List<TransportRequest> stackToBufferTransportRequests = preprocessor.getStackToBufferTransportRequests(transportRequests);
        //System.out.println("amount of stackToBufferTransportRequests: " + stackToBufferTransportRequests.size());
        List<TransportRequest> stackToStackTransportRequests =  calculateMakeSpaceTransportRequests();
        //System.out.println("amount of stackToStackTransportRequests: " + stackToStackTransportRequests.size());
        List<TransportRequest> bufferToStackTransportRequests = preprocessor.getBufferToStackTransportRequests(transportRequests);
        //System.out.println("amount of bufferToStackTransportRequests: " + bufferToStackTransportRequests.size());
        calculateStackToBufferRoutes(stackToBufferTransportRequests);


        List<TransportRequest> transportRequestsTobeFixed = searchMistakes(stackToBufferTransportRequests, locationManager);
        while(!transportRequestsTobeFixed.isEmpty()) {
            calculateStackToBufferRoutes(transportRequestsTobeFixed);
            transportRequestsTobeFixed = searchMistakes(stackToBufferTransportRequests, locationManager);
        }



        calculateMakeSpaceRoutes(stackToStackTransportRequests);
        calculateBufferToStackRoutes(bufferToStackTransportRequests);
        preprocessor.printBufferToStackRequests(bufferToStackTransportRequests);
        return routes;
    }

    public List<TransportRequest> searchMistakes(List<TransportRequest> transportRequests, LocationManager locationManager) {
        List<TransportRequest> transportRequestsFix = new ArrayList<>();
        int counter = 0;
        for(TransportRequest t : transportRequests) {
            Location l = locationManager.findLocationByName(t.getPlaceLocationName());
            boolean found = false;
            assert l != null;
            if(t.getPlaceLocationName().equals(l.getName())) {
                for(Box b : l.getBoxes()) {
                    if (b.getBoxID().equals(t.getBoxID())) {
                        found = true;
                        break;
                    }
                }
            }
            if(!found) {
                String pickupLocationName = null;
                //System.err.println("[BOX ERROR]: " + t.getBoxID() + " wasn't found at the location " + l.getName());
                for(Location l2 : locationManager.locations) {
                    for(Box b : l2.getBoxes()) {
                        if(b.getBoxID().equals(t.getBoxID())) {
                            pickupLocationName = l2.getName();
                        }
                    }
                }
                TransportRequest transportRequest = new TransportRequest(-1, pickupLocationName, l.getName(), t.getBoxID());
                transportRequestsFix.add(transportRequest);
                counter++;
            }
        }
        //System.err.println("Amount of box errors: " + counter);
        return transportRequestsFix;
    }


    public void calculateStackToBufferRoutes(List<TransportRequest> stackToBufferTransportRequests){
        int vehicleIndex = 0;
        int vehicle_start_time;
        Route initRoute;
        for(TransportRequest r : stackToBufferTransportRequests){
            Vehicle v = vehicles.get(vehicleIndex);
            currentTime = v.getBusyUntil();
            //System.out.println("CurrentTime: " + currentTime);
            if(!v.isBusy(currentTime+1)){
                vehicle_start_time = currentTime;

                if(isFirstVehicleInit(v, routes)){
                    initRoute = generateVehicleInitRoute(new Location("init " + v.getName(), v.getXCoordinate(), v.getYCoordinate()), v, r);
                    updateVehiclePositionAfterInitRoute(v, initRoute.getPath().getDestinationNode().getLocation());
                }

                Location pickupLocation = graph.getNodeWithLocationName(r.getPickupLocationName()).getLocation();
                Location placementLocation = graph.getNodeWithLocationName(r.getPlaceLocationName()).getLocation();
                List<Box> boxes = new ArrayList<>();

                int amountOfBoxesToBeRelocated = pickupLocation.getAmountOfBoxesAboveBox(r.getBoxID());
                if(pickupLocation.isBufferPoint()) {
                    amountOfBoxesToBeRelocated = 0;
                }
                // ALLES KAN IN 1X GEDRAGEN WORDEN DOOR DE VEHICLE, EN GEEN PICKUP RELOCATIONS NODIG
                if(amountOfBoxesToBeRelocated == 0 && r.getBoxIDs().size() <= v.getCapacity()){
                    Location currentLocation = locationManager.findLocationByCoordinates(v.getXCoordinate(), v.getYCoordinate());
                    generatePickupRoute(currentLocation, pickupLocation, r.getBoxIDs().size(), v, r);

                    if(pickupLocation.isStorageStack()) {
                        boxes = locationManager.removeBoxesFromLocation(pickupLocation, r.getBoxIDs().size());
                        updateBoxesLocationThatAppearInRequests(stackToBufferTransportRequests);    // NEW
                    }else{
                        for(String b : r.getBoxIDs()) {
                            Box box = locationManager.removeBoxFromLocation(pickupLocation, b);
                            boxes.add(box);
                        }
                        updateBoxesLocationThatAppearInRequests(stackToBufferTransportRequests);    // NEW
                    }
                    generatePlacementRoute(pickupLocation, placementLocation, boxes.size(), v, r);

                    locationManager.addMultipleBoxesToLocation(placementLocation, boxes, currentTime);
                    updateBoxesLocationThatAppearInRequests(stackToBufferTransportRequests);    // NEW
                    // PICKUPRELOCATIONS NODIG, KAN IN 1x GEDRAGEN WORDEN DOOR VEHICLE
                }else if(amountOfBoxesToBeRelocated > 0 && amountOfBoxesToBeRelocated <= v.getCapacity()){
                    Location currentLocation = locationManager.findLocationByCoordinates(v.getXCoordinate(), v.getYCoordinate());

                    generatePickupRoute(currentLocation, pickupLocation, v.getCapacity(), v, r);

                    generatePickupRelocationRoutes(pickupLocation, amountOfBoxesToBeRelocated, v, r);
                    updateBoxesLocationThatAppearInRequests(stackToBufferTransportRequests);            // NEW
                    boxes = locationManager.removeBoxesFromLocation(pickupLocation, r.getBoxIDs().size());
                    generatePlacementRoute(pickupLocation, placementLocation, r.getBoxIDs().size(), v, r);
                    locationManager.addMultipleBoxesToLocation(placementLocation, boxes, currentTime);

                }else{ // TE RELOCEREN BOXES KUNNEN NIET IN 1X GEDRAGEN WORDEN, DE BOXES VAN DE REQUEST WEL
                    if (amountOfBoxesToBeRelocated > 0) {
                        int first_relocation_amount_of_boxes_to_be_relocated = Math.min(amountOfBoxesToBeRelocated, v.getCapacity());
                        Location currentLocation = locationManager.findLocationByCoordinates(v.getXCoordinate(), v.getYCoordinate());

                        generatePickupRoute(currentLocation, pickupLocation, first_relocation_amount_of_boxes_to_be_relocated, v, r);

                        while (amountOfBoxesToBeRelocated > 0) {
                            int vehicleCapacity = v.getCapacity();
                            int amountOfBoxesToBeRelocated_i = Math.min(amountOfBoxesToBeRelocated, vehicleCapacity);

                            // Maak een nieuwe TransportRequest voor de huidige batch
                            TransportRequest r_i = new TransportRequest(-1, pickupLocation.getName(), placementLocation.getName(), r.getBoxID());

                            generatePickupRelocationRoutes(pickupLocation, amountOfBoxesToBeRelocated_i, v, r_i);
                            amountOfBoxesToBeRelocated -= amountOfBoxesToBeRelocated_i;
                        }

                        boxes = locationManager.removeBoxesFromLocation(pickupLocation, r.getBoxIDs().size());
                        generatePlacementRoute(pickupLocation, placementLocation, r.getBoxIDs().size(), v, r);
                        //System.out.println("r.id :" + r.getID());
                        locationManager.addMultipleBoxesToLocation(placementLocation, boxes, currentTime);
                        updateBoxesLocationThatAppearInRequests(stackToBufferTransportRequests);            // NEW
                    }
                }

                v.addBusyInterval(v, vehicle_start_time, currentTime, placementLocation);
                v.setBusyUntil(currentTime);
                vehicleIndex = (vehicleIndex + 1) % vehicles.size();
                updateVehiclePositionAfterRequest(v, placementLocation);
                updateBoxesLocationThatAppearInRequests(stackToBufferTransportRequests);
            }
            //System.out.println("Request " + r.getID() + " has been handled");
        }
        remove_bufferpoint_loading_unloading_from_intervals();
    }

    // stack->stack should only happen when ALL stack->buffer requests are complete
    public void calculateMakeSpaceRoutes(List<TransportRequest> stackToStackTransportRequests){
        int vehicleIndex = 0;
        int highestBusyUntil = 0;
        int vehicle_start_time;
        for (Vehicle v : vehicles) {
            int busyUntil = v.getBusyUntil();
            if (busyUntil > highestBusyUntil) {
                highestBusyUntil = busyUntil;
            }
        }

        for(Vehicle v : vehicles){
            v.setBusyUntil(highestBusyUntil);
        }

        for(TransportRequest r : stackToStackTransportRequests){
            Vehicle v = vehicles.get(vehicleIndex);
            currentTime = v.getBusyUntil();
            //System.out.println("CurrentTime: " + currentTime);
            if(!v.isBusy(currentTime+1)){
                vehicle_start_time = currentTime;
                Location pickupLocation = graph.getNodeWithLocationName(r.getPickupLocationName()).getLocation();
                Location placementLocation = graph.getNodeWithLocationName(r.getPlaceLocationName()).getLocation();
                List<Box> boxes = new ArrayList<>();
                generatePickupRoute(locationManager.findLocationByCoordinates(v.getXCoordinate(), v.getYCoordinate()), pickupLocation, r.getBoxIDs().size(), v, r );
                if(pickupLocation.isStorageStack()) {
                    // Top boxes are being removed
                    boxes = locationManager.removeBoxesFromLocation(pickupLocation, r.getBoxIDs().size());
                }else{
                    // BufferPoint : no LIFO so we can choose which boxes we pickup
                    boxes.add(locationManager.removeBoxFromLocation(pickupLocation, r.getBoxID()));
                }
                generatePlacementRoute(pickupLocation, placementLocation, boxes.size(), v, r);
                locationManager.addMultipleBoxesToLocation(placementLocation, boxes, currentTime);

                v.addBusyInterval(v, vehicle_start_time, currentTime, placementLocation);
                v.setBusyUntil(currentTime);
                vehicleIndex = (vehicleIndex + 1) % vehicles.size();
                updateVehiclePositionAfterRequest(v, placementLocation);
                updateBoxesLocationThatAppearInRequests(stackToStackTransportRequests);
            }
        }
    }

    public List<TransportRequest> calculateMakeSpaceTransportRequests(){
        List<Location> locations = locationManager.locations;
        List<Location> verbodenBoxesNaarTeVerplaatsen = new ArrayList<>();
        List<Location> toegestaanOmBoxesNaarTeVerplaatsen = new ArrayList<>();
        List<TransportRequest> stackToStackTransportRequests = new ArrayList<>();

        int minVehicleCapacity = Integer.MAX_VALUE;

        for (Vehicle vehicle : vehicles) {
            int capacity = vehicle.getCapacity();
            if (capacity < minVehicleCapacity) {
                minVehicleCapacity = capacity;
            }
        }

        for(Location l : locations){
            if(l.getAmountOfReservations() > l.getCapacityLeft() && l.isStorageStack()){
                verbodenBoxesNaarTeVerplaatsen.add(l);
            }else if(l.getCapacityLeft() - l.getAmountOfReservations() > 0 && l.isStorageStack()){
                toegestaanOmBoxesNaarTeVerplaatsen.add(l);
            }
        }

        for(Location l : verbodenBoxesNaarTeVerplaatsen){
            int plaatsTekort = l.getAmountOfReservations() - l.getCapacityLeft();
            //System.out.println("Plaatstekort op locatie: " + l.getName() + " is " + plaatsTekort);
            int addedBoxes = 0;
            for(Location l2 : toegestaanOmBoxesNaarTeVerplaatsen){
                TransportRequest r = new TransportRequest(-1, l.getName(), l2.getName(), "S2S");
                r.removeBoxId("S2S");
                int amountOfBoxes = Math.min(minVehicleCapacity, Math.min(plaatsTekort, l2.getCapacityLeft() - l2.getAmountOfReservations() - l2.getStackToStackReservations()));
                if(amountOfBoxes > 0) {
                    plaatsTekort -= amountOfBoxes;
                    l2.addstackToStackReservations(amountOfBoxes);
                    for (int i = 0; i < amountOfBoxes; i++) {
                        String boxId = l.getBoxes().get(l.getBoxes().size()-i-1-addedBoxes).getBoxID();
                        addedBoxes++;
                        //System.out.println("Adding boxID " +boxId + " to transportrequest");
                        r.addBoxID(boxId);
                    }
                    stackToStackTransportRequests.add(r);
                }
            }
        }
        return stackToStackTransportRequests;
    }

    public void calculateBufferToStackRoutes(List<TransportRequest> bufferToStackTransportRequests){
        // This method can be re-used since placement relocations are not needed for buffer->stack after you made space
        calculateMakeSpaceRoutes(bufferToStackTransportRequests);

    }

    public Route generateVehicleInitRoute(Location initialVehicleLocation, Vehicle v, TransportRequest transportRequest){
        Path vehicleToNodePath = graph.findShortestPathToNearestNodeOnY(initialVehicleLocation, graph.getNodes());
        Route vehicleToNodeRoute = new Route(v, vehicleToNodePath, transportRequest, OperationType.VEHICLE_INIT, currentTime, currentTime + vehicleToNodePath.getTotalPathCost());
        currentTime = currentTime + vehicleToNodePath.getTotalPathCost();
        routes.add(vehicleToNodeRoute);
        return vehicleToNodeRoute;
    }

    public void generatePlacementRoute(Location currentLocation, Location placementLocation, int amountOfBoxesToBePlaced, Vehicle v, TransportRequest transportRequest){
        Path placementPath = graph.findShortestPath(currentLocation, placementLocation);
        Route placementRoute = new Route(v, placementPath, transportRequest, OperationType.PLACEMENT, currentTime, currentTime + placementPath.getTotalPathCost() + amountOfBoxesToBePlaced * v.getLoadDuration());
        currentTime = currentTime + placementPath.getTotalPathCost() + amountOfBoxesToBePlaced * v.getLoadDuration();
        routes.add(placementRoute);
    }

    public void generatePickupRoute(Location currentLocation, Location pickupLocation, int amountOfBoxesToBePickedUp, Vehicle v, TransportRequest transportRequest){
        Path pickupPath = graph.findShortestPath(currentLocation, pickupLocation);
        //System.out.println("amountOfBoxesToBePickedUp " + amountOfBoxesToBePickedUp + " en " + transportRequest.getBoxIDs().size());
        Route pickupRoute = new Route(v, pickupPath, transportRequest, OperationType.PICKUP, currentTime, currentTime + pickupPath.getTotalPathCost() +  transportRequest.getBoxIDs().size()*v.getLoadDuration());
        currentTime = currentTime + pickupPath.getTotalPathCost() + transportRequest.getBoxIDs().size() * v.getLoadDuration();
        routes.add(pickupRoute);
    }

    public void generatePickupRelocationRoutes(Location pickupLocation, int boxesToBeRelocatedAmount, Vehicle v, TransportRequest transportRequest){
        List<Location> locationsSortedByCapacityLeft = sortStorageStacksByCapacity();

        // Relocation can happen in one go
        if(locationsSortedByCapacityLeft.get(0).getCapacityLeft() >= boxesToBeRelocatedAmount && locationsSortedByCapacityLeft.get(0) != pickupLocation){
            generatePickupRelocationRoutesOneGo(pickupLocation, locationsSortedByCapacityLeft.get(0), v, boxesToBeRelocatedAmount, transportRequest);
        }else if(locationsSortedByCapacityLeft.get(1).getCapacityLeft() >= boxesToBeRelocatedAmount){
            generatePickupRelocationRoutesOneGo(pickupLocation, locationsSortedByCapacityLeft.get(1), v, boxesToBeRelocatedAmount, transportRequest);
        }else{
            List<Location> relocationLocations = locationManager.findRelocationLocations(pickupLocation, boxesToBeRelocatedAmount);
            generatePickupRelocationRoutesMultipleGos(pickupLocation,  relocationLocations, v, boxesToBeRelocatedAmount, transportRequest);
        }
    }

    public void generatePickupRelocationRoutesMultipleGos(Location pickupLocation, List<Location> relocationLocations, Vehicle v, int amountOfBoxes, TransportRequest transportRequest) {
        Location currentLocation = pickupLocation;
        List<Box> boxesToBeRelocated = locationManager.removeBoxesFromLocation(pickupLocation, amountOfBoxes);
        for (int i = 0; i < relocationLocations.size(); i++) {
            Location l = relocationLocations.get(i);
            int capacityLeft = l.getCapacityLeft();
            int boxesToMove = Math.min(amountOfBoxes, capacityLeft);

            List<Box> boxesToBeRelocatedSubset = new ArrayList<>();
            for(int b = 0; b < boxesToMove; b++){
                Box box = boxesToBeRelocated.remove(boxesToBeRelocated.size()-1);
                boxesToBeRelocatedSubset.add(box);
            }
            locationManager.addMultipleBoxesToLocation(l, boxesToBeRelocatedSubset, currentTime);
            TransportRequest r = new TransportRequest(-1, currentLocation.getName(), l.getName(), transportRequest.getBoxID());
            for(Box b : boxesToBeRelocatedSubset){
                r.addBoxID(b.getBoxID());
            }

            Path path = graph.findShortestPath(currentLocation, l);

            int loadDuration = (i == relocationLocations.size() - 1) ? boxesToMove * v.getLoadDuration() : capacityLeft * v.getLoadDuration();
            Route route = new Route(v, path, r, OperationType.RELOCATION_HEEN, currentTime, currentTime + path.getTotalPathCost() + loadDuration);
            routes.add(route);
            currentTime = currentTime + path.getTotalPathCost() + loadDuration;
            currentLocation = l;
            amountOfBoxes -= boxesToMove;
        }
        Path path_terug = graph.findShortestPath(currentLocation, pickupLocation);
        Route route_terug = new Route(v, path_terug, transportRequest, OperationType.RELOCATION_TERUG, currentTime, currentTime + path_terug.getTotalPathCost() + transportRequest.getBoxIDs().size() * v.getLoadDuration());
        currentTime = currentTime + path_terug.getTotalPathCost() + transportRequest.getBoxIDs().size() * v.getLoadDuration();
        routes.add(route_terug);
    }

    public void generatePickupRelocationRoutesOneGo(Location pickupLocation, Location relocationLocation, Vehicle v, int amountOfBoxes, TransportRequest transportRequest){
        Path path_heen = graph.findShortestPath(pickupLocation, relocationLocation);
        List<Box> boxes = locationManager.removeBoxesFromLocation(pickupLocation, amountOfBoxes);
        Path path_terug = graph.findShortestPath(relocationLocation, pickupLocation);
        locationManager.addMultipleBoxesToLocation(relocationLocation, boxes, currentTime);
        TransportRequest r = new TransportRequest(-1, pickupLocation.getName(), relocationLocation.getName(), transportRequest.getBoxID());
        r.removeBoxId(transportRequest.getBoxID());
        for(Box b : boxes){
            r.addBoxID(b.getBoxID());
        }

        Route route_heen = new Route(v, path_heen, r, OperationType.RELOCATION_HEEN, currentTime, currentTime + path_heen.getTotalPathCost() + amountOfBoxes * v.getLoadDuration());
        currentTime = currentTime + path_heen.getTotalPathCost() + amountOfBoxes * v.getLoadDuration();
        Route route_terug = new Route(v, path_terug, r, OperationType.RELOCATION_TERUG, currentTime, currentTime + path_terug.getTotalPathCost() + amountOfBoxes * v.getLoadDuration());
        currentTime = currentTime + path_terug.getTotalPathCost() + amountOfBoxes * v.getLoadDuration();

        routes.add(route_heen);
        routes.add(route_terug);

    }

    // Sorted from tons of capacity left to no capacity left
    public List<Location> sortStorageStacksByCapacity() {
        return locationManager.locations.stream()
                .filter(location -> location.getLocationType() == LocationType.STORAGESTACK)
                .sorted(Comparator.comparingInt(Location::getCapacityLeft).reversed())
                .collect(Collectors.toList());
    }

    private boolean isFirstVehicleInit(Vehicle vehicle, List<Route> routes){
        for(Route route : routes){
            if(route.getOperationType() == OperationType.VEHICLE_INIT && route.getVehicle() == vehicle){
                return false;
            }
        }
        return true;
    }

    private void updateVehiclePositionAfterRequest(Vehicle vehicle, Location placementLocation){
        vehicle.setXCoordinate(placementLocation.getX());
        vehicle.setYCoordinate(placementLocation.getY());
    }

    private void updateVehiclePositionAfterInitRoute(Vehicle vehicle, Location placementLocation){
            vehicle.setXCoordinate(placementLocation.getX());
            vehicle.setYCoordinate(placementLocation.getY());
    }

    // There are no loading conflicts at BufferPoints for pickups/placements
    public void remove_bufferpoint_loading_unloading_from_intervals(){
        for(int i = 0; i < intervals.size(); i++){
            if(Objects.requireNonNull(locationManager.findLocationByName((String) intervals.get(i).second)).isBufferPoint()){
                intervals.remove(i);
            }
        }
    }

    private void updateBoxesLocationThatAppearInRequests(List<TransportRequest> transportRequests){
        for(Location l : locationManager.locations){
            for(Box b : l.getBoxes()){
                for(TransportRequest t : transportRequests){
                    for(String boxID : t.getBoxIDs()){
                        if(boxID.equals(b.getBoxID())){
                            t.setPickupLocation(l.getName());
                        }
                    }
                }
            }
        }

    }

    public List<Pair<TimeInterval, String>> findConflictingIntervals(){
        List<Pair<TimeInterval, String>> conflictingIntervals = new ArrayList<>();
        for(int i = 0; i < routes.size(); i++){
            for(int j = 0; j < routes.size(); j++){
                if(routes.get(i) != routes.get(j) && routes.get(i).getPath().getDestinationNode().getLocation() == routes.get(j).getPath().getDestinationNode().getLocation()
                        && !routes.get(i).getPath().getDestinationNode().getLocation().isBufferPoint()){
                    int routeEndTimeUpper_I = routes.get(i).getRouteEndTime();
                    int routeEndTimeLower_I = routes.get(i).getRouteEndTime() - routes.get(i).getVehicle().getLoadDuration() * routes.get(i).getTransportRequest().getBoxIDs().size();
                    TimeInterval operationInterval_I = new TimeInterval(routeEndTimeLower_I, routeEndTimeUpper_I);

                    int routeEndTimeUpper_J = routes.get(j).getRouteEndTime();
                    int routeEndTimeLower_J = routes.get(j).getRouteEndTime() - routes.get(j).getVehicle().getLoadDuration() * routes.get(j).getTransportRequest().getBoxIDs().size();
                    TimeInterval operationInterval_J = new TimeInterval(routeEndTimeLower_J, routeEndTimeUpper_J);

                    if(hasOverlapAtSameTime(operationInterval_I, operationInterval_J)){
                        conflictingIntervals.add(new Pair<>(operationInterval_I, routes.get(i).getPath().getDestinationNode().getLocation().getName()));
                    }
                }
            }
        }
        //System.out.println("FOUND CONFLICTINGINTERVALS: " + conflictingIntervals.size());
        return conflictingIntervals;
    }

    private boolean hasOverlapAtSameTime(TimeInterval interval1, TimeInterval interval2){
        //return interval1.getStartTime() < interval2.getEndTime()&& interval1.getEndTime() > interval2.getStartTime()&& interval1.getStartTime() != interval1.getEndTime()&& interval2.getStartTime() != interval2.getEndTime();
        // TODO CHECK
        return interval1.getStartTime() < interval2.getEndTime()&& interval1.getEndTime() > interval2.getStartTime();
    }

    public List<Route> solveConflicts(List<Route> routes){
        List<Pair<TimeInterval, String>> conflictingIntervals = findConflictingIntervals();
        System.out.println("INITIELE CONFLICT INTERVAL SIZE : " + conflictingIntervals.size());
        while(!conflictingIntervals.isEmpty()){
            for(Route r : routes) {
                int beginTimeOperation = r.getRouteEndTime() - r.getVehicle().getLoadDuration() * r.getTransportRequest().getBoxIDs().size();
                int endTimeOperation = r.getRouteEndTime();
                TimeInterval operationInterval = new TimeInterval(beginTimeOperation, endTimeOperation);

                for (int i = 0; i < conflictingIntervals.size(); i++) {
                    TimeInterval conflictingInterval = conflictingIntervals.get(i).first;
                    if (hasOverlapAtSameTime(operationInterval, conflictingInterval)) {
                        //Random random = new Random();
                        //int rInt = random.nextInt(3, 5);
                        //r.setRouteEndTime(r.getRouteEndTime() + r.getVehicle().getLoadDuration() + rInt); // WAIT LOADINGDURATION SEC
                        r.setRouteEndTime(r.getRouteEndTime() + r.getVehicle().getLoadDuration()); // WAIT LOADINGDURATION SEC

                        conflictingIntervals.remove(i);
                        for (Route r2 : routes) {
                            if (r != r2 && r2.getRouteStartTime() > r.getRouteStartTime() && r.getVehicle() == r2.getVehicle()) {
                                //r2.setRouteStartTime(r2.getRouteStartTime() + r.getVehicle().getLoadDuration() + 1 + rInt);
                                //r2.setRouteEndTime(r2.getRouteEndTime() + r.getVehicle().getLoadDuration() + 1 + rInt);
                                r2.setRouteStartTime(r2.getRouteStartTime() + r.getVehicle().getLoadDuration() + 1);
                                r2.setRouteEndTime(r2.getRouteEndTime() + r.getVehicle().getLoadDuration() + 1);
                                //System.out.println("C: " + r.getTransportRequest().getID() + ", " + r.getVehicle().getName() + ", " + r.getPath().getDestinationNode().getLocation().getBoxes().size() + ", " + r.getPath().getDestinationNode().getLocation().getName());
                                //System.out.println("C: " + r2.getTransportRequest().getID() + ", " + r2.getVehicle().getName() + ", " + r2.getPath().getDestinationNode().getLocation().getBoxes().size() + ", " + r2.getPath().getDestinationNode().getLocation().getName());
                            }
                        }

                    }
                }

            }
            conflictingIntervals = findConflictingIntervals();
            System.out.println("Number of conflicting intervals: " + conflictingIntervals.size());
            System.out.println("Conflicting Intervals: ");
            for(Pair<TimeInterval, String> p : conflictingIntervals) {
                System.out.println("[C]: " + p.first + ", " + p.second);
            }
        }
        return routes;
    }

}
