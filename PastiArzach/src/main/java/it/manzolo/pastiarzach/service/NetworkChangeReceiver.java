package it.manzolo.pastiarzach.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

import it.manzolo.utils.ToolTip;

public class NetworkChangeReceiver extends BroadcastReceiver {
    public static boolean ACTIVE = false;

    @Override
    public void onReceive(final Context context, final Intent intent) {
        final ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final android.net.NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        final android.net.NetworkInfo mobile = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if (wifi.isAvailable() || mobile.isAvailable()) {
            ACTIVE = true;
            //Toast.makeText(context, "Connessione attiva", Toast.LENGTH_SHORT).show();
        } else {
            ACTIVE = false;
            new ToolTip(context, "Nessuna connessione a internet");
        }
    }
}