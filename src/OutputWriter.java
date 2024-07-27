package src;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import src.algorithm.*;
import src.instances.*;
import src.scheduler.RoutingSchedule;

public class OutputWriter {
    public static void writeScheduleAndLocations(String outputPath, RoutingSchedule routingSchedule, LocationManager locationManager) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {
            writer.write(routingSchedule.toString());
            //writer.write(locationManager.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
