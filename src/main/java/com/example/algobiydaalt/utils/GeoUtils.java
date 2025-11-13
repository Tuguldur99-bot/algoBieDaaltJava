package com.example.algobiydaalt.utils;

import org.locationtech.jts.geom.Coordinate;
import java.util.*;

public class GeoUtils {
    private static final double EARTH_RADIUS_KM = 6371.0;

    public static double haversine(Coordinate a, Coordinate b) {
        double lon1 = a.x, lat1 = a.y;
        double lon2 = b.x, lat2 = b.y;
        double dLon = Math.toRadians(lon2 - lon1);
        double dLat = Math.toRadians(lat2 - lat1);
        double h = Math.pow(Math.sin(dLat / 2), 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.pow(Math.sin(dLon / 2), 2);
        return 2 * EARTH_RADIUS_KM * Math.asin(Math.sqrt(h));
    }

    public static class NodeLocator {
        private final List<Coordinate> nodes;

        public NodeLocator(List<Coordinate> nodes) {
            this.nodes = nodes;
        }

        public Coordinate findNearest(Coordinate coord) {
            Coordinate nearest = null;
            double bestDist = Double.MAX_VALUE;
            for (Coordinate c : nodes) {
                double dist = haversine(coord, c);
                if (dist < bestDist) {
                    bestDist = dist;
                    nearest = c;
                }
            }
            return nearest;
        }
    }
}
