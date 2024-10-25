package com.aau.herd.controller.Utils;

public class Trigonometry {

    // Calculates the angle between the current position and the go-to position.
    public static double calcAngleBtwPoints(Position pos1, Position pos2){

        // Convert to radians
        double lat1Rad = Math.toRadians(pos1.getLat());
        double lon1Rad = Math.toRadians(pos1.getLng());
        double lat2Rad = Math.toRadians(pos2.getLat());
        double lon2Rad = Math.toRadians(pos2.getLng());

        double y = Math.sin(lon2Rad-lon1Rad) * Math.cos(lat2Rad);
        double x = Math.cos(lat1Rad) * Math.sin(lat2Rad) - Math.sin(lat1Rad) * Math.cos(lat2Rad) * Math.cos(lon2Rad-lon1Rad);

        return Math.toDegrees(Math.atan2(y, x));
    }

    // Calculates the distance between the current position and the go-to position.
    public static double calcDistanceBtwPoints(Position pos1, Position pos2){

        // Earth's radius
        double R = 6371e3;

        // Convert to radians
        double lat1Rad = Math.toRadians(pos1.getLat());
        double lon1Rad = Math.toRadians(pos1.getLng());
        double lat2Rad = Math.toRadians(pos2.getLat());
        double lon2Rad = Math.toRadians(pos2.getLng());

        // Delta
        double dLat = lat2Rad - lat1Rad;
        double dLon = lon2Rad - lon1Rad;

        double a = Math.sin(dLat/2) * Math.sin(dLat/2) + Math.cos(lat1Rad) * Math.cos(lat2Rad) * Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

        return R * c;
    }
}
