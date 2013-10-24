package it.manzolo.pastiarzach.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import it.manzolo.pastiarzach.ArzachUrls;
import it.manzolo.pastiarzach.Dipendente;
import it.manzolo.pastiarzach.Ordine;
import it.manzolo.pastiarzach.Parameters;
import it.manzolo.pastiarzach.PrezziPasti;
import it.manzolo.pastiarzach.R;
import it.manzolo.pastiarzach.service.CheckNotificationService;
import it.manzolo.utils.Internet;
import it.manzolo.utils.MessageBox;
import it.manzolo.utils.ToolTip;

public class LoginActivity extends Activity {
    static final String PRIMO = "primo";
    static final String SECONDO = "secondo";
    static final String CONTORNO = "contorno";
    static final String DOLCE = "dolce";
    PrezziPasti prezziPasti;

    List<NameValuePair> pietanze = new ArrayList<NameValuePair>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        Intent intent = getIntent();
        // Si prende la matricola che e' stata passata
        String matricola = intent.getStringExtra("matricola");
        if (isAutenticato(matricola)) {
            new Parameters(this).saveMatricola();
            // Nome e Direzione
            TextView welcome = (TextView) findViewById(R.id.welcome);
            welcome.setTextSize(20);
            welcome.setTextColor(Color.parseColor("#5500AA"));
            welcome.setText(Dipendente.NOMINATIVO + "\n" + Dipendente.DIREZIONE + " (" + Dipendente.LOCAZIONE + ")" + "\n");
        } else {
            Dipendente.cleanAll();
            //Se non si e' autorizzati si da l'avviso
            new ToolTip(this, "Matricola non autorizzata", true);
            finish();
            return;
        }
        //Si stoppa la notifica per oggi
        Log.i("ManzoloNessuna notifica", "Menu visionato dall'utente");
        CheckNotificationService.stopService();
        prezziPasti = new PrezziPasti();
        generateMenu();

    }

    private boolean isAutenticato(String matricola) {
        boolean state;
        JSONObject retval;

        try {
            retval = new Internet(ArzachUrls.LOGIN_PAGE + "" + matricola).getJSONObject();
        } catch (Exception e) {
            return false;
        }
        try {
            if (Integer.parseInt(retval.getString("retcode")) == 0) {
                Dipendente.AUTENTICATIO = true;
                Dipendente.MATRICOLA = matricola;
                Dipendente.ID = retval.getString("dipendente_id");
                Dipendente.DIREZIONE = retval.getString("direzione");
                Dipendente.LOCAZIONE = retval.getString("locazione");
                Dipendente.NOMINATIVO = retval.getString("nome");
                state = true;
            } else {
                state = false;
            }
        } catch (NumberFormatException e) {
            return false;
        } catch (JSONException e) {
            return false;
        }
        return state;

    }

    private void generateMenu() {
        LinearLayout rl = (LinearLayout) findViewById(R.id.scrollLinearLayout);
        rl.removeAllViews();
        pietanze.clear();
        try {
            Ordine ordine = new Ordine();

            // Costruzione menu
            boolean almenouno = false;
            try {
                JSONArray jsonArray = new Internet(ArzachUrls.MENU_DAY_PAGE).getJSONArray();
                for (int i = 0; i < jsonArray.length(); i++) {
                    almenouno = true;
                    //Si prede ogni pietanza
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    //SI creano le singole pietanze con il proprio checkbox (se il menu è aperto, altrimenti Textview)
                    View tv;
                    if (ordine.isAperto()) {
                        tv = new CheckBox(this);
                        ((CheckBox) tv).setTextSize(15);
                    } else {
                        tv = new TextView(this);
                        ((TextView) tv).setTextSize(15);
                    }

                    Integer pietanzaID = Integer.parseInt(jsonObject.getString("pasto_id"));
                    pietanze.add(new BasicNameValuePair("id", jsonObject.getString("pasto_id")));
                    tv.setId(pietanzaID);

                    Boolean primo = jsonObject.getString(PRIMO).equals("1");
                    Boolean secondo = jsonObject.getString(SECONDO).equals("1");
                    Boolean contorno = jsonObject.getString(CONTORNO).equals("1");
                    Boolean dolce = jsonObject.getString(DOLCE).equals("1");

                    //Si colorano diversamente i tipi di piatto
                    if (primo) {
                        ((TextView) tv).setTextColor(Color.BLUE);
                        ((TextView) tv).setTag(PRIMO);
                    } else if (secondo) {
                        ((TextView) tv).setTextColor(Color.rgb(0, 150, 0));
                        ((TextView) tv).setTag(SECONDO);
                    } else if (contorno) {
                        ((TextView) tv).setTextColor(Color.parseColor("#DD6F00"));
                        ((TextView) tv).setTag(CONTORNO);
                    } else if (dolce) {
                        ((TextView) tv).setTextColor(Color.parseColor("#663300"));
                        ((TextView) tv).setTag(DOLCE);
                    }
                    ((TextView) tv).setGravity(Gravity.LEFT);
                    ((TextView) tv).setText(jsonObject.getString("descrizione") + "\n");


                    if (tv instanceof CheckBox) {
                        ((TextView) tv).setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                controllaPrezzo();
                            }
                        });
                    }

                    rl.addView(tv);
                }

                //Si genera il tasto che permette l'ordine
                if (almenouno && ordine.isAperto()) {
                    Button btnOrdina = new Button(this);
                    btnOrdina.setText("Ordina");
                    btnOrdina.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ordina(v);
                        }
                    });

                    rl.addView(btnOrdina);
                }

            } catch (Exception e) {
                new ToolTip(this, "Si e' verificato un errore nel reperire il menu", true);
                finish();
                return;
            }

            getSaldo();

        } catch (Exception e) {
            //Se non si e' autorizzati si da l'avviso
            new ToolTip(this, "Impossibile contattare il server Arzach", true);
            finish();
            return;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.login, menu);
        return true;
    }

    public void ordina(View view) {
        // SI chiede conferma se si vuole ordinare
        new AlertDialog.Builder(this)
                .setTitle("Arzach")
                .setMessage("Sicuro di voler ordinare il pasto?")
                .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        /* Sul SI */
                        // Controllo stato ordine
                        try {
                            Ordine ordine = new Ordine();
                            if (!ordine.isAperto()) {
                                new MessageBox(LoginActivity.this, "Ordine", ordine.getMessaggio());
                                return;
                            }
                        } catch (Exception e) {
                            new ToolTip(LoginActivity.this, "Impossibile determinare lo stato dell'ordine");
                            return;

                        }
                        // Fine controllo stato ordine


                        // Si crea una connessione sulla pagina che permette l'ordine
                        HttpClient httpclient = new DefaultHttpClient();
                        HttpPost httppost = new HttpPost(ArzachUrls.MENU_ORDINA_PAGE);
                        // Si controlla che sia stato selezionato almeno un piatto
                        try {
                            //Si controlla quanti piatti sono stati selezionati
                            List<NameValuePair> selezionati = new ArrayList<NameValuePair>();

                            for (NameValuePair pietanza : pietanze) {
                                CheckBox elemento = (CheckBox) findViewById(Integer
                                        .parseInt(pietanza.getValue()));
                                if (elemento.isChecked()) {
                                    selezionati.add(new org.apache.http.message.BasicNameValuePair("options[]", pietanza.getValue()));
                                }
                            }
                            if (selezionati.size() <= 0) {
                                new MessageBox(LoginActivity.this,
                                        "Attenzione",
                                        "Seleziona almeno un piatto!");
                                return;
                            }

                            //Si prende la data del giorno
                            String giorno = new SimpleDateFormat("yyyy-MM-dd", Locale.ITALY).format(new Date());
                            //Log.i("ordina", giorno);
                            selezionati.add(new BasicNameValuePair("giorno", giorno));
                            selezionati.add(new BasicNameValuePair("dipendente_id", Dipendente.ID));
                            httppost.setEntity(new UrlEncodedFormEntity(selezionati));
                            //Si esegue la POST alla pagina che raccoglie gli ordini
                            httpclient.execute(httppost);

                            /*
                            //Si ferma la notifica per oggi
                            Calendar calendar = Calendar.getInstance();
                            SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy", Locale.ITALY);
                            String formattedDate = df.format(calendar.getTime());

                            DbNotificheAdapter dbNotificheAdapter = new DbNotificheAdapter(LoginActivity.this);
                            dbNotificheAdapter.open();
                            boolean alreadyMenuNotify = dbNotificheAdapter.NotificationByDateExists(formattedDate);
                            dbNotificheAdapter.close();

                            if (!alreadyMenuNotify) {
                                dbNotificheAdapter.open();
                                dbNotificheAdapter.createNotification(formattedDate);
                                dbNotificheAdapter.close();
                            }
                            */
                        } catch (ClientProtocolException e) {
                            new MessageBox(
                                    LoginActivity.this,
                                    "Attenzione",
                                    "Non e' stato possibile ordinare il pasto, controllare di essere connessi a internet");
                        } catch (IOException e) {
                            new MessageBox(LoginActivity.this, "Attenzione",
                                    "Non e' stato possibile ordinare il pasto, errore nell'invio, riprovare!");
                        }

                        //Si apre la maschera di riepilogo
                        Intent intent = new Intent(LoginActivity.this, DisplayRiepilogoActivity.class);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Sul NO non si fa niente
                    }
                }).show();

    }

    public void controllaPrezzo() {
        // SI chiede conferma se si vuole ordinare
        //Si controlla quanti piatti sono stati selezionati
        List<NameValuePair> selezionati = new ArrayList<NameValuePair>();
        int numprimo = 0;
        int numsecondo = 0;
        int numcontorno = 0;
        int numdolce = 0;

        for (NameValuePair pietanza : pietanze) {
            CheckBox elemento = (CheckBox) findViewById(Integer.parseInt(pietanza.getValue()));
            if (elemento.isChecked()) {
                if (elemento.getTag().toString().equals(PRIMO)) {
                    numprimo = numprimo + 1;
                }
                if (elemento.getTag().toString().equals(SECONDO)) {
                    numsecondo = numsecondo + 1;
                }
                if (elemento.getTag().toString().equals(CONTORNO)) {
                    numcontorno = numcontorno + 1;
                }
                if (elemento.getTag().toString().equals(DOLCE)) {
                    numdolce = numdolce + 1;
                }
            }
        }
        float ret = prezziPasti.calcolaPrezzo(numprimo, numsecondo, numcontorno, numdolce);
        new ToolTip(this, "\u20AC " + String.valueOf(ret));

    }

    @Override
    public void onResume() {
        super.onResume();
        //Si rimettono tutti i pasti come non selezionati
        try {
            refresh();
        } catch (Exception e) {
            new ToolTip(this, "Impossibile comunicare con il server");
        }

    }

    public void refresh() {
        try {
            final ProgressDialog dialog = ProgressDialog.show(LoginActivity.this, "", "Elaborazione dati..", true);
            dialog.show();
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    //Si ricalcola il saldo nel caso fosse cambiato
                    getSaldo();
                    //Per ottimizzare una chiamata web commentare il rigeneramenu
                    generateMenu();
                         /*
                         if (new Ordine(this).isAperto()){
							 for (NameValuePair pietanza : pietanze) {
									CheckBox elemento = (CheckBox) findViewById(Integer.parseInt(pietanza.getValue()));
									if (elemento.isChecked()) {
										elemento.setChecked(!elemento.isChecked());
									}
			 			     }
				         }
				         */
                    dialog.dismiss();
                }
            }, 3000);  // 3000 milliseconds
        } catch (Exception e) {
            new ToolTip(this, "Impossibile comunicare con il server", true);
        }

    }

    public void uscita(View view) {
        finish();
    }

    public void menu(View view) {

    }

    public void situazione(View view) {
        //Si apre la maschera di riepilogo
        Intent intent = new Intent(LoginActivity.this, DisplayRiepilogoActivity.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    private void getSaldo() {
        TextView saldo = (TextView) findViewById(R.id.saldo);

        try {
            //Si riporta il saldo attuale
            TextView lblsaldo = (TextView) findViewById(R.id.lblsaldo);
            lblsaldo.setTextSize(20);
            lblsaldo.setTextColor(Color.BLUE);
            lblsaldo.setText("Saldo attuale");

            float saldofinale = Dipendente.getSaldo();
            saldo.setTextSize(20);
            if (saldofinale < 0) {
                saldo.setTextColor(Color.RED);
            } else {
                saldo.setTextColor(Color.BLUE);
            }
            saldo.setText("\u20AC " + String.format("%.2f", saldofinale));
            //Fine saldo

        } catch (Exception e) {
            saldo.setTextColor(Color.DKGRAY);
            saldo.setText("Non disponibile");
        }

    }

}
