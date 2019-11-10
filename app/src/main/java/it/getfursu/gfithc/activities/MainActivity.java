package it.getfursu.gfithc.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import it.getfursu.gfithc.R;
import it.getfursu.gfithc.service.HealthCheckService;
import it.getfursu.gfithc.service.HealthCheckStatus;

public class MainActivity extends AppCompatActivity implements HealthCheckStatus.Listener {
    private TextView txvHcResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txvHcResult = findViewById(R.id.txvHcResult);

        HealthCheckStatus.getInstance().registerListener(this);
        refresh();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        HealthCheckStatus.getInstance().unregisterListener(this);
    }

    public void onStartServiceClick(View view) {
        startForegroundService(new Intent(this, HealthCheckService.class));
    }

    public void onStopServiceClick(View view) {
        Intent intent = new Intent(this, HealthCheckService.class);
        intent.setAction(HealthCheckService.ACTION_STOP);
        startForegroundService(intent);
    }

    @Override
    public void update() {
        refresh();
    }

    private void refresh() {
        txvHcResult.setText(HealthCheckStatus.getInstance().getMessage().replace("\",\"", "\",\n\""));
    }
}
