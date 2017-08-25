package com.example.akivabamberger.balloflightcontroller;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.GridLayout;
import android.widget.RelativeLayout;
import android.widget.ToggleButton;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ControllerFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ControllerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ControllerFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = "ControllerFragment";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private ArrayList<ToggleButton> buttons;
    private CompoundButton.OnCheckedChangeListener checkListener;


    public ControllerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        buttons = new ArrayList<>();

        checkListener = new ToggleButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    disableAllButtonsBeside(compoundButton);
                    WiFiController.getInstance().sendDownKeyCommand(compoundButton.getText());
                } else {
                    WiFiController.getInstance().sendUpKeyCommand(compoundButton.getText());
                }
            }
        };
    }

    private void disableAllButtonsBeside(CompoundButton button) {
        for (ToggleButton b : buttons) {
            if (b.getText().equals(button.getText())) {
                continue;
            }
            if (b.isChecked()) {
                b.setChecked(false);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_controller, container, false);
        GridLayout layout = (GridLayout) view.findViewById(R.id.controllerLayout);
        for (char i = 'A'; i <= 'Z'; i++) {
            ToggleButton b = new ToggleButton(getContext());
            b.setText("" + i);
            b.setTextOn("" + i);
            b.setTextOff("" + i);
            b.setWidth(50);
            b.setHeight(50);

            layout.addView(b, i - 'A');
            buttons.add(b);
            b.setOnCheckedChangeListener(checkListener);
        }
        WiFiController.getInstance().updateTextView(view);
        return view;
    }


    @Override
    public void onDetach() {
        super.onDetach();
    }
}
