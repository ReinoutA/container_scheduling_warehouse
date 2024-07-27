package src.instances;

import java.util.*;

public class Vehicle {

    public static int VEHICLE_SPEED;
    public static int LOAD_DURATION;

    private int ID;
    private String name;
    private int xCoordinate;
    private int yCoordinate;
    private int capacity;
    private final List<TimeInterval> busyIntervals = new ArrayList<>();
    private int busyUntil;

    public Vehicle(int ID, String name, int xCoordinate, int yCoordinate, int capacity) {
        this.ID = ID;
        this.name = name;
        this.xCoordinate = xCoordinate;
        this.yCoordinate = yCoordinate;
        this.capacity = capacity;
    }

    public int getBusyUntil() {
        return busyUntil;
    }

    public void setBusyUntil(int busyUntil) {
        this.busyUntil = busyUntil;
    }

    public int getID() {
        return ID;
    }

    public String getName() {
        return name;
    }

    public int getXCoordinate() {
        return xCoordinate;
    }

    public int getYCoordinate() {
        return yCoordinate;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setXCoordinate(int xCoordinate) {
        this.xCoordinate = xCoordinate;
    }

    public void setYCoordinate(int yCoordinate) {
        this.yCoordinate = yCoordinate;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public void setVEHICLE_SPEED(int VEHICLE_SPEED) {
        Vehicle.VEHICLE_SPEED = VEHICLE_SPEED;
    }

    public void setLOAD_DURATION(int LOAD_DURATION) {
        Vehicle.LOAD_DURATION = LOAD_DURATION;
    }

    public int getLoadDuration() {
        return LOAD_DURATION;
    }

    public void addBusyInterval(Vehicle vehicle, int startTime, int endTime, Location location) {
        TimeInterval interval = new TimeInterval(vehicle.getName(), startTime, endTime, location.getName());
        busyIntervals.add(interval);
    }

    public boolean isBusy(int timestamp) {
        for (TimeInterval interval : busyIntervals) {
            if (interval.contains(timestamp)) {
                return true;
            }
        }
        return false;
    }

    public List<TimeInterval> getBusyIntervals() {
        return busyIntervals;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("vehicle: [id: ").append(ID).append(", name: ").append(name).append(", x: ").append(xCoordinate).append(", y: ").append(yCoordinate).append(", capacity: ").append(capacity).append("]");
        return sb.toString();
    }

}
