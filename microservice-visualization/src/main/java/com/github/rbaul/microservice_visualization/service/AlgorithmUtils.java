package com.github.rbaul.microservice_visualization.service;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.cycle.PatonCycleBase;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@UtilityClass
public class AlgorithmUtils {

    // Function to find all simple cycles in an undirected graph using Paton's algorithm
    public Set<List<String>> findSimpleCycles(Map<String, List<String>> connections) {
        Graph<String, CustomEdge> graph = new SimpleGraph<>(CustomEdge.class);

        // Vertex
        connections.keySet().forEach(graph::addVertex);

        // Edges
        connections.forEach((vertex, cons) -> {
            for (String con : cons) {
                graph.addEdge(vertex, con, new CustomEdge());
            }
        });

        return findSimpleCycles(graph);
    }

    // Function to find all simple cycles in an undirected graph using Paton's algorithm
    private Set<List<String>> findSimpleCycles(Graph<String, CustomEdge> graph) {
        PatonCycleBase<String, CustomEdge> cycleBase = new PatonCycleBase<>(graph);
        return cycleBase.getCycleBasis().getCyclesAsGraphPaths().stream().map(GraphPath::getVertexList).collect(Collectors.toSet());
    }

    // Custom edge class (required by JGraphT for undirected graphs)
    private static class CustomEdge extends DefaultEdge {
        // You can add custom properties if needed
    }
}
