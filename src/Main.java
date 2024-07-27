package src;

import src.factories.*;
import src.instances.*;
import src.networkgraph.*;
import src.scheduler.*;
import java.io.IOException;
import java.util.*;
import src.algorithm.*;

public class Main {
    // False: First instances
    // True : New instances

    public static boolean NEW_FORMAT = true;
    public static boolean SPLIT = true;

    // BASIC
    //private static String PATH = "container_reinout/resources/basic/I3_3_1.json";

    // ADVANCED
    //private static String PATH = "container_reinout/resources/advanced/I20_20_2_2_8b2.json";
    //private static String PATH = "container_reinout/resources/advanced/I30_100_1_1_10.json";
    //private static String PATH = "container_reinout/resources/advanced/I30_100_3_3_10.json";
    //private static String PATH = "container_reinout/resources/advanced/I30_200_3_3_10.json";
    //private static String PATH = "container_reinout/resources/advanced/I100_50_2_2_8b2.json";
    ///private static String PATH = "container_reinout/resources/advanced/I100_120_2_2_8b2.json";


    // ULTRA
    //private static String PATH = "C:/ELICT4/Algoritmes_voor_beslissingsondersteuning/container_reinout/container_reinout/resources/ultra/I100_500_3_1_20b2.json";
    //private static String PATH = "C:/ELICT4/Algoritmes_voor_beslissingsondersteuning/container_reinout/container_reinout/resources/ultra/I100_500_3_5_20.json"; // TODO: FIX LOADING DURATION
    //private static String PATH = "C:/ELICT4/Algoritmes_voor_beslissingsondersteuning/container_reinout/container_reinout/resources/ultra/I100_800_1_1_20b2.json";
    //private static String PATH = "C:/ELICT4/Algoritmes_voor_beslissingsondersteuning/container_reinout/container_reinout/resources/ultra/I100_800_3_1_20b2.json"; // TODO: FIX LOADING DURATION
    public static void main(String[] args) throws IOException {
        String OUTPUT_PATH = null;
        String PATH = null;
        System.out.println("Use: java -jar algo.jar <input_path> <output_path>");
        if(args.length == 2) {
            PATH = args[0];
            OUTPUT_PATH = args[1];
        }
        VehicleFactory vehicleFactory = new VehicleFactory();
        TransportRequestFactory transportRequestFactory = new TransportRequestFactory();
        LocationFactory locationFactory = new LocationFactory();
        GraphFactory graphFactory = new GraphFactory();

        List<TransportRequest> transportRequests = transportRequestFactory.createTransportRequestsFromJson(PATH);
        List<Vehicle> vehicles = vehicleFactory.createVehiclesFromJson(PATH);
        List<Location> locations = locationFactory.createLocationsFromJson(PATH, transportRequests);
        Graph graph = graphFactory.convertLocationsToGraph(locations);
        System.out.println("Amount of requests : " + transportRequests.size());
        System.out.println("Amount of vehicles : " + vehicles.size());
        System.out.println("Amount of locations : " + locations.size());

        printGraph(graph);
        LocationManager locationManager = new LocationManager(locations);
        RoutingAlgorithm routingAlgorithm = new RoutingAlgorithm(locationManager, graph, vehicles);
        Preprocessor preprocessor = new Preprocessor(locationManager);
        preprocessor.makeReservations(transportRequests, graph);
        RoutingTableGenerator routingTableGenerator = new RoutingTableGenerator();
        printInstances(vehicles, transportRequests, locations);
        List<Route> routes = routingAlgorithm.calculateRoutes(transportRequests);
        //System.out.println("Amount of Routes: " + routes.size());
        routes = routingAlgorithm.solveConflicts(routes);

        // BRECHT
        if(SPLIT) {
            List<Route> newRoutes = new ArrayList<>();

            for (Route r : routes) {
                newRoutes.addAll(r.split());
            }
            routes = newRoutes;
            // BRECHT
        }
        List<RouteTable> routingTables = routingTableGenerator.generateRoutingTables(routes);
        RoutingSchedule routingSchedule = new RoutingSchedule(routingTables);
        System.out.println(routingSchedule);
        System.out.println(locationManager);

        //OutputWriter.writeScheduleAndLocations("schedule.txt", routingSchedule, locationManager);
        //Validator.validateSchedule("schedule.txt");
        validateBoxes(transportRequests, locationManager);
        validateTeleportations(routingSchedule);
        validateRouteTimes(routingSchedule);
        if(args.length == 2) {
            OutputWriter.writeScheduleAndLocations(OUTPUT_PATH, routingSchedule, locationManager);
        }
    }

    public static void printInstances(List<Vehicle> vehicles, List<TransportRequest> transportRequests,
            List<Location> locations) {
        for (Vehicle v : vehicles)
            System.out.println(v);
        for (TransportRequest t : transportRequests)
            System.out.println(t);
        for (Location l : locations)
            System.out.println(l);

        System.out.println("NETWORK:");
        System.out.println(" - VEHICLE_SPEED: " + Vehicle.VEHICLE_SPEED);
        System.out.println(" - LOAD_DURATION: " + Vehicle.LOAD_DURATION);
    }

    public static void printGraph(Graph graph) {
        System.out.println(graph);
    }

    public static void validateBoxes(List<TransportRequest> transportRequests, LocationManager locationManager) {
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
                System.err.println("[BOX ERROR]: " + t.getBoxID() + " wasn't found at the location " + l.getName());
                counter++;
            }
        }
        System.err.println("Amount of box errors: " + counter);
    }

    public static void validateTeleportations(RoutingSchedule routingSchedule) {
        int counter = 0;
        for(RouteTable rt : routingSchedule.getRoutingTables()) {
            List<Route> routes = rt.getRoutes();
            for(int i = 0; i < routes.size()-1; i++) {
                Route route1 = routes.get(i);
                Route route2 = routes.get(i+1);
                if(route1.getPath().getDestinationNode().getLocation() != route2.getPath().getSourceNode().getLocation()) {
                    System.err.println("[TELEPORTATION ERROR]: from " + route1.getPath().getDestinationNode().getLocation().getName() + ", to " + route2.getPath().getSourceNode().getLocation().getName());
                }
            }
        }
        System.err.println("Amount of teleportation errors: " + counter);
    }

    public static void validateRouteTimes(RoutingSchedule routingSchedule) {
        int counter = 0;
        for(RouteTable rt : routingSchedule.getRoutingTables()) {
            List<Route> routes = rt.getRoutes();
            for(int i = 0; i < routes.size()-1; i++) {
                Route route1 = routes.get(i);
                Route route2 = routes.get(i+1);
                if(route1.getRouteEndTime() > route2.getRouteStartTime()) {
                    System.err.println("[ROUTE TIME ERROR]: from " + route1.getPath().getDestinationNode().getLocation().getName() + ", to " + route2.getPath().getSourceNode().getLocation().getName());
                    System.err.println(route1.getRouteEndTime() + ", " + route2.getRouteStartTime());
                }
            }
        }
        System.err.println("Amount of teleportation errors: " + counter);
    }
}
