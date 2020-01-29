package it.getfursu.gfithc.service;

import android.graphics.Color;

import androidx.annotation.ColorInt;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HealthCheckStatus {
    enum Status {
        ERROR  ("Error",   40, Color.RED),
        UNKNOWN("Unknown", 30, Color.argb(255, 255, 96, 0)),
        WARNING("Warning", 20, Color.YELLOW),
        OK     ("OK",      10, Color.GREEN);

        private final String description;
        private final int severity;
        private final @ColorInt int colorRepresentation;

        Status(String description, int severity, @ColorInt int colorRepresentation) {
            this.description = description;
            this.severity = severity;
            this.colorRepresentation = colorRepresentation;
        }

        public String getDescription() {
            return description;
        }

        public int getSeverity() {
            return severity;
        }

        public int getColorRepresentation() {
            return colorRepresentation;
        }

        public boolean isWorseThan(Status other) {
            return severity > other.severity;
        }

        public Status getWorse(Status other) {
            return isWorseThan(other) ? this : other;
        }

        public static Status fromString(String input) {
            if (OK.description.equalsIgnoreCase(input)) {
                return OK;
            }
            if (WARNING.description.equalsIgnoreCase(input)) {
                return WARNING;
            }
            if (ERROR.description.equalsIgnoreCase(input)) {
                return ERROR;
            }
            return UNKNOWN;
        }
    }

    public interface Listener {
        void update();
    }

    private static HealthCheckStatus INSTANCE;

    private final List<String> SUBITEMS = Arrays.asList("status", "cstStatus", "load", "memory", "disk");

    private List<Listener> listeners = new ArrayList<>();

    private String message = "Not checked yet";
    private JSONObject jsonResponse = new JSONObject();
    private Status status = Status.UNKNOWN;

    private HealthCheckStatus() {
    }

    public static synchronized HealthCheckStatus getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new HealthCheckStatus();
        }

        return INSTANCE;
    }

    public String getMessage() {
        return message;
    }

    public JSONObject getJsonResponse() {
        return jsonResponse;
    }

    boolean isOK() {
        return status == Status.OK;
    }

    Status getStatus() {
        return status;
    }

    void update(JSONObject jsonObject, String message) {
        this.jsonResponse = jsonObject;
        this.message = message;

        status = jsonReponseToStatusText(jsonObject);

        listeners.forEach(Listener::update);
    }

    private Status jsonReponseToStatusText(JSONObject jsonObject) {
        Status worst = Status.OK;

        if (jsonObject == null) {
            return Status.ERROR;
        }

        try {
            for (String item : SUBITEMS) {
                worst = worst.getWorse(Status.fromString(jsonObject.getString(item)));
            }
        } catch (JSONException e) {
            return Status.ERROR;
        }

        return worst;
    }

    public void registerListener(Listener listener) {
        this.listeners.add(listener);
    }

    public void unregisterListener(Listener listener) {
        this.listeners.remove(listener);
    }
}
