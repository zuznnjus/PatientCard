package com.example.patientcard.domain.utils;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

public class GraphPoint implements Serializable {

    private final Date date;
    private final BigDecimal value;

    public GraphPoint(Date date, BigDecimal value) {
        this.date = date;
        this.value = value;
    }

    public Date getDate() {
        return date;
    }

    public BigDecimal getValue() {
        return value;
    }
}
