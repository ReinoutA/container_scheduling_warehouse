package src.instances;

import java.util.*;

public class TransportRequest {
    private int ID;
    private String pickupLocationName; // Stack or buffer
    private String placeLocationName; // Stack or buffer
    private String boxID;
    private List<String> boxIDs = new ArrayList<>();

    public TransportRequest(int ID, String pickupLocationName, String placeLocationName, String boxID) {
        this.ID = ID;
        this.pickupLocationName = pickupLocationName;
        this.placeLocationName = placeLocationName;
        this.boxID = boxID;
        boxIDs.add(boxID);
    }

    public TransportRequest(TransportRequest transportRequest) {
        this.ID = transportRequest.ID;
        this.pickupLocationName = transportRequest.pickupLocationName;
        this.placeLocationName = transportRequest.placeLocationName;
        this.boxID = transportRequest.boxID;
        this.boxIDs = new ArrayList<>();
        boxIDs.addAll(transportRequest.boxIDs);
    }

    public int getID() {
        return ID;
    }

    public String getPickupLocationName() {
        return pickupLocationName;
    }

    public String getPlaceLocationName() {
        return placeLocationName;
    }

    public String getBoxID() {
        return boxID;
    }

    public List<String> getBoxIDs(){
        return boxIDs;
    }

    public void addBoxID(String boxID){
        this.boxIDs.add(boxID);
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public void setPickupLocation(String pickupLocationName) {
        this.pickupLocationName = pickupLocationName;
    }

    public void setPossiblePlaceLocations(String placeLocationName) {
        this.placeLocationName = placeLocationName;
    }

    public void setBoxID(String boxID) {
        this.boxID = boxID;
    }

    public void removeBoxId(String boxID){
        boxIDs.removeIf(s -> s.equals(boxID));
    }

    public void setBoxIDs(List<String> boxIDs) {
        this.boxIDs = boxIDs;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("transportRequest: [id: ").append(ID)
                .append(", pickupLocationName: ").append(pickupLocationName)
                .append(", placeLocationName: ").append(placeLocationName);

        if (boxIDs.size() > 1) {
            sb.append(", boxIDs: ").append(Arrays.toString(boxIDs.toArray()));
        } else if(boxID==null){
            sb.append(", boxIDs: ").append(Arrays.toString(boxIDs.toArray()));
        }else {
            sb.append(", boxID: ").append(boxID);
        }

        sb.append("]");
        return sb.toString();
    }

}
