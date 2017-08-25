package com.example.akivabamberger.balloflightcontroller;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by akivabam.berger on 8/24/17.
 */

interface WifiControllerDelegate
{
    void onWifiControllerCallback();
}
public class WiFiController {
    public CurrentConnectionMode currentMode() {
        return connectionMode;
    }

    enum CurrentConnectionMode {
        UNCONNECTED,
        TRYING_NEW_HOST,
        ERROR_CONNECTING,
        CONNECTED
    }
    public static String SHARED_PREF_HOST_KEY = "Host";
    public static final String HOST_NAME = "BallOfLightHost";

    private static final String TAG = "WiFiController";
    private String host = null; //"10.29.6.73";
    private final String kLastCommandPath = "/lastCommand";
    private final String kUploadImagePath = "/uploadImage";
    private final long kMinimumTimeBetweenRequests = 5 * 1000; // 5 seconds
    private Date dateAtLastRequestSent;
    private RequestQueue requestQueue = null;
    private String currentStatus;
    private boolean isPolling = false;
    private Timer timer;
    private static WiFiController INSTANCE = new WiFiController();
    private CurrentConnectionMode connectionMode;
    private WifiControllerDelegate delegate;

    public static WiFiController getInstance() {
        return INSTANCE;
    }


    public void setContextAndDelegate(Context context, WifiControllerDelegate delegate) {
        if (delegate == null) {
            Log.e(TAG, "Delegate cannot be null!");
            return;
        }
        requestQueue = Volley.newRequestQueue(context);
        this.delegate = delegate;
    }


    private void requestLastCommandPage() {
        if (requestQueue == null || delegate == null) {
            throw new RuntimeException("Request queue and delegate cannot be null!");
        }
        if (host == null) {
            return;
        }
        connectionMode = CurrentConnectionMode.TRYING_NEW_HOST;
        delegate.onWifiControllerCallback();
        StringRequest stringRequest = new StringRequest(Request.Method.GET, getLastCommandUri(),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        currentStatus = "Saw response: " + response;
                        Log.d(TAG, "Updated current status: " + currentStatus);
                        connectionMode = CurrentConnectionMode.CONNECTED;
                        delegate.onWifiControllerCallback();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                currentStatus = "Saw error: " + error.getMessage() + " for URI " + getLastCommandUri();
                connectionMode = CurrentConnectionMode.ERROR_CONNECTING;;
                Log.d(TAG, "Updated current status: " + currentStatus);
                delegate.onWifiControllerCallback();
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
        if (connectionMode == CurrentConnectionMode.CONNECTED) {
            Log.d(TAG, "Sending down key for: " + text);
            String url = getLastCommandUri() + "?cmd=" + text + "&action=down";
            StringRequest req = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            });
            requestQueue.add(req);
        }
    }

    public void sendUpKeyCommand(CharSequence text) {
        if (connectionMode == CurrentConnectionMode.CONNECTED) {
            Log.d(TAG, "Sending up key for: " + text);
            String url = getLastCommandUri() + "?cmd=" + text + "&action=up";
            StringRequest req = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            });
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
        try {
            MultipartUtility multipartUtility = new MultipartUtility(getUploadImageUri());
            multipartUtility.addFilePart("imageFile", imageFile);
            multipartUtility.finish();
            dialog.dismiss();
        } catch (IOException e) {
            e.printStackTrace();
            dialog.dismiss();
        }
    }

    public void stop() {
        if (timer != null) {
            timer.cancel();
        }
        timer = null;
        isPolling = false;
    }

    public void setHost(String s) {
        host = s;

        tryToRequestLastCommandPage();
    }

    public String getHost() {
        return host;
    }
}
