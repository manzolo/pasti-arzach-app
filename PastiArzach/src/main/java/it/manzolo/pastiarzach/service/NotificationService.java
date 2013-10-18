package it.manzolo.pastiarzach.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import it.manzolo.pastiarzach.Ordine;
import it.manzolo.pastiarzach.R;
import it.manzolo.pastiarzach.database.DbNotificheAdapter;
import it.manzolo.pastiarzach.ui.MainActivity;

public class NotificationService extends Service {
    private WakeLock mWakeLock;

    /**
     * * Simply return null, since our Service will not be communicating with *
     * any other components. It just does its work silently.
     */
    @Override
    public IBinder onBind(Intent intent) {
        Log.i("StartService", "Bind");
        return null;
    }

    /**
     * * This is where we initialize. We call this when onStart/onStartCommand
     * is * called by the system. We won't do anything with the intent here, and
     * you * probably won't, either.
     */
    private void handleIntent(Intent intent) {
        // obtain the wake lock PowerManager
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "arzachTag");
        mWakeLock.acquire();
        // check the global background data setting
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if (!cm.getActiveNetworkInfo().isConnected()) {
            stopSelf();
            return;
        }
        // do the actual work, in a separate thread
        new PollTask().execute();
    }

    private class PollTask extends AsyncTask<Void, Void, Void> {
        protected static final int SIMPLE_NOTIFICATION_ID = 999;

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
                    SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy", Locale.ITALY);
                    String formattedDate = df.format(calendar.getTime());
                    int day = calendar.get(Calendar.DAY_OF_WEEK);
                    int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);

                    //Log.i("Controllo menu","Controllo");
                    //Solo se e' martedi o giovedi si cerca il menu
                    if ((hour >= 9 && hour <= 11) && (day == Calendar.THURSDAY || day == Calendar.TUESDAY)) {
                        //if ((hour >= 19 && hour <=20 ) && (day==Calendar.SUNDAY || day==Calendar.MONDAY)){
                        //Log.i("Controllo menu","Si");
                        Ordine ordine = new Ordine();
                        DbNotificheAdapter dbNotificheAdapter = new DbNotificheAdapter(NotificationService.this);
                        dbNotificheAdapter.open();
                        boolean alreadyMenuNotify = dbNotificheAdapter.NotificationByDateExists(formattedDate);
                        dbNotificheAdapter.close();

                        if (ordine.isDisponibile() && ordine.isAperto() && !alreadyMenuNotify) {
                            //Log.i("arzach", "Service running");
                            NotifyMenu();

                            dbNotificheAdapter.open();
                            dbNotificheAdapter.createNotification(formattedDate);
                            dbNotificheAdapter.close();
                        }

                    } else {

                    }
                    stopSelf();
                }
            };

            Thread t = new Thread(r);
            t.start();
            //return Service.START_STICKY;
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

        protected void NotifyMenu() {
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(NotificationService.this);

            // Titolo e testo della notifica
            notificationBuilder.setContentTitle("Osteria di Arzach");
            notificationBuilder.setContentText("Il menu di oggi e' disponibile!!");

            // Testo che compare nella barra di stato non appena compare la notifica
            notificationBuilder.setTicker("Il menu di Arzach e' disponibile!!");

            // Data e ora della notifica
            notificationBuilder.setWhen(System.currentTimeMillis());

            // Icona della notifica
            notificationBuilder.setSmallIcon(R.drawable.menu);

            notificationBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));


            // Creiamo il pending intent che verra' lanciato quando la notifica
            // viene premuta

            Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(NotificationService.this, 0, notificationIntent, 0);
            notificationBuilder.setContentIntent(pendingIntent);


            // Impostiamo il suono, le luci e la vibrazione di default
            notificationBuilder.setDefaults(Notification.DEFAULT_SOUND | Notification.FLAG_SHOW_LIGHTS | Notification.DEFAULT_VIBRATE);
            notificationBuilder.setLights(Color.GRAY, 100, 100);

            notificationBuilder.setAutoCancel(true);

            mNotificationManager.notify(SIMPLE_NOTIFICATION_ID,
                    notificationBuilder.build());
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
        mWakeLock.release();
    }

}
