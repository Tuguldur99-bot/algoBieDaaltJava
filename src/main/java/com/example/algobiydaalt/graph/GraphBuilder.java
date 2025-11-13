package com.example.algobiydaalt.graph;

import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.api.data.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.locationtech.jts.geom.*;
import org.geotools.api.feature.simple.SimpleFeature;
import com.example.algobiydaalt.utils.GeoUtils;

import java.io.File;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class GraphBuilder {

    public static Map<Coordinate, List<Edge>> buildGraph(String shpPath) throws Exception {
        Map<Coordinate, List<Edge>> graph = new HashMap<>();


        File file = new File("src/main/resources/roadShapeFiles/gis_osm_roads_free_1.shp");
        if (!file.exists()) {
            throw new Exception("Shapefile not found: " + file.getAbsolutePath());
        }

        System.out.println(" Loading shapefile: " + file.getAbsolutePath());

        Map<String, Serializable> params = new HashMap<>();
        params.put("url", file.toURI().toURL());
        params.put("create spatial index", Boolean.TRUE);

        ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();
        ShapefileDataStore store = (ShapefileDataStore) dataStoreFactory.createDataStore(params);
        store.setCharset(StandardCharsets.UTF_8);

        SimpleFeatureSource source = store.getFeatureSource();
        int featureCount = 0;
        int edgeCount = 0;

        try (SimpleFeatureIterator iterator = source.getFeatures().features()) {
            while (iterator.hasNext()) {
                SimpleFeature feature = iterator.next();
                featureCount++;
                Object geomObj = feature.getDefaultGeometry();

                // Handle both LineString and MultiLineString
                List<LineString> lines = new ArrayList<>();
                if (geomObj instanceof LineString line) {
                    lines.add(line);
                } else if (geomObj instanceof MultiLineString multiLine) {
                    for (int i = 0; i < multiLine.getNumGeometries(); i++) {
                        lines.add((LineString) multiLine.getGeometryN(i));
                    }
                } else {
                    continue;
                }

                String oneway = Objects.toString(feature.getAttribute("oneway"), "no").toLowerCase();
                String fclass = Objects.toString(feature.getAttribute("fclass"), "").toLowerCase();
                double speed = parseSpeed(feature.getAttribute("maxspeed"));

                double typeFactor = computeTypeFactor(fclass);
                for (LineString line : lines) {
                    Coordinate[] coords = line.getCoordinates();

                    for (int i = 0; i < coords.length - 1; i++) {
                        Coordinate start = coords[i];
                        Coordinate end = coords[i + 1];
                        double distKm = GeoUtils.haversine(start, end);
                        double denom = (speed / 60.0 / typeFactor);
                        if (denom == 0) denom = 1e-6;
                        double weight = distKm / denom;

                        graph.computeIfAbsent(start, k -> new ArrayList<>()).add(new Edge(end, weight));
                        if (!"yes".equals(oneway)) {
                            graph.computeIfAbsent(end, k -> new ArrayList<>()).add(new Edge(start, weight));
                        }
                        edgeCount++;
                    }
                }
            }
        } finally {
            store.dispose();
        }

        System.out.println(" Loaded " + featureCount + " road features.");
        System.out.println(" Built graph with " + graph.size() + " unique nodes and " + edgeCount + " edges.");
        return graph;
    }

    private static double parseSpeed(Object val) {
        try {
            if (val == null) return 60.0;
            return Double.parseDouble(val.toString());
        } catch (Exception e) {
            return 60.0;
        }
    }

    private static double computeTypeFactor(String fclass) {
        double factor = 1.0;
        if (fclass.contains("motorway")) factor = 0.8;
        else if (fclass.contains("trunk")) factor = 0.9;
        else if (fclass.contains("residential")) factor = 1.2;
        else if (fclass.contains("service") || fclass.contains("track")) factor = 1.5;
        return factor;
    }

    public static class Edge {
        public Coordinate dest;
        public double weight;
        public Edge(Coordinate dest, double weight) {
            this.dest = dest;
            this.weight = weight;
        }
    }
}
