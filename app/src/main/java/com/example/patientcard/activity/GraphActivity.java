package com.example.patientcard.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;

import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYGraphWidget;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.example.patientcard.R;
import com.example.patientcard.dialog.DateDialog;
import com.example.patientcard.domain.control.GraphDataHandler;
import com.example.patientcard.domain.utils.GraphPoint;
import com.example.patientcard.domain.utils.IntentMessageCodes;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.math.BigDecimal;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class GraphActivity extends AppCompatActivity {

    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd");
    private XYPlot plot;
    private DateDialog beginDate;
    private DateDialog endDate;
    private GraphDataHandler graphDataHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        Intent intent = getIntent();
        graphDataHandler = (GraphDataHandler) intent.getSerializableExtra(IntentMessageCodes.GRAPH_DATA_MESSAGE);

        plot = findViewById(R.id.plot);

        EditText textGraphBegin = findViewById(R.id.editTextGraphBegin);
        EditText textGraphEnd = findViewById(R.id.editTextGraphEnd);
        beginDate = new DateDialog(this, textGraphBegin);
        endDate = new DateDialog(this, textGraphEnd);
        textGraphBegin.addTextChangedListener(createTextListener());
        textGraphEnd.addTextChangedListener(createTextListener());

        drawPlot(graphDataHandler.getGraphData());

        Button buttonClearFilters = findViewById(R.id.buttonClearGraphFilters);
        buttonClearFilters.setOnClickListener(v -> {
            textGraphBegin.setText(StringUtils.EMPTY);
            textGraphEnd.setText(StringUtils.EMPTY);
        });
        Button buttonOk = findViewById(R.id.buttonGraphOk);
        buttonOk.setOnClickListener(v -> finish());
    }

    private TextWatcher createTextListener() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                new Thread(() -> {
                    List<GraphPoint> graphPoints =
                            graphDataHandler.getFilteredGraphData(
                                    beginDate.getEditText().getText().toString(),
                                    endDate.getEditText().getText().toString());
                    runOnUiThread(() -> drawPlot(graphPoints));
                }).start();
            }
        };
    }

    private void drawPlot(List<GraphPoint> graphPoints) {
        List<Long> dates = graphPoints.stream()
                .map(GraphPoint::getDate)
                .map(Date::getTime)
                .collect(Collectors.toList());

        List<BigDecimal> values = graphPoints.stream()
                .map(GraphPoint::getValue)
                .collect(Collectors.toList());

        BigDecimal minValue = Collections.min(values)
                .subtract(BigDecimal.valueOf(10))
                .max(BigDecimal.valueOf(0));
        BigDecimal maxValue = Collections.max(values)
                .add(BigDecimal.valueOf(10));

        Long minDate = DateUtils.addDays(new Date(Collections.min(dates)), -30).getTime();
        Long maxDate = DateUtils.addDays(new Date(Collections.max(dates)), 30).getTime();

        XYSeries plotSeries = new SimpleXYSeries(dates, values, graphDataHandler.getGraphTitle());
        LineAndPointFormatter seriesFormat = getFormatter();

        plot.clear();
        plot.addSeries(plotSeries, seriesFormat);
        plot.setTitle(graphDataHandler.getGraphTitle());
        plot.setRangeBoundaries(minValue, maxValue, BoundaryMode.FIXED);
        plot.setDomainBoundaries(minDate, maxDate, BoundaryMode.FIXED);
        plot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.BOTTOM).setFormat(getBottomEdgeFormat());
        plot.getLayoutManager().remove(plot.getLegend());
        plot.redraw();
    }

    private Format getBottomEdgeFormat() {
        return new Format() {
            @Override
            public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
                long longValue = ((Double) obj).longValue();
                Date date = new Date(longValue);
                return toAppendTo.append(DATE_FORMATTER.format(date));
            }

            @Override
            public Object parseObject(String source, ParsePosition pos) {
                return null;
            }
        };
    }

    private LineAndPointFormatter getFormatter() {
        LineAndPointFormatter seriesFormatter =
                new LineAndPointFormatter(this, R.xml.line_point_formatter_with_labels);
        seriesFormatter.setPointLabeler(
                (series, index) -> String.format(Locale.getDefault(), "%.2f", series.getY(index).doubleValue())
        );
        return seriesFormatter;
    }
}