package com.example.distrisandi;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;

public class pickdate extends DialogFragment{


    DatePickerDialog.OnDateSetListener onDateSetListener;
    private int year, month, day;

    public pickdate() {}
    public void setCallBack(DatePickerDialog.OnDateSetListener ondate) {
        this.onDateSetListener= ondate;
    }
    @SuppressLint("NewApi")
    @Override
    public void  setArguments(Bundle args) {
        super.setArguments(args);

        year = args.getInt("year");
        month = args.getInt("month");
        day = args.getInt("day");

    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        return new DatePickerDialog(getActivity(), onDateSetListener, year, month,day);
    }


    }

