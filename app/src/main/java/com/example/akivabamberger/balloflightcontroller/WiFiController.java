package com.example.akivabamberger.balloflightcontroller;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by akivabam.berger on 8/24/17.
 */

public class WiFiController {
    private static final String TAG = "WiFiController";
    private String host = null; //"10.29.6.73";
    private final String kLastCommandPath = "/lastCommand";
    private final String kUploadImagePath = "/uploadImage";
    private final long kMinimumTimeBetweenRequests = 5 * 1000; // 10 seconds
    private Date dateAtLastRequestSent;
    private RequestQueue requestQueue = null;
    private String currentStatus;
    private boolean isPolling = false;
    private Timer timer;
    private static WiFiController INSTANCE = new WiFiController();
    private boolean isConnected;

    public static WiFiController getInstance() {
        return INSTANCE;
    }


    public void setContext(Context context) {
        requestQueue = Volley.newRequestQueue(context);
    }

    private void requestLastCommandPage() {
        if (requestQueue == null) {
            throw new RuntimeException("Request queue cannot be null!");
        }
        if (host == null) {
            return;
        }
        StringRequest stringRequest = new StringRequest(Request.Method.GET, getLastCommandUri(),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        currentStatus = "Saw response: " + response;
                        Log.d(TAG, "Updated current status: " + currentStatus);
                        isConnected = true;
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                currentStatus = "Saw error: " + error.getMessage() + " for URI " + getLastCommandUri();
                isConnected = false;
                Log.d(TAG, "Updated current status: " + currentStatus);
            }
        });
        requestQueue.add(stringRequest);
    }

    private void tryToRequestLastCommandPage() {
        Date now = Calendar.getInstance().getTime();
        if (dateAtLastRequestSent == null || now.getTime() - dateAtLastRequestSent.getTime() > kMinimumTimeBetweenRequests) {
            dateAtLastRequestSent = now;
            requestLastCommandPage();
        }
    }

    public void startPolling() {
        if (isPolling) {
            throw new RuntimeException("Should only poll once!");
        }
        isPolling = true;
        timer = new Timer();

        TimerTask doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                tryToRequestLastCommandPage();
            }
        };
        timer.schedule(doAsynchronousTask, 0, kMinimumTimeBetweenRequests);
    }

    private String getLastCommandUri() {
        return "http://" + this.host + ":8080" + kLastCommandPath;
    }
    private String getUploadImageUri() {
        return "http://" + this.host + ":8080" + kUploadImagePath;
    }



    public void sendDownKeyCommand(CharSequence text) {
        if (isConnected) {
            Log.d(TAG, "Sending down key for: " + text);
            String url = getLastCommandUri() + "?cmd=" + text + "&action=down";
            StringRequest req = new StringRequest(Request.Method.GET, url, null, null);
            requestQueue.add(req);
        }
    }

    public void sendUpKeyCommand(CharSequence text) {
        if (isConnected) {
            Log.d(TAG, "Sending up key for: " + text);
            String url = getLastCommandUri() + "?cmd=" + text + "&action=up";
            StringRequest req = new StringRequest(Request.Method.GET, url, null, null);
            requestQueue.add(req);
        }
    }

    public void uploadLatestImage(final File imageFile, final Activity activity) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final ProgressDialog dialog = new ProgressDialog(activity); // this = YourActivity
                dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                dialog.setMessage("Uploading...");
                dialog.setIndeterminate(true);
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();

                Thread th = new Thread() {
                    @Override
                    public void run() {

                        Log.d(TAG, "Uploading image at path: " + imageFile.getAbsolutePath());
                        uploadUserPhoto(imageFile, dialog);
                    }
                };
                th.start();
            }
        });
    }


    public void uploadUserPhoto(final File imageFile, final ProgressDialog dialog) {
        VolleyMultipartRequest req = new VolleyMultipartRequest(Request.Method.POST, getUploadImageUri(), new Response.Listener<NetworkResponse>() {
            @Override
            public void onResponse(NetworkResponse response) {
                dialog.dismiss();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                dialog.dismiss();
            }
        }) {
            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                try {
                    byte[] bytes = readFile(imageFile);
                    params.put("imageFile", new DataPart(imageFile.getName(), bytes));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return params;
            }
        };
        requestQueue.add(req);
    }

    public static byte[] readFile(File file) throws IOException {
        // Open file
        RandomAccessFile f = new RandomAccessFile(file, "r");
        try {
            // Get and check length
            long longlength = f.length();
            int length = (int) longlength;
            if (length != longlength)
                throw new IOException("File size >= 2 GB");
            // Read file and return data
            byte[] data = new byte[length];
            f.readFully(data);
            return data;
        } finally {
            f.close();
        }
    }


    public void stop() {
        if (timer != null) {
            timer.cancel();
        }
        timer = null;
        isPolling = false;
    }

    public void updateTextView(View v) {
        TextView tv = (TextView) v.findViewById(R.id.textView);
        if (isConnected) {
            tv.setText("Connected to Ball @" + host);
            tv.setTextColor(Color.GREEN);
        } else {
            tv.setText("Not Connected to Ball");
            tv.setTextColor(Color.LTGRAY);
        }
    }

    public void setHost(String s) {
        host = s;
        tryToRequestLastCommandPage();
    }

    public String getHost() {
        return host;
    }
}
