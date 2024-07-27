package src.algorithm;

import src.instances.*;

import java.util.*;

public class LocationManager {

    List<Location> locations;
    public LocationManager(List<Location> locations) {
        this.locations = locations;
    }
    public Location findLocationByName(String locationName) {
        for (Location location : locations) {
            if (location.getName().equals(locationName)) {
                return location;
            }
        }
        //System.err.println("Location not found");
        return null;
    }

    public Location findLocationByCoordinates(int x, int y) {
        for (Location l : locations) {
            if (l.getX() == x && l.getY() == y) {
                return l;
            }
        }
        //System.err.println("No location found with these coordinates");
        return null;
    }

    public void addMultipleBoxesToLocation(Location location, List<Box> boxes, int atLocationSince) {
        location.addMultipleBoxes(boxes, atLocationSince);
    }

    public List<Box> removeBoxesFromLocation(Location location, int amountOfBoxes) {
        return location.removeMultipleBoxes(amountOfBoxes);
    }

    public Box removeBoxFromLocation(Location location, String boxID) {
        return location.removeBox(boxID);
    }

    public boolean isLocationFull(Location location) {
        if (location.isStorageStack()) {
            return location.getBoxes().size() >= location.getMaxNumboxes();
        }
        return false; // BufferPoint has infinite capacity
    }

    public int getAmountOfBoxesAboveBox(Location location, String boxId) {
        return location.getAmountOfBoxesAboveBox(boxId);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("LocationManager:\n");
        for (Location location : locations) {
            sb.append(" - ").append(location.toString()).append("\n");
        }
        return sb.toString();
    }

    public Location findStackWithEnoughCapacityForRelocation(Location currentLocation, int amount) {
        for (Location l : locations) {
            if (l.isStorageStack()) {
                if (l.getCapacityLeft() >= amount && currentLocation != l) {
                    return l;
                }
            }
        }
        //System.err.println("No stack found with enough capacity left to add boxes to in 1 go");
        return null;
    }

    public List<Location> findRelocationLocations(Location currentLocation, int amount) {
        List<Location> relocationLocations = new ArrayList<>();
        for (Location l : locations) {
            // Only stacks allowed as destination for relocations
            if (l.isStorageStack() && amount != 0) {
                // Stack can't be full and you can't relocate to yourself
                if (!l.isFull() && l != currentLocation) {
                    int capacityLeft = l.getCapacityLeft();
                    // Stack found which can fit all boxes
                    if (capacityLeft >= amount) {
                        relocationLocations.add(l);
                        return relocationLocations;
                    } // You will need multiple stacks to fit all boxes
                    else {
                        relocationLocations.add(l);
                        amount -= l.getCapacityLeft();
                    }
                }
            }
            if (amount == 0) {
                return relocationLocations;
            }
        }
        return null;
    }

}
