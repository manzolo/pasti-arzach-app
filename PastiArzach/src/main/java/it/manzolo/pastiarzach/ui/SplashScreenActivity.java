package it.manzolo.pastiarzach.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import it.manzolo.pastiarzach.R;
import it.manzolo.pastiarzach.service.NetworkChangeReceiver;
import it.manzolo.pastiarzach.service.NotificationService;
import it.manzolo.pastiarzach.util.SystemUiHider;
import it.manzolo.utils.Internet;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */

public class SplashScreenActivity extends Activity {

    // Splash screen timer
    private static int SPLASH_TIME_OUT = 500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        startService(new Intent(this, NotificationService.class));
        new Handler().postDelayed(new Runnable() {
            /*
             * Showing splash screen with a timer. This will be useful when you
             * want to show case your app logo / company
             */
            @Override
            public void run() {
                // This method will be executed once the timer is over
                // Start your app main activity
                NetworkChangeReceiver.ACTIVE = Internet.isNetworkAvailable(getApplicationContext());

                Intent i = new Intent(SplashScreenActivity.this, MainActivity.class);
                startActivity(i);
                // close this activity
                finish();
            }
        }, SPLASH_TIME_OUT);
    }

}
