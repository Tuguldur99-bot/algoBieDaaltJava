package com.example.algobiydaalt.algorithms;

import org.locationtech.jts.geom.Coordinate;
import java.util.*;
import com.example.algobiydaalt.graph.GraphBuilder.Edge;

public class DFS {
    public static List<Coordinate> path(Map<Coordinate, List<Edge>> graph, Coordinate start, Coordinate goal) {
        Stack<Coordinate> stack = new Stack<>();
        Map<Coordinate, Coordinate> parent = new HashMap<>();
        Set<Coordinate> visited = new HashSet<>();

        stack.push(start);
        parent.put(start, null);

        while (!stack.isEmpty()) {
            Coordinate node = stack.pop();
            if (visited.contains(node)) continue;
            visited.add(node);

            if (node.equals(goal)) return reconstructPath(parent, start, goal);

            for (Edge e : graph.getOrDefault(node, List.of())) {
                if (!visited.contains(e.dest)) {
                    parent.put(e.dest, node);
                    stack.push(e.dest);
                }
            }
        }
        return List.of();
    }

    private static List<Coordinate> reconstructPath(Map<Coordinate, Coordinate> parent, Coordinate start, Coordinate goal) {
        List<Coordinate> path = new ArrayList<>();
        Coordinate cur = goal;
        while (cur != null) {
            path.add(cur);
            cur = parent.get(cur);
        }
        Collections.reverse(path);
        return path;
    }
}
