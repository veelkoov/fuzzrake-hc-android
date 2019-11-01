package it.getfursu.gfithc.service;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

final class HealthCheckThread extends Thread implements Response.Listener<JSONObject>, Response.ErrorListener {
    private static final String URL = "https://getfursu.it/health";
    private static final Object TAG = new Object();
    private static final int CHECK_INTERVAL_IN_MILLIS = 5 * 60 * 1000; // 5 minutes

    private final HealthCheckStatus status;
    private RequestQueue requestQueue;

    HealthCheckThread(Context context) {
        requestQueue = Volley.newRequestQueue(context);
        status = HealthCheckStatus.getInstance();
    }

    @Override
    public void run() {
        while (!isInterrupted()) {
            try {
                makeHcCall();

                Thread.sleep(CHECK_INTERVAL_IN_MILLIS);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        String message;

        if (null != error.networkResponse) {
            message = new String(error.networkResponse.data);
        } else if (null != error.getCause()) {
            message = error.getCause().getMessage();
        } else if (null != error.getMessage()) {
            message = error.getMessage();
        } else {
            message = "Unknown error";
        }

        status.update(new JSONObject(), message);
    }

    @Override
    public void onResponse(JSONObject response) {
        status.update(response, response.toString());
    }

    synchronized void startOnce() {
        if (getState() == State.NEW) {
            start();
        }
    }

    private void makeHcCall() {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, URL,
                null, this, this);
        request.setTag(TAG);

        requestQueue.cancelAll(TAG);
        requestQueue.add(request);
    }
}
