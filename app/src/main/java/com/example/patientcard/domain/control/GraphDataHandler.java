package com.example.patientcard.domain.control;

import com.example.patientcard.domain.utils.GraphPoint;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

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

    public List<GraphPoint> getFilteredGraphData(String begin, String end) {
        if (StringUtils.isBlank(begin) && StringUtils.isBlank(end)) {
            return graphData;
        }
        LocalDate beginDate;
        LocalDate endDate;
        if (StringUtils.isNotBlank(begin)) {
            beginDate = parseStringToDate(begin);
        } else {
            beginDate = LocalDate.of(1900, 1, 1);
        }
        if (StringUtils.isNotBlank(end)) {
            endDate = parseStringToDate(end);
        } else {
            endDate = LocalDate.now();
        }

        return graphData.stream()
                .filter(graphPoint -> isBetweenGivenDates(graphPoint.getDate(), beginDate, endDate))
                .collect(Collectors.toList());
    }

    private LocalDate parseStringToDate(String date) {
        String[] split = date.split("-");
        return LocalDate.of(Integer.parseInt(split[2]), Integer.parseInt(split[1]), Integer.parseInt(split[0]));
    }

    private boolean isBetweenGivenDates(Date date, LocalDate beginDate, LocalDate endDate) {
        LocalDate convertedDate = date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        return convertedDate.isAfter(beginDate) && convertedDate.isBefore(endDate);
    }
}
