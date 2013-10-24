package it.manzolo.pastiarzach;

import org.json.JSONObject;

import it.manzolo.pastiarzach.parameters.ArzachUrls;
import it.manzolo.pastiarzach.service.NetworkChangeReceiver;
import it.manzolo.utils.Internet;

public class Dipendente {
    public static String MATRICOLA;
    public static String ID;
    public static String DIREZIONE;
    public static String LOCAZIONE;
    public static String NOMINATIVO;
    public static boolean AUTENTICATIO;

    public static void cleanAll() {
        AUTENTICATIO = false;
        MATRICOLA = "";
        ID = "";
        DIREZIONE = "";
        LOCAZIONE = "";
        NOMINATIVO = "";
    }

    public static float getSaldo() throws Exception {
        if (NetworkChangeReceiver.ACTIVE) {
            try {
                // Si prende il saldo attuale
                JSONObject retval = new Internet(ArzachUrls.LOGIN_PAGE
                        + Dipendente.MATRICOLA).getJSONObject();
                // JSONObject retval = new
                // Internet(ArzachUrls.TEST_SLEEPING).getJSONObject();
                if (Integer.parseInt(retval.getString("retcode")) == 0) {
                    return Float.parseFloat(retval.getString("saldo"));
                } else {
                    throw new Exception("Risposta errata dal server");
                }
            } catch (Exception e) {
                throw new Exception(
                        "Chiamata al servizio web di saldo non riuscita");
            }
        } else {
            throw new Exception("Nessuna connessione a internet disponibile");
        }
    }

}
