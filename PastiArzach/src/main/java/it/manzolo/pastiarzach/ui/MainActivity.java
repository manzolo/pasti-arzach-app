package it.manzolo.pastiarzach.ui;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.text.Html;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

import it.manzolo.pastiarzach.Ordine;
import it.manzolo.pastiarzach.R;
import it.manzolo.pastiarzach.UpdateNotification;
import it.manzolo.pastiarzach.parameters.ArzachUrls;
import it.manzolo.pastiarzach.parameters.Parameters;
import it.manzolo.pastiarzach.service.NetworkChangeReceiver;
import it.manzolo.pastiarzach.service.NotificationService;
import it.manzolo.utils.Internet;
import it.manzolo.utils.ToolTip;

public class MainActivity extends Activity {

    static final String NETWORK_CONNECTION_CHANGE = "it.manzolo.pastiarzach.NetworkConnectionChange";
    static boolean LAST_NETWORKSTATUS;
    TextView tvConnectionStatus;
    LinearLayout messageContext;
    static final int MINUTI_CONTROLLO_MENU = 10;
    private final BroadcastReceiver networkChangeReceiver = new NetworkChangeReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Just for testing, allow network access in the main thread, NEVER use
        // this is productive code
        // StrictMode.ThreadPolicy policy = new
        // StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitNetwork().build();
        StrictMode.setThreadPolicy(policy);


        setContentView(R.layout.activity_main);
        //Si controlla la presenza di nuove versioni della app
        if (NetworkChangeReceiver.ACTIVE) {
            new UpdateNotification(MainActivity.this);
        }

        // Box per contenere i messaggi
        messageContext = (LinearLayout) findViewById(R.id.scrollLinearLayout);

        // Si impostano gli attributi della text per lo stato della connessione
        tvConnectionStatus = (TextView) findViewById(R.id.connectionStatus);
        tvConnectionStatus.setTextColor(Color.WHITE);
        tvConnectionStatus.setBackgroundColor(Color.BLACK);
        tvConnectionStatus.setGravity(Gravity.CENTER_HORIZONTAL);

        String lastMatricola = new Parameters(this).loadMatricola();
        EditText txtMatricola = (EditText) findViewById(R.id.matricola);
        txtMatricola.setText(lastMatricola);
        // Receiver per ricevere l'evento che lo stato della connessione e'
        // cambiato
        registerReceiver(updateConnectionStatusUiReceiver, new IntentFilter(
                NETWORK_CONNECTION_CHANGE));

        Timer timerConnectionState = new Timer();
        TimerTask timerTaskConnectionState = new TimerTask() {
            Intent intentConnectionState = new Intent(NETWORK_CONNECTION_CHANGE);

            @Override
            public void run() {
                if (LAST_NETWORKSTATUS != NetworkChangeReceiver.ACTIVE) {
                    // boolean NetworkActive = NetworkState.ACTIVE;
                    // intentConnectionState.putExtra("ActiveConnection",
                    // NetworkActive);
                    sendBroadcast(intentConnectionState);

                    LAST_NETWORKSTATUS = NetworkChangeReceiver.ACTIVE;
                }

            }
        };

        timerConnectionState.scheduleAtFixedRate(timerTaskConnectionState, 0,
                1000);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.refresh:
                messagesRefresh();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        // Se cambia il focus della finestra
        if (hasFocus) {
            // Se e' cambiato lo stato della connessione
            if (LAST_NETWORKSTATUS != NetworkChangeReceiver.ACTIVE) {
                messagesRefresh();
                LAST_NETWORKSTATUS = NetworkChangeReceiver.ACTIVE;
            }

            if (NetworkChangeReceiver.ACTIVE) {
                tvConnectionStatus.setVisibility(View.GONE);
            }
        } else {
            // Losefocus
        }

    }

    /**
     * Viene chiamato appena si clicca su accedi
     */
    public void login(View view) {
        try {

            Intent intent = new Intent(this, LoginActivity.class);
            EditText txtMatricola = (EditText) findViewById(R.id.matricola);
            String matricola = txtMatricola.getText().toString();
            if (matricola.length() == 0) {
                new ToolTip(this, "Inserisci prima la matricola");
                return;
            }

            if (NetworkChangeReceiver.ACTIVE) {
                intent.putExtra("matricola", matricola);
                startActivity(intent);
            } else {
                new ToolTip(this, "Nessuna connessione ad internet disponibile");
            }

        } catch (Exception e) {
            new ToolTip(this,
                    "Impossibile stabilire una connessione con il server");
            return;
        }

    }

    private void messagesRefresh() {

        try {
            final ProgressDialog dialog = ProgressDialog.show(
                    MainActivity.this, "", "Elaborazione dati..", true);
            dialog.show();
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    messageContext.removeAllViews();
                    GetMessages();
                    dialog.dismiss();
                }
            }, 3000); // 3000 milliseconds
        } catch (Exception e) {
            new ToolTip(this, "Impossibile comunicare con il server", true);
        }

    }

    private void GetMessages() {
        // StackTraceElement[] stackTraceElements =
        // Thread.currentThread().getStackTrace();
        // Si legge lo stato dell'ordine
        try {
            Ordine ordine = new Ordine();
            writeMessages(Integer.parseInt(ordine.getStato()),
                    ordine.getMessaggio());

        } catch (Exception e) {
            new ToolTip(this, "Impossibile determinare lo stato dell'ordine");
        }

        // Si leggono i messaggi scritti da Arzach
        try {
            JSONArray jsonArrayMessages = new Internet(
                    ArzachUrls.CHECK_MESSAGES_PAGE).getJSONArray();
            for (int i = 0; i < jsonArrayMessages.length(); i++) {
                JSONObject jsonObjectSingleMessage = jsonArrayMessages
                        .getJSONObject(i);
                writeMessages(Integer.parseInt(jsonObjectSingleMessage
                        .getString("retcode")),
                        jsonObjectSingleMessage.getString("messaggio"));
            }

        } catch (Exception e) {
            new ToolTip(this, "Impossibile scaricare eventuali messaggi di arzach");
            //Log.e("qui", e.getMessage());
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        onActivityResume();
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onResume();
        onActivityResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        //Si deregistra il receiver se cambia la connessione
        unregisterReceiver(networkChangeReceiver);
    }

    private void onActivityResume() {
        try {
            //Si registra il receiver se cambia la connessione
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
            registerReceiver(networkChangeReceiver, filter);

            //Si fa partire il controllo ogni x minuti per controllare se il menu e' disponibile o meno
            SharedPreferences prefs = PreferenceManager
                    .getDefaultSharedPreferences(this);
            int minutes = prefs.getInt("interval", MINUTI_CONTROLLO_MENU);
            AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
            Intent i = new Intent(this, NotificationService.class);
            PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
            am.cancel(pi);
            // by my own convention, minutes <= 0 means notifications are
            // disabled
            if (minutes > 0) {
                am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        SystemClock.elapsedRealtime() + minutes * 60 * 1000,
                        minutes * 60 * 1000, pi);
            }

            // Quando riprende il focus questa Activity si imposta il focus
            // sulla matricola attivando la tastiera
            final TextView tMatricola = (TextView) findViewById(R.id.matricola);
            tMatricola.setOnFocusChangeListener(new OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    tMatricola.post(new Runnable() {
                        @Override
                        public void run() {
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.showSoftInput(tMatricola,
                                    InputMethodManager.SHOW_IMPLICIT);
                        }
                    });
                }
            });
            tMatricola.requestFocus();
            messagesRefresh();
        } catch (Exception e) {
            new ToolTip(MainActivity.this,
                    "Impossibile stabilire una connessione con il server");
            return;
        }

    }

    private final BroadcastReceiver updateConnectionStatusUiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Si controlla se lo stato della rete internet e' cambiato
            if (NetworkChangeReceiver.ACTIVE) {
                tvConnectionStatus.setVisibility(View.GONE);
                // Se c'e' linea si controlla lo stato del menu e vari messaggi
                messagesRefresh();
            } else {
                tvConnectionStatus.setText("Nessuna connessione disponibile");
                tvConnectionStatus.setVisibility(View.VISIBLE);

            }

        }
    };

    @Override
    public void onDestroy() {
        unregisterReceiver(updateConnectionStatusUiReceiver);
        super.onDestroy();
    }

    //Non si chiude per lasciare attiva la notifica di menu disponibile
    //@Override
    //public void onBackPressed() {
    //finish();
    //}

    private void writeMessages(int retCode, String message) {
        // Si scrivono i messaggi colorati a seconda del retCode
        TextView tv = new TextView(this);
        tv.setId(1);
        if (retCode == -3) {
            tv.setTextSize(25);
            tv.setTextColor(Color.rgb(150, 150, 150));
        } else if (retCode == 0) {
            tv.setTextSize(20);
            tv.setTextColor(Color.BLUE);
        } else if (retCode == -1) {
            tv.setTextSize(20);
            tv.setTextColor(Color.RED);
        } else if (retCode == -2) {
            tv.setTextSize(20);
            tv.setTextColor(Color.parseColor("#DBA901"));
        } else {
            tv.setTextSize(15);
            tv.setTextColor(Color.BLACK);
        }

        tv.setGravity(Gravity.CENTER_HORIZONTAL);

        tv.setText(Html.fromHtml(message) + "\n");
        // Si accodano nel box dei messaggi
        messageContext.addView(tv);
    }
}
