package com.example.patientcard.activity;

import android.app.DatePickerDialog;
import android.content.Context;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;

import java.time.LocalDate;

public class DateDialog implements View.OnClickListener, DatePickerDialog.OnDateSetListener {

    private final Context context;
    private final EditText editText;
    private LocalDate date;

    public DateDialog(Context context, EditText editText) {
        this.context = context;
        this.editText = editText;
        editText.setOnClickListener(this);
        this.date = LocalDate.now();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        date = date.withYear(year)
                .withMonth(month + 1)
                .withDayOfMonth(dayOfMonth);

        editText.setText(String.format("%s-%s-%s",
                date.getDayOfMonth(),
                date.getMonthValue(),
                date.getYear()));
    }

    @Override
    public void onClick(View v) {
        new DatePickerDialog(context,
                this,
                date.getYear(),
                date.getMonthValue() - 1,
                date.getDayOfMonth())
                .show();
    }

    public EditText getEditText() {
        return editText;
    }
}
