package it.getfursu.gfithc.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import it.getfursu.gfithc.R;
import it.getfursu.gfithc.activities.MainActivity;

public class HealthCheckService extends Service implements HealthCheckStatus.Listener {
    private static final String NOTIFICATION_CHANNEL_ID = "LE_ID";
    private final static int NOTIFICATION_ID = 10;

    private HealthCheckThread thread;
    private NotificationManagerCompat notificationManager;
    private HealthCheckStatus status = HealthCheckStatus.getInstance();
    private boolean lastIsOkStatus = true;

    @Override
    public void onCreate() {
        thread = new HealthCheckThread(this);
        HealthCheckStatus.getInstance().registerListener(this);

        notificationManager = NotificationManagerCompat.from(this);

        createNotificationChannel();
        startForeground(NOTIFICATION_ID, createNotification());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        thread.startOnce();

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        notificationManager
                .createNotificationChannel(new NotificationChannel(
                        NOTIFICATION_CHANNEL_ID,
                        getString(R.string.channel_name),
                        NotificationManager.IMPORTANCE_DEFAULT)
                );
    }

    @Override
    public void onDestroy() {
        thread.interrupt();
    }

    private Notification createNotification() {
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,
                NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setPriority(status.isOK() ? NotificationCompat.PRIORITY_MIN : NotificationCompat.PRIORITY_MAX)
                .setContentText(status.isOK() ? "OK" : "ERROR")
                .setOnlyAlertOnce(status.isOK())
                .setContentIntent(contentIntent);

        return builder.build();
    }

    @Override
    public void update() {
        if (status.isOK() != lastIsOkStatus) {
            notificationManager.notify(NOTIFICATION_ID, createNotification());
        }

        lastIsOkStatus = status.isOK();
    }
}
