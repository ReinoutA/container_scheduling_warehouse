package src.instances;

public class Box {
    private String boxID;
    private int positionWithinStack;
    private String stackId;
    private int atLocationSince;

    public Box(String boxID, int positionWithinStack, String stackId) {
        this.boxID = boxID;
        this.positionWithinStack = positionWithinStack;
        this.stackId = stackId;
        this.atLocationSince = 0;
    }

    public String getBoxID() {
        return boxID;
    }

    public int getPositionWithinStack() {
        return positionWithinStack;
    }

    public String getStackId() {
        return stackId;
    }

    public void setBoxID(String boxID) {
        this.boxID = boxID;
    }

    public void setPositionWithinStack(int positionWithinStack) {
        this.positionWithinStack = positionWithinStack;
    }

    public void setStackId(String stackId) {
        this.stackId = stackId;
    }


    public int getAtLocationSince(){
        return atLocationSince;
    }

    public void setAtLocationSince(int atLocationSince){
        this.atLocationSince = atLocationSince;
    }
    public String toString() {
        return "box: [boxID: " + boxID + ", positionWithinStack: " + positionWithinStack + ", at location since: " + atLocationSince + ", stackId: " + stackId + "]";
    }
}
