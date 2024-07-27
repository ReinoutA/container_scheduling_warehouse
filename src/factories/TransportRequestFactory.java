package src.factories;

import org.json.*;
import src.Main;
import src.instances.*;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public class TransportRequestFactory {

    public List<TransportRequest> createTransportRequestsFromJson(String json) throws IOException {
        List<TransportRequest> transportRequests = new ArrayList<>();

        String jsonContent = new String(Files.readAllBytes(Paths.get(json)));
        JSONObject jsonObject = new JSONObject(jsonContent);

        JSONArray requestArray = jsonObject.getJSONArray("requests");

        for (int i = 0; i < requestArray.length(); i++) {
            JSONObject requestJson = requestArray.getJSONObject(i);

            int id = requestJson.getInt("ID");

            // Get pickupLocation as an array of strings
            String pickupLocationName;
            String placeLocationName;
            // The formats of the instances are different
            if(Main.NEW_FORMAT) {   // this is the format of the advanced instances
                pickupLocationName = requestJson.getString("pickupLocation");
                placeLocationName = requestJson.getString("placeLocation");
            }else{  // this is the format of the basic instances
                JSONArray pickupLocationArray = requestJson.getJSONArray("pickupLocation");
                pickupLocationName = pickupLocationArray.getString(0);
                JSONArray placeLocationArray = requestJson.getJSONArray("placeLocation");
                placeLocationName = placeLocationArray.getString(0);
            }

            String boxID = requestJson.getString("boxID");

            TransportRequest transportRequest = new TransportRequest(id, pickupLocationName, placeLocationName, boxID);

            transportRequests.add(transportRequest);
        }

        return transportRequests;
    }
}
