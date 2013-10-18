package it.manzolo.pastiarzach.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
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
import it.manzolo.pastiarzach.R;
import it.manzolo.utils.Internet;
import it.manzolo.utils.MessageBox;
import it.manzolo.utils.ToolTip;

public class DisplayRiepilogoActivity extends Activity {

    List<NameValuePair> pietanze = new ArrayList<NameValuePair>();
    static final int BTN_ELIMINA_ID = 99999999;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_display_riepilogo);

        getSaldo();
        getMenuOrdinato();

    }

    public void getMenuOrdinato() {
        String giorno = new SimpleDateFormat("yyyy-MM-dd", Locale.ITALY).format(new Date());
        String url = ArzachUrls.RIEPILOGO_ORDINE_PAGE.concat("?").concat("dipendente_id=").concat(Dipendente.ID).concat("&giorno=").concat(giorno);
        LinearLayout rl = (LinearLayout) findViewById(R.id.scrollLinearLayout);
        rl.removeAllViews();
        //Si legge lo stato dell'ordine
        Ordine ordine = new Ordine();

        // Costruzione menu
        try {
            String riepilogoDay = new Internet(url).getResponse();
            JSONArray jsonArray = new JSONArray(riepilogoDay);
            for (int i = 0; i < jsonArray.length(); i++) {
                // Si prede ogni pietanza
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                // SI creano le singole pietanze con il proprio checkbox
                View tv;
                if (ordine.isAperto()) {
                    tv = new CheckBox(this);
                    ((CheckBox) tv).setTextSize(15);
                } else {
                    tv = new TextView(this);
                    ((TextView) tv).setTextSize(15);

                }

                Integer pietanzaID = Integer.parseInt(jsonObject.getString("menu_id"));
                pietanze.add(new BasicNameValuePair("id", jsonObject.getString("menu_id")));
                tv.setId(pietanzaID);

                Boolean primo = jsonObject.getString("primo").equals("1");
                Boolean secondo = jsonObject.getString("secondo").equals("1");
                Boolean contorno = jsonObject.getString("contorno").equals("1");
                Boolean dolce = jsonObject.getString("dolce").equals("1");

                // Si colorano diversamente i tipi di piatto
                if (primo) {
                    ((TextView) tv).setTextColor(Color.BLUE);
                } else if (secondo) {
                    ((TextView) tv).setTextColor(Color.rgb(0, 150, 0));
                } else if (contorno) {
                    ((TextView) tv).setTextColor(Color.parseColor("#DD6F00"));
                } else if (dolce) {
                    ((TextView) tv).setTextColor(Color.parseColor("#663300"));
                }
                ((TextView) tv).setGravity(Gravity.LEFT);
                ((TextView) tv).setText(jsonObject.getString("descrizione").concat("\n"));
                rl.addView(tv);
            }
            if (ordine.isAperto()) {
                //Si genera il tasto che permette l'ordine
                Button btnElimina = new Button(this);
                btnElimina.setId(BTN_ELIMINA_ID);
                btnElimina.setText("Annulla ordini selezionati");
                btnElimina.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        elimina(v);
                    }
                });

                rl.addView(btnElimina);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getSaldo() {
        TextView saldo = (TextView) findViewById(R.id.saldo);
        try {
            // Si prende il saldo attuale
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
            // Fine saldo
        } catch (Exception e) {
            saldo.setTextColor(Color.DKGRAY);
            saldo.setText("Non disponibile");
        }
    }

    public void elimina(View view) {
        // SI chiede conferma se si vuole ordinare
        new AlertDialog.Builder(this)
                .setTitle("Arzach")
                .setMessage("Sicuro di voler eliminare i pasti selezionati?")
                .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        /* Sul SI */

                        // Controllo stato ordine
                        try {
                            Ordine ordine = new Ordine();
                            if (!ordine.isDisponibile() || !ordine.isAperto()) {
                                new MessageBox(DisplayRiepilogoActivity.this, "Ordine", ordine.getMessaggio());
                                return;
                            }
                        } catch (Exception e) {
                            new MessageBox(DisplayRiepilogoActivity.this, "Ordine", e.getMessage());
                            return;
                        }
                        // Fine controllo stato ordine

                        // Si crea una connessione sulla pagina che permette la
                        // cancellazione dell'ordine
                        HttpClient httpclient = new DefaultHttpClient();
                        HttpPost httppost = new HttpPost(ArzachUrls.MENU_ELIMINA_PAGE);
                        // Si controlla quanti piatti sono stati selezionati
                        List<NameValuePair> selezionati = new ArrayList<NameValuePair>();

                        // Si controlla che sia stato selezionato almeno un piatto
                        try {

                            for (NameValuePair pietanza : pietanze) {
                                CheckBox elemento = (CheckBox) findViewById(Integer.parseInt(pietanza.getValue()));
                                if (elemento.isChecked()) {
                                    selezionati
                                            .add(new org.apache.http.message.BasicNameValuePair(
                                                    "options[]", pietanza
                                                    .getValue()));
                                }
                            }
                            if (selezionati.size() <= 0) {
                                new MessageBox(DisplayRiepilogoActivity.this,
                                        "Attenzione",
                                        "Seleziona almeno un piatto!");
                                return;
                            }

                            selezionati.add(new BasicNameValuePair(
                                    "dipendente_id", Dipendente.ID));
                            httppost.setEntity(new UrlEncodedFormEntity(
                                    selezionati));
                            // Si esegue la POST alla pagina che raccoglie gli
                            // ordini
                            httpclient.execute(httppost);

                            LinearLayout rl = (LinearLayout) findViewById(R.id.scrollLinearLayout);
                            for (NameValuePair pietanza : selezionati) {
                                CheckBox elemento = (CheckBox) findViewById(Integer.parseInt(pietanza.getValue()));
                                pietanze.remove(new BasicNameValuePair("id", pietanza.getValue()));
                                rl.removeView(elemento);
                            }

                            getSaldo();

                        } catch (ClientProtocolException e) {
                            new MessageBox(
                                    DisplayRiepilogoActivity.this,
                                    "Attenzione",
                                    "Non e' stato possibile cancellare l'ordine, controllare di essere connessi a internet");
                        } catch (IOException e) {
                            new MessageBox(DisplayRiepilogoActivity.this,
                                    "Attenzione",
                                    "Non e' stato possibile cancellare l'ordine, errore nell'invio dei dati");
                        }
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Sul NO non si fa niente
                    }
                }).show();

    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            refresh();
        } catch (Exception e) {
            new ToolTip(this, "Impossibile comunicare con il server Arzach", true);
        }
    }


    public void refresh() {
        try {
            final ProgressDialog dialog = ProgressDialog.show(DisplayRiepilogoActivity.this, "", "Elaborazione dati..", true);
            dialog.show();
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    getMenuOrdinato();
                    getSaldo();
                    dialog.dismiss();
                }
            }, 3000);  // 3000 milliseconds
        } catch (Exception e) {
            new ToolTip(this, "Impossibile comunicare con il server", true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.display_riepilogo, menu);
        return true;
    }

    public void uscita(View view) {
        finish();
    }

    public void situazione(View view) {

    }

    public void menu(View view) {
        finish();
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
