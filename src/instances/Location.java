package src.instances;

import java.util.*;

public class Location {
    private String ID;
    private LocationType locationType;
    private List<Box> boxes;
    private String name;
    private int x;
    private int y;
    private int maxNumboxes;
    private int amountOfReservations;
    private int stackToStackReservations = 0;

    // Regular Location Constructor
    public Location(String ID, LocationType locationType, List<Box> boxes, String name, int x, int y, int maxNumboxes) {
        this.ID = ID;
        this.locationType = locationType;
        this.boxes = boxes;
        this.name = name;
        this.x = x;
        this.y = y;
        this.maxNumboxes = maxNumboxes;
        this.amountOfReservations = 0;
    }

    // Vehicle Location Constructor
    public Location(String vehicleName, int x, int y) {
        this.name = vehicleName;
        this.x = x;
        this.y = y;
        this.boxes = new ArrayList<>();
    }

    public void addstackToStackReservations(int amount){
        this.stackToStackReservations +=amount;
    }

    public int getStackToStackReservations(){
        return stackToStackReservations;
    }

    public String getID() {
        return ID;
    }

    public LocationType getLocationType() {
        return locationType;
    }

    public List<Box> getBoxes() {
        return boxes;
    }

    public String getName() {
        return name;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getMaxNumboxes() {
        return maxNumboxes;
    }

    public int getAmountOfReservations() {
        return amountOfReservations;
    }

    public int getAmountOfStackToStackReservations(){
        return stackToStackReservations;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public void setLocationType(LocationType locationType) {
        this.locationType = locationType;
    }

    public void setBoxes(List<Box> boxes) {
        this.boxes = boxes;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setMaxNumboxes(int maxNumboxes) {
        this.maxNumboxes = maxNumboxes;
    }

    public void setAmountOfReservations(int amountOfReservations){
        this.amountOfReservations = amountOfReservations;
    }

    public void addAmountOfReservations(int amount){
        this.amountOfReservations+=amount;
    }

    public boolean addBox(Box box, int currentTime) {
            if (isStorageStack()) {
                if (boxes.size() < maxNumboxes) {
                    box.setStackId(this.ID);
                    int positionWithinStack = calculatePositionWithinStack(currentTime);
                    box.setPositionWithinStack(positionWithinStack);
                    box.setAtLocationSince(currentTime);
                    boxes.add(box);
                    //System.out.println("Box " + box.getBoxID() + " geplaatst in " + this.name);
                } else {
                    // Voeg hier de logica toe voor het geval de stack vol is
                    //System.out.println("Stack is vol, relocation nodig...");
                    return false;
                }
            } else { // Het is een buffer
                box.setStackId(this.ID);
                box.setPositionWithinStack(-1); // Position within buffer
                box.setAtLocationSince(currentTime);
                boxes.add(box);
            }
        return true;
    }

    // Bereken de juiste positionWithinStack op basis van de huidige tijd
    private int calculatePositionWithinStack(int currentTime) {
        int position = 0;
        for (Box existingBox : boxes) {
            if (existingBox.getAtLocationSince() <= currentTime) {
                position++;
            }
        }
        return position;
    }

    public void addMultipleBoxes(List<Box> transportReqestBoxes, int currentTime){
        if(this.getCapacityLeft() >= transportReqestBoxes.size()){
            for(Box b : transportReqestBoxes){
                addBox(b, currentTime);
                //System.out.println("Added box " + b.getBoxID() + " to " + this.name);
            }
        }else{
            //System.out.println("addMultipleBoxes FAILED : not enough capacity on this stack");
        }

    }

    public List<Box> removeMultipleBoxes(int amountToBeRemoved){
        List<Box> removedBoxes = new ArrayList<>();

        for(int i = 0; i < amountToBeRemoved; i++){
            Box b = removeTopBox();
            removedBoxes.add(b);
        }
        return removedBoxes;
    }

    public int getCapacityLeft(){
        return maxNumboxes - boxes.size();
    }

    // Verwijder de laatst toegevoegde box van de locatie (LIFO-regel)
    public Box removeTopBox() {
        if (!boxes.isEmpty()) {
            int lastIndex = boxes.size() - 1;
            Box removedBox = boxes.get(lastIndex);
            boxes.remove(lastIndex);
            removedBox.setPositionWithinStack(-1);
            removedBox.setStackId("VEHICLE");
            //System.out.println("Removed TopBox " + removedBox.getBoxID());
            return removedBox;
        } else {
            //System.out.println("Er is geen box om te removen op " + this.getName());
            return null;
        }
    }

    // Bij buffer geen lifo regel
    public Box removeBox(String boxID) {
        if (this.isBufferPoint()) {
            for (Box b : boxes) {
                if (b.getBoxID().equals(boxID)) {
                    Box toBeRemoved = b;
                    boxes.remove(b);
                    toBeRemoved.setPositionWithinStack(-1);
                    toBeRemoved.setStackId("VEHICLE");
                    return toBeRemoved;
                }
            }
        } else {
            //System.out.println("Bij een stack mag je niet in het midden een box pakken");
        }
        return null;
    }

    public Box getTopBox() {
        return boxes.get(boxes.size() - 1);
    }

    public boolean isOnTop(String boxID) {
        if (boxes.isEmpty()) {
            return false; // The list is empty, so the box can't be on top.
        } else {
            return boxes.get(boxes.size() - 1).getBoxID().equals(boxID);
        }

    }

    public int getAmountOfBoxesAboveBox(String boxId) {
        for (Box box : boxes) {
            if (box.getBoxID().equals(boxId)) {
                return boxes.size() - box.getPositionWithinStack() - 1;
            }
        }
        //System.out.println("BoxId niet gevonden. Boxid: " + boxId);
        return -1;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("location: [id: ").append(ID).append(", name: ").append(name).append(", locationType: ").append(locationType).append(", boxes: [");

        int numberOfBoxes = boxes.size();
        for (int i = 0; i < numberOfBoxes; i++) {
            Box b = boxes.get(i);
            sb.append(b.toString());
            if (i < numberOfBoxes - 1) {
                sb.append(", ");
            }
        }

        sb.append("]");
        sb.append(", x: ").append(x).append("; y: ").append(y).append(", maxNumBoxes: ").append(maxNumboxes).append(", amountOfBoxes: ").append(boxes.size());
        sb.append(" ]");
        return sb.toString();
    }

    public boolean isFull() {
        return boxes.size() == maxNumboxes;
    }

    public boolean isBufferPoint() {
        return locationType == LocationType.BUFFERPOINT;
    }

    public boolean isStorageStack() {
        return locationType == LocationType.STORAGESTACK;
    }

}
