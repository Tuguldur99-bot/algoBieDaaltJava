package com.example.algobiydaalt.algorithms;

import org.locationtech.jts.geom.Coordinate;
import java.util.*;
import com.example.algobiydaalt.graph.GraphBuilder.Edge;

public class Dijkstra {

    public static List<Coordinate> shortestPath(
            Map<Coordinate, List<Edge>> graph,
            Coordinate start,
            Coordinate goal
    ) {
        if (start.equals(goal)) return List.of(start);

        PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingDouble(n -> n.cost));
        Map<Coordinate, Double> dist = new HashMap<>();
        Map<Coordinate, Coordinate> parent = new HashMap<>();
        Set<Coordinate> visited = new HashSet<>();

        pq.add(new Node(start, 0.0));
        dist.put(start, 0.0);
        parent.put(start, null);

        while (!pq.isEmpty()) {
            Node cur = pq.poll();
            if (visited.contains(cur.coord)) continue;
            visited.add(cur.coord);

            if (cur.coord.equals(goal)) return reconstructPath(parent, start, goal);

            for (Edge e : graph.getOrDefault(cur.coord, List.of())) {
                double newCost = cur.cost + e.weight;
                if (newCost < dist.getOrDefault(e.dest, Double.MAX_VALUE)) {
                    dist.put(e.dest, newCost);
                    parent.put(e.dest, cur.coord);
                    pq.add(new Node(e.dest, newCost));
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

    private static class Node {
        public Coordinate coord;
        public double cost;
        Node(Coordinate c, double cost) { this.coord = c; this.cost = cost; }
    }
}
