package com.example.distrisandi;


import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;


/**
 * A simple {@link Fragment} subclass.
 */
public class pagosFragment extends Fragment {


    public pagosFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_pagos, container, false);

        View view = inflater.inflate(R.layout.fragment_pagos,container,false);
        Spinner provedors = (Spinner)view.findViewById(R.id.spinnerProovedores);
        String[] proovedores = {"ciente 1", "Cinete 2","Cliente 3","Cliente 4", "Cliente 5","cliente 6","Cliente 7", "Cliente 8","Cliente 9"};
        provedors.setAdapter(new ArrayAdapter<String>(getActivity(),android.R.layout.simple_spinner_item,proovedores));
        return view;
    }

}
