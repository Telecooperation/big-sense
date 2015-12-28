package de.orolle.bigsense;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import de.orolle.bigsense.update.RestartService;
import de.orolle.bigsense.update.UpdateService;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_layout);

        try {
            stopService(new Intent(this, UpdateService.class));
            stopService(new Intent(this, RestartService.class));
        }
        catch (Exception e) {
        }
        startService(new Intent(this, UpdateService.class));
        startService(new Intent(this, RestartService.class));

        Button retryButton = (Button) findViewById(R.id.retry_button);
        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopService(new Intent(getApplicationContext(), UpdateService.class));
                stopService(new Intent(getApplicationContext(), RestartService.class));
                startService(new Intent(getApplicationContext(), UpdateService.class));
                startService(new Intent(getApplicationContext(), RestartService.class));
            }
        });
    }
}
