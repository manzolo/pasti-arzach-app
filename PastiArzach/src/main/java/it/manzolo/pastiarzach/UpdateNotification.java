package it.manzolo.pastiarzach;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import it.manzolo.utils.Internet;

public class UpdateNotification {
    private Context context;

    public UpdateNotification(Context ctx) {
        try {
            this.context = ctx;
            String versionCode;
            versionCode = this.context.getPackageManager().getPackageInfo(this.context.getPackageName(), 0).versionName;
            String webVersion = new Internet(ArzachUrls.APP_VERSION_PAGE).getResponse();
            //Log.i("cur",versionCode);Log.i("web",webVersion);
            if (Float.parseFloat(webVersion) > Float.parseFloat(versionCode)) {
                NotifyNewRelease();
            }
        } catch (NameNotFoundException e) {
            // Se non si raggiunge pace, si controllera' la prossima volta
        } catch (Exception e) {
            // Se non si raggiunge pace, si controllera' la prossima volta
            //e.printStackTrace();
        }

    }

    protected void NotifyNewRelease() {

        NotificationManager notificationManager = (NotificationManager) this.context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this.context);

        // Titolo e testo della notifica
        notificationBuilder.setContentTitle("Osteria di Arzach");
        notificationBuilder.setContentText("E' disponibile la nuova versione!!");

        // Testo che compare nella barra di stato non appena compare la notifica
        notificationBuilder.setTicker("Arzach nuova versione!");

        // Data e ora della notifica
        notificationBuilder.setWhen(System.currentTimeMillis());

        // Icona della notifica
        notificationBuilder.setSmallIcon(R.drawable.pasto);

        notificationBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));


        // Creiamo il pending intent che verra' lanciato quando la notifica
        // viene premuta

        Intent notificationIntent = new Intent(Intent.ACTION_VIEW);
        notificationIntent.setData(Uri.parse(ArzachUrls.APP_DOWNLOAD_PAGE));
        PendingIntent pendingIntent = PendingIntent.getActivity(this.context, 0, notificationIntent, 0);
        notificationBuilder.setContentIntent(pendingIntent);


        // Impostiamo il suono, le luci e la vibrazione di default
        notificationBuilder.setDefaults(Notification.DEFAULT_SOUND | Notification.FLAG_SHOW_LIGHTS | Notification.DEFAULT_VIBRATE);
        //notificationBuilder.setLights(Color.GRAY, 100, 100);
        notificationBuilder.setAutoCancel(true);
        notificationManager.notify(NotificationParameters.NOTIFICATION_NEWVERSION_ID, notificationBuilder.build());

    }

}
