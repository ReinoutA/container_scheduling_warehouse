package src.factories;

import org.json.JSONArray;
import org.json.JSONObject;
import src.Main;
import src.instances.Vehicle;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class VehicleFactory {

    public List<Vehicle> createVehiclesFromJson(String json) throws IOException {
        List<Vehicle> vehicles = new ArrayList<>();

        String jsonContent = new String(Files.readAllBytes(Paths.get(json)));
        JSONObject jsonObject = new JSONObject(jsonContent);

        Vehicle.LOAD_DURATION = jsonObject.getInt("loadingduration");
        Vehicle.VEHICLE_SPEED = jsonObject.getInt("vehiclespeed");

        JSONArray vehicleArray = jsonObject.getJSONArray("vehicles");

        for (int i = 0; i < vehicleArray.length(); i++) {
            JSONObject vehicleJson = vehicleArray.getJSONObject(i);

            int ID = vehicleJson.getInt("ID");
            String name = vehicleJson.getString("name");
            int xCoordinate;
            int yCoordinate;
            if(Main.NEW_FORMAT){
                xCoordinate = vehicleJson.getInt("x");
                yCoordinate = vehicleJson.getInt("y");
            }else{
                xCoordinate = vehicleJson.getInt("xCoordinate");
                yCoordinate = vehicleJson.getInt("yCoordinate");
            }

            int capacity = vehicleJson.getInt("capacity");

            Vehicle vehicle = new Vehicle(ID, name, xCoordinate, yCoordinate, capacity);
            vehicles.add(vehicle);
        }

        return vehicles;
    }
}

