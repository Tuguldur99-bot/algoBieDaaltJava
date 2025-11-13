package com.example.algobiydaalt.controller;

import com.example.algobiydaalt.graph.GraphBuilder;
import com.example.algobiydaalt.graph.GraphBuilder.Edge;
import com.example.algobiydaalt.utils.GeoUtils;
import com.example.algobiydaalt.algorithms.*;
import org.locationtech.jts.geom.Coordinate;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.*;
import java.io.File;

@RestController
@CrossOrigin
public class RouteController {

    private static Map<Coordinate, List<Edge>> graph;
    private static List<Coordinate> nodes;

    static {
        try {
            System.out.println("Loading shapefile... this may take a while.");
            String shpPath = new File("src/main/resources/roadShapeFiles/gis_osm_roads_free_1.shp").getAbsolutePath();

            graph = GraphBuilder.buildGraph(shpPath);
            nodes = new ArrayList<>(graph.keySet());
            System.out.println("Graph loaded with " + nodes.size() + " nodes.");
        } catch (Exception e) {
            e.printStackTrace();
            graph = new HashMap<>();
            nodes = new ArrayList<>();
        }
    }

@GetMapping("/route")
public ResponseEntity<?> getRoute(
        @RequestParam(name = "start") String start,
        @RequestParam(name = "end") String end,
        @RequestParam(name = "algo", defaultValue = "dijkstra") String algo
)
 {
        try {
            Coordinate startCoord = parseCoord(start);
            Coordinate endCoord = parseCoord(end);


            GeoUtils.NodeLocator locator = new GeoUtils.NodeLocator(nodes);
            Coordinate startNode = locator.findNearest(startCoord);
            Coordinate endNode = locator.findNearest(endCoord);

                    if (startNode == null || endNode == null) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Could not find nearest road node for one or both of the selected points. " +
                         "Try clicking closer to a visible road."
            ));
        }

            long startTime = System.nanoTime();
            List<Coordinate> path = switch (algo.toLowerCase()) {
                case "bfs" -> BFS.path(graph, startNode, endNode);
                case "dfs" -> DFS.path(graph, startNode, endNode);
                default -> Dijkstra.shortestPath(graph, startNode, endNode);
            };
            long duration = System.nanoTime() - startTime;
            double runtimeSec = duration / 1e9;

            Map<String, Object> result = new HashMap<>();
            result.put("algorithm", algo);
            result.put("start", coordToString(startNode));
            result.put("end", coordToString(endNode));
            result.put("path_length", path.size());
            result.put("runtime_seconds", runtimeSec);
            result.put("path", path.stream().map(this::coordToString).toList());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    private Coordinate parseCoord(String s) {
        String[] parts = s.split(",");
        return new Coordinate(Double.parseDouble(parts[0]), Double.parseDouble(parts[1]));
    }

    private String coordToString(Coordinate c) {
        return String.format("%.6f,%.6f", c.x, c.y);
    }
}
