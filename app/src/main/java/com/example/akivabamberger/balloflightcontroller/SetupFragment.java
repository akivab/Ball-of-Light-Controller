package com.example.akivabamberger.balloflightcontroller;

import android.content.Context;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SetupFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SetupFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SetupFragment extends Fragment {
    public SetupFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_setup, container, false);
        WiFiController.getInstance().updateTextView(view);

        EditText et = (EditText)view.findViewById(R.id.editText2);
        String host = WiFiController.getInstance().getHost();
        if (host != null)  {
            et.setText(host);
        }

        Button bt = (Button) view.findViewById(R.id.button3);
        final EditText editText = (EditText) view.findViewById(R.id.editText2);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WiFiController.getInstance().setHost(editText.getText().toString());
            }
        });
        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
