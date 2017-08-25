package com.example.akivabamberger.balloflightcontroller;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import static android.content.Context.MODE_PRIVATE;
import static com.example.akivabamberger.balloflightcontroller.WiFiController.HOST_NAME;
import static com.example.akivabamberger.balloflightcontroller.WiFiController.SHARED_PREF_HOST_KEY;

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

    void updateHost(EditText editText, SharedPreferences settings) {
        String host = editText.getText().toString();
        if (!isValidIP(host)) {
            Toast.makeText(getContext(), host + " is not a valid IP", Toast.LENGTH_SHORT).show();
            return;
        }
        WiFiController.getInstance().setHost(host);
        SharedPreferences.Editor prefEditor = settings.edit();
        prefEditor.putString(SHARED_PREF_HOST_KEY, host);
        prefEditor.apply();
    }

    private boolean isValidIP(String host) {
        return Patterns.IP_ADDRESS.matcher(host).matches();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_setup, container, false);
        final SharedPreferences settings = getContext().getSharedPreferences(HOST_NAME, MODE_PRIVATE);

        final EditText et = (EditText)view.findViewById(R.id.editText2);
        et.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                if ((keyEvent.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    updateHost(et, settings);
                }
                return false;
            }
        });
        String host = WiFiController.getInstance().getHost();
        if (host != null)  {
            et.setText(host);
        }


        Button bt = (Button) view.findViewById(R.id.button3);
        final EditText editText = (EditText) view.findViewById(R.id.editText2);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateHost(editText, settings);
            }
        });
        ActionListActivity.updateWifiStateTextView((TextView) view.findViewById(R.id.textView));
        return view;
    }
}
