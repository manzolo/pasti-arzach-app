package it.manzolo.pastiarzach.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

import java.util.Calendar;

public class ScheduleNotificationService extends Service {
    /**
     * * Simply return null, since our Service will not be communicating with *
     * any other components. It just does its work silently.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * * This is where we initialize. We call this when onStart/onStartCommand
     * is * called by the system. We won't do anything with the intent here, and
     * you * probably won't, either.
     */
    private void handleIntent(Intent intent) {
        // do the actual work, in a separate thread
        new PollTask().execute();
    }

    private class PollTask extends AsyncTask<Void, Void, Void> {

        /**
         * * This is where YOU do YOUR work. There's nothing for me to write
         * here * you have to fill this in. Make your HTTP request(s) or
         * whatever it is * you have to do to get your updates in here, because
         * this is run in a * separate thread
         */

        @Override
        protected Void doInBackground(Void... params) {
            // do stuff!
            Runnable r = new Runnable() {
                public void run() {
                    Calendar calendar = Calendar.getInstance();
                    Log.i("ManzoloSet", "Schedulazione impostata alle " + ScheduleOptions.ORA + " e " + ScheduleOptions.MINUTO);

                    //calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                    calendar.set(Calendar.HOUR_OF_DAY, ScheduleOptions.ORA);
                    calendar.set(Calendar.MINUTE, ScheduleOptions.MINUTO);
                    calendar.set(Calendar.SECOND, ScheduleOptions.SECONDO);
                    ScheduleOptions.INTERVAL = ScheduleOptions.DEFAULT_INTERVAL;

                    PendingIntent pi = PendingIntent.getService(ScheduleNotificationService.this, 0,
                            new Intent(ScheduleNotificationService.this, CheckNotificationService.class), PendingIntent.FLAG_UPDATE_CURRENT);
                    AlarmManager am = (AlarmManager) ScheduleNotificationService.this.getSystemService(ScheduleNotificationService.this.ALARM_SERVICE);
                    am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                            AlarmManager.INTERVAL_DAY, pi);
                    stopSelf();
                }
            };

            Thread t = new Thread(r);
            t.start();
            return null;
        }

        /**
         * * In here you should interpret whatever you fetched in doInBackground
         * * and push any notifications you need to the status bar, using the *
         * NotificationManager. I will not cover this here, go check the docs on
         * * NotificationManager. * * What you HAVE to do is call stopSelf()
         * after you've pushed your * notification(s). This will: * 1) Kill the
         * service so it doesn't waste precious resources * 2) Call onDestroy()
         * which will release the wake lock, so the device * can go to sleep
         * again and save precious battery.
         */
        @Override
        protected void onPostExecute(Void result) {
            // handle your data
            stopSelf();
        }
    }

    /**
     * * This is deprecated, but you have to implement it if you're planning on
     * * supporting devices with an API level lower than 5 (Android 2.0).
     */
    @Override
    public void onStart(Intent intent, int startId) {
        handleIntent(intent);
    }

    /**
     * * This is called on 2.0+ (API level 5 or higher). Returning *
     * START_NOT_STICKY tells the system to not restart the service if it is *
     * killed because of poor resource (memory/cpu) conditions.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("ManzoloScheduleService", "Start");
        handleIntent(intent);
        return START_NOT_STICKY;
    }

    /**
     * * In onDestroy() we release our wake lock. This ensures that whenever the
     * * Service stops (killed for resources, stopSelf() called, etc.), the wake
     * * lock will be released.
     */
    public void onDestroy() {
        super.onDestroy();
    }

}
