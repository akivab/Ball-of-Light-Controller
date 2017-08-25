package com.example.akivabamberger.balloflightcontroller;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;


import com.example.akivabamberger.balloflightcontroller.dummy.DummyContent;

import java.util.List;

import static com.example.akivabamberger.balloflightcontroller.ActionDetailFragment.ARG_ITEM_ID;
import static com.example.akivabamberger.balloflightcontroller.WiFiController.HOST_NAME;
import static com.example.akivabamberger.balloflightcontroller.WiFiController.SHARED_PREF_HOST_KEY;

/**
 * An activity representing a list of Actions. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link ActionDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class ActionListActivity extends AppCompatActivity implements WifiControllerDelegate {

    private boolean shouldShowUploadImage;

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_action_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        View recyclerView = findViewById(R.id.action_list);
        assert recyclerView != null;
        setupRecyclerView((RecyclerView) recyclerView);

        final SharedPreferences settings = getApplicationContext().getSharedPreferences(HOST_NAME, MODE_PRIVATE);


        WiFiController.getInstance().stop();
        WiFiController.getInstance().setContextAndDelegate(getApplicationContext(), this);
        String prefHost = settings.getString(SHARED_PREF_HOST_KEY, null);
        if (prefHost != null) {
            WiFiController.getInstance().setHost(prefHost);
        }
        WiFiController.getInstance().startPolling();

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if (type.startsWith("image/")) {
                handleSendImage(intent); // Handle single image being sent
            }
        }
    }

    public void onWifiControllerCallback() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView tv = (TextView) findViewById(R.id.textView);
                updateWifiStateTextView(tv);
            }
        });
    }

    public static void updateWifiStateTextView(TextView tv) {
        if (tv == null) {
            return;
        }
        if (WiFiController.getInstance().currentMode() == WiFiController.CurrentConnectionMode.CONNECTED) {
            tv.setText("Connected to " + WiFiController.getInstance().getHost());
            tv.setTextColor(Color.rgb(39, 139, 34));
        } else if (WiFiController.getInstance().currentMode() == WiFiController.CurrentConnectionMode.TRYING_NEW_HOST) {
            tv.setText("Trying to connect to " + WiFiController.getInstance().getHost());
            tv.setTextColor(Color.DKGRAY);
        } else if (WiFiController.getInstance().currentMode() == WiFiController.CurrentConnectionMode.ERROR_CONNECTING) {
            tv.setText("Error connecting to " + WiFiController.getInstance().getHost());
            tv.setTextColor(Color.rgb(128, 0, 0));
        } else if (WiFiController.getInstance().currentMode() == WiFiController.CurrentConnectionMode.UNCONNECTED) {
            tv.setText("Not Connected to Ball");
            tv.setTextColor(Color.LTGRAY);
        }
    }

    private void handleSendImage(Intent intent) {
        Uri imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (imageUri != null) {
            ImageManipulator.setSavedImageFromUri(imageUri, this);
            shouldShowUploadImage = true;
        }

    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(DummyContent.ITEMS));
    }

    public class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private final List<DummyContent.DummyItem> mValues;
        private ViewHolder lastHeld = null;
        public SimpleItemRecyclerViewAdapter(List<DummyContent.DummyItem> items) {
            mValues = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.action_list_content, parent, false);
            return new ViewHolder(view);
        }

        void handleClick(final ViewHolder holder) {
            Bundle args = new Bundle();
            args.putString(ARG_ITEM_ID, "" + holder.mItem.id);
            Fragment fragment = holder.getFragment();
            fragment.setArguments(args);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.action_detail_container, fragment)
                    .commit();
            if (lastHeld != null) {
                lastHeld.mView.setBackgroundColor(Color.WHITE);
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(lastHeld.mView.getWindowToken(), 0);
            }
            holder.mView.setBackgroundColor(Color.LTGRAY);
            lastHeld = holder;
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.mItem = mValues.get(position);
            holder.mContentView.setText(mValues.get(position).content);

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handleClick(holder);
                }
            });
            if (!shouldShowUploadImage && position == 0) {
                handleClick(holder);
            } else if (shouldShowUploadImage && position == 2) {
                handleClick(holder);
            }
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final TextView mIdView;
            public final TextView mContentView;
            public DummyContent.DummyItem mItem;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mIdView = (TextView) view.findViewById(R.id.id);
                mContentView = (TextView) view.findViewById(R.id.content);
            }

            public Fragment getFragment() {
                switch(mItem.id) {
                    case 1:
                        return new SetupFragment();
                    case 2:
                        return new ControllerFragment();
                    case 3:
                        return new ImageUploadFragment();
                }
                return new ActionDetailFragment();
            }

            @Override
            public String toString() {
                return super.toString() + " '" + mContentView.getText() + "'";
            }
        }
    }
}
