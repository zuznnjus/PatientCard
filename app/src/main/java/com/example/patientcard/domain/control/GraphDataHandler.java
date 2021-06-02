package com.example.patientcard.domain.control;

import com.example.patientcard.domain.utils.GraphPoint;

import java.io.Serializable;
import java.util.List;

public class GraphDataHandler implements Serializable {

    private final String graphTitle;
    private final List<GraphPoint> graphData;

    public GraphDataHandler(String graphTitle, List<GraphPoint> graphData) {
        this.graphTitle = graphTitle;
        this.graphData = graphData;
    }

    public String getGraphTitle() {
        return graphTitle;
    }

    public List<GraphPoint> getGraphData() {
        return graphData;
    }
}
