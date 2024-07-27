package src.GUI;

import src.algorithm.Pair;
import src.instances.Location;
import src.instances.LocationType;
import src.instances.Vehicle;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class IO {
/*
    public void printInitialIntervalsBeforeConflictResolving() {
        System.out.println("Amount of intervals " + intervals.size());
        for (Pair p : intervals) {
            System.out.println(p.getFirst());
        }
    }

    public void printVehicleLocations() {
        for (Vehicle vehicle : vehicleLocations.keySet()) {
            System.out.println("Vehicle: " + vehicle.getName());
            Map<Integer, Location> locations = vehicleLocations.get(vehicle);
            List<Integer> sortedTimes = new ArrayList<>(locations.keySet());
            Collections.sort(sortedTimes);

            int startTime = sortedTimes.get(0);
            int endTime = startTime;

            for (int i = 1; i < sortedTimes.size(); i++) {
                int currentTime = sortedTimes.get(i);
                Location currentLocation = locations.get(currentTime);
                Location previousLocation = locations.get(sortedTimes.get(i - 1));

                if (currentTime - endTime == vehicle.getLoadDuration() && currentLocation.equals(previousLocation) && currentLocation.getLocationType() != LocationType.BUFFERPOINT) {
                    // The current timestamp is loadTime units after the previous timestamp and has
                    // the same location + is not a buffer.
                    endTime = currentTime;
                } else {
                    printLocationInterval(startTime, endTime, previousLocation);
                    startTime = endTime = currentTime;
                }
            }

            printLocationInterval(startTime, endTime, locations.get(endTime)); // Laatste interval afdrukken
            System.out.println("==================================");
        }
    }

    private void printLocationInterval(int startTime, int endTime, Location location) {
        if (startTime == endTime) {
            System.out.println("Time: " + startTime + ", Location: " + location.getName() + " (" + location.getX() + ","
                    + location.getY() + ")");
        } else {
            System.out.println("Time: [" + startTime + "," + endTime + "], Location: " + location.getName() + " ("
                    + location.getX() + "," + location.getY() + ")");
        }
    }

    public void writeVehicleLocationsToFile(String filePath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (Vehicle vehicle : vehicleLocations.keySet()) {
                writer.write("Vehicle: " + vehicle.getName() + "\n");
                Map<Integer, Location> locations = vehicleLocations.get(vehicle);
                List<Integer> sortedTimes = new ArrayList<>(locations.keySet());
                Collections.sort(sortedTimes);

                int startTime = sortedTimes.get(0);
                int endTime = startTime;
                Location previousLocation = locations.get(startTime);

                for (int i = 1; i < sortedTimes.size(); i++) {
                    int currentTime = sortedTimes.get(i);
                    Location currentLocation = locations.get(currentTime);

                    if (currentTime - endTime == 5 && currentLocation.equals(previousLocation)) {
                        endTime = currentTime;
                    } else {
                        writeLocationInterval(writer, startTime, endTime, previousLocation);
                        startTime = endTime = currentTime;
                    }

                    previousLocation = currentLocation;
                }

                writeLocationInterval(writer, startTime, endTime, previousLocation);
                writer.write("==================================\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeLocationInterval(BufferedWriter writer, int startTime, int endTime, Location location)
            throws IOException {
        if (startTime == endTime) {
            writer.write("Time: " + startTime + ", Location: " + location.getName() + ", Coordinates: ("
                    + location.getX() + ", " + location.getY() + ")\n");
        } else {
            writer.write("Time: [" + startTime + "," + endTime + "], Location: " + location.getName()
                    + ", Coordinates: (" + location.getX() + ", " + location.getY() + ")\n");
        }
    }

    public void writeVehicleLocationsToCSV(String filePath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write("vehicle,time,location,coordinates\n");
            for (Vehicle vehicle : vehicleLocations.keySet()) {
                Map<Integer, Location> locations = vehicleLocations.get(vehicle);
                List<Integer> sortedTimes = new ArrayList<>(locations.keySet());
                Collections.sort(sortedTimes);
                for (int time : sortedTimes) {
                    Location location = locations.get(time);
                    String line = vehicle.getName() + "," + time + "," + location.getName() + "," + location.getX()
                            + "," + location.getY() + "\n";
                    writer.write(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
*/
}
