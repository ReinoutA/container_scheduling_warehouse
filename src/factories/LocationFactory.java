package src.factories;

import org.json.*;
import src.instances.*;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public class LocationFactory {

    public List<Location> createLocationsFromJson(String json, List<TransportRequest> transportRequests) throws IOException {
        List<Location> locations = new ArrayList<>();

        String jsonContent = new String(Files.readAllBytes(Paths.get(json)));
        JSONObject jsonObject = new JSONObject(jsonContent);

        JSONArray stacksArray = jsonObject.getJSONArray("stacks");

        // Process stack data
        for (int i = 0; i < stacksArray.length(); i++) {
            JSONObject stackJson = stacksArray.getJSONObject(i);

            String ID = String.valueOf(stackJson.getInt("ID"));
            LocationType locationType = LocationType.STORAGESTACK;
            List<Box> boxes = new ArrayList<>();
            JSONArray boxArray = stackJson.getJSONArray("boxes");
            for (int j = 0; j < boxArray.length(); j++) {
                String boxID = boxArray.getString(j);
                boxes.add(new Box(boxID, j, ID));
            }

            String name = stackJson.getString("name");
            int x = stackJson.getInt("x");
            int y = stackJson.getInt("y");
            int maxNumboxes = jsonObject.getInt("stackcapacity");

            Location location = new Location(ID, locationType, boxes, name, x, y, maxNumboxes);
            // System.out.println("Added box " + boxes + " to Stack: " + ID);
            locations.add(location);
        }

        JSONArray bufferpointsArray = jsonObject.getJSONArray("bufferpoints");

        // Process bufferpoint data
        for (int i = 0; i < bufferpointsArray.length(); i++) {
            JSONObject bufferpointJson = bufferpointsArray.getJSONObject(i);

            String ID = String.valueOf(bufferpointJson.getInt("ID"));
            LocationType locationType = LocationType.BUFFERPOINT;
            List<Box> boxes = new ArrayList<>();
            String name = bufferpointJson.getString("name");
            int x = bufferpointJson.getInt("x");
            int y = bufferpointJson.getInt("y");
            int maxNumboxes = Integer.MAX_VALUE;
            Location location = new Location(ID, locationType, boxes, name, x, y, maxNumboxes);

            // Add boxes that will be picked up from entrance
            for (TransportRequest tr : transportRequests) {
                String pickupLocationName = tr.getPickupLocationName();
                if(name.equals(pickupLocationName)) {
                    location.addBox(new Box(tr.getBoxID(), 0, ID), 0);
                    //System.out.println("Added box " + tr.getBoxID() + " to BufferPoint: " + pickupLocationName);
                }
            }

            locations.add(location);
        }

        return locations;
    }
}
