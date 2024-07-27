package src.algorithm;

import src.instances.*;
import src.networkgraph.*;

import java.util.*;

public class Preprocessor {
    public final LocationManager locationManager;

    public Preprocessor(LocationManager locationManager) {
        this.locationManager = locationManager;
    }

    public void makeReservations(List<TransportRequest> transportRequests, Graph graph){
        for(TransportRequest transportRequest : transportRequests){
            graph.getNodeWithLocation(locationManager.findLocationByName(transportRequest.getPlaceLocationName())).getLocation().addAmountOfReservations(1);
        }

        for(Location l : locationManager.locations){
            //System.out.println("Location " + l.getName() + " has " + l.getAmountOfReservations() + " reservations");
        }
    }

    public List<Location> findUnusedLocations(List<TransportRequest> transportRequests, Graph graph){
        List<Location> unusedLocations = new ArrayList<>(locationManager.locations);

        for(TransportRequest t : transportRequests){
            unusedLocations.remove(graph.getNodeWithLocationName(t.getPlaceLocationName()).getLocation());
            unusedLocations.remove(graph.getNodeWithLocationName(t.getPickupLocationName()).getLocation());
        }
        return unusedLocations;
    }

    public List<String> findUsedBoxes(List<TransportRequest> transportRequests){
        List<String> boxes = new ArrayList<>();
        for(TransportRequest t : transportRequests){
            boxes.add(t.getBoxID());
        }
        return boxes;
    }

    public List<String> findUnusedBoxes(){
        List<String> boxes = new ArrayList<>();
        for(Location l : locationManager.locations){
            for(Box b : l.getBoxes()){
                boxes.add(b.getBoxID());
            }
        }
        return boxes;
    }

    public List<TransportRequest> getBufferToStackTransportRequests(List<TransportRequest> transportRequests){
        List<TransportRequest> bufferToStackTransportRequests = new ArrayList<>();

        for(TransportRequest t : transportRequests) {
            if(Objects.requireNonNull(locationManager.findLocationByName(t.getPickupLocationName())).isBufferPoint()){
                bufferToStackTransportRequests.add(t);
            }
        }

        return bufferToStackTransportRequests;
    }

    public List<TransportRequest> getStackToBufferTransportRequests(List<TransportRequest> transportRequests){
        List<TransportRequest> stackToBufferTransportRequests = new ArrayList<>();

        for(TransportRequest t : transportRequests) {
            if(Objects.requireNonNull(locationManager.findLocationByName(t.getPlaceLocationName())).isBufferPoint()) {
                stackToBufferTransportRequests.add(t);
            }
        }

        return stackToBufferTransportRequests;
    }

    public void printBufferToStackRequests(List<TransportRequest> bufferToStackRequests) {
        System.out.println("Amount of Buffer->Stack requests " + bufferToStackRequests.size());
        System.out.println("Amount of unique Buffer->Stack combinations: " + countUniqueCombinations(bufferToStackRequests));
    }

    public int countUniqueCombinations(List<TransportRequest> transportRequests) {
        Set<String> uniqueCombinations = new HashSet<>();

        for (TransportRequest request : transportRequests) {
            String combination = request.getPickupLocationName() + "-" + request.getPlaceLocationName();
            uniqueCombinations.add(combination);
        }

        return uniqueCombinations.size();
    }

}
