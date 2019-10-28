package it.getfursu.gfithc.service;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class HealthCheckStatus {
    public interface Listener {
        void update();
    }

    private static HealthCheckStatus INSTANCE;
    private List<Listener> listeners = new ArrayList<>();

    private String message = "Not checked yet";
    private JSONObject jsonResponse = new JSONObject();
    private boolean isOK = true;

    private HealthCheckStatus() {
    }

    public static synchronized HealthCheckStatus getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new HealthCheckStatus();
        }

        return INSTANCE;
    }

    public boolean isOK() {
        return isOK;
    }

    public String getMessage() {
        return message;
    }

    public JSONObject getJsonResponse() {
        return jsonResponse;
    }

    void update(JSONObject jsonObject, String message) {
        this.jsonResponse = jsonObject;
        this.message = message;

        try {
            this.isOK = jsonObject != null
                    && "OK".equals(jsonObject.getString("status"))
                    && "OK".equals(jsonObject.getString("cstStatus"))
                    && "OK".equals(jsonObject.getString("load"))
                    && "OK".equals(jsonObject.getString("memory"))
                    && "OK".equals(jsonObject.getString("disk"))
            ;
        } catch (JSONException e) {
            this.isOK = false;
        }

        listeners.forEach(Listener::update);
    }

    public void registerListener(Listener listener) {
        this.listeners.add(listener);
    }

    public void unregisterListener(Listener listener) {
        this.listeners.remove(listener);
    }
}
