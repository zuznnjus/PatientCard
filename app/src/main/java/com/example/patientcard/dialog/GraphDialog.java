package com.example.patientcard.dialog;

import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.RadioButton;

import com.example.patientcard.R;
import com.example.patientcard.activity.GraphActivity;
import com.example.patientcard.domain.control.GraphDataHandler;
import com.example.patientcard.domain.control.PatientDataHandler;
import com.example.patientcard.domain.utils.GraphPoint;
import com.example.patientcard.domain.utils.IntentMessageCodes;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GraphDialog extends Dialog {

    public static final String BODY_HEIGHT_CODE = "Body Height";
    public static final String BODY_WEIGHT_CODE = "Body Weight";
    private final Context context;
    private final PatientDataHandler patientDataHandler;

    private RadioButton radioButtonHeight;
    private RadioButton radioButtonWeight;

    public GraphDialog(Context context, PatientDataHandler patientDataHandler) {
        super(context);
        this.context = context;
        this.patientDataHandler = patientDataHandler;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_pick_graph);

        radioButtonHeight = findViewById(R.id.radioButtonHeight);
        radioButtonWeight = findViewById(R.id.radioButtonWeight);

        Button buttonConfirm = findViewById(R.id.buttonConfirm);
        Button buttonCancel = findViewById(R.id.buttonCancel);

        buttonConfirm.setOnClickListener(v -> getMatchingDataThread().start());
        buttonCancel.setOnClickListener(v -> dismiss());
    }

    private Thread getMatchingDataThread() {
        return new Thread(() -> {
            Optional<GraphDataHandler> graphDataHandler = getProperGraphData();
            graphDataHandler.ifPresent(this::startGraphActivity);
        });
    }

    private Optional<GraphDataHandler> getProperGraphData() {
        if (radioButtonHeight.isChecked()) {
            return Optional.of(new GraphDataHandler(BODY_HEIGHT_CODE, patientDataHandler.getGraphData(BODY_HEIGHT_CODE)));
        } else if (radioButtonWeight.isChecked()) {
            return Optional.of(new GraphDataHandler(BODY_WEIGHT_CODE, patientDataHandler.getGraphData(BODY_WEIGHT_CODE)));
        }
        return Optional.empty();
    }

    private void startGraphActivity(GraphDataHandler graphDataHandler) {
        dismiss();
        Intent intent = new Intent(context, GraphActivity.class);
        intent.putExtra(IntentMessageCodes.GRAPH_DATA_MESSAGE, graphDataHandler);
        context.startActivity(intent);
    }
}
