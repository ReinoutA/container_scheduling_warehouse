package src.instances;

public class TimeInterval {
    private String vehicleName;
    private final int startTime;
    private final int endTime;
    private String location;
    public int teller;

    public TimeInterval(String vehicleName, int startTime, int endTime, String location) {
        this.vehicleName = vehicleName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.location = location;
        this.teller = 0;
    }

    public TimeInterval(int startTime, int endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public boolean contains(int timestamp) {
        return timestamp >= startTime && timestamp <= endTime;
    }

    public String getLocation() {
        return location;
    }

    public int getStartTime() {
        return startTime;
    }

    public String getVehicleName() {
        return vehicleName;
    }

    public void setVehicle(String vehicleName) {
        this.vehicleName = vehicleName;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getEndTime() {
        return endTime;
    }

    @Override
    public String toString() {
        return "[" + startTime + " - " + endTime + "]";
    }

    public int getOverlapDuration(TimeInterval other) {
        int overlapStart = Math.max(this.startTime, other.startTime);
        int overlapEnd = Math.min(this.endTime, other.endTime);

        if (overlapStart < overlapEnd) {
            return overlapEnd - overlapStart;
        } else {
            return 0; // No overlap
        }
    }
}
