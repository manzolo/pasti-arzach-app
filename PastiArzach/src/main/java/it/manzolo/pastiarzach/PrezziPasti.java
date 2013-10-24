package it.manzolo.pastiarzach;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import it.manzolo.pastiarzach.parameters.ArzachUrls;
import it.manzolo.utils.Internet;

class MappaPrezzoPasti {
    int primo;
    int secondo;
    int contorno;
    int dolce;
    float prezzo;
}

class QuantitaPasti {
    int primo;
    int secondo;
    int contorno;
    int dolce;
}

public class PrezziPasti {
    private JSONArray jsonArray;
    private List<MappaPrezzoPasti> mappaprezzopasti = new ArrayList<MappaPrezzoPasti>();

    public PrezziPasti() throws Exception {
        try {
            jsonArray = new Internet(ArzachUrls.PREZZI_PASTO_PAGE)
                    .getJSONArray();
            for (int i = 0; i < jsonArray.length(); i++) {
                // Si prede ogni pietanza
                try {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    MappaPrezzoPasti mappa = new MappaPrezzoPasti();
                    mappa.primo = Integer.parseInt(jsonObject.get("primo").toString());
                    mappa.secondo = Integer.parseInt(jsonObject.get("secondo").toString());
                    mappa.contorno = Integer.parseInt(jsonObject.get("contorno").toString());
                    mappa.dolce = Integer.parseInt(jsonObject.get("dolce").toString());
                    mappa.prezzo = Float.parseFloat(jsonObject.get("prezzo").toString());
                    mappaprezzopasti.add(mappa);
                } catch (JSONException e) {
                    throw new Exception("Impossibile decodificare il listino prezzi");
                }
            }
        } catch (Exception e) {
            throw new Exception("Impossibile accedere al listino prezzi");
        }
    }

    public float calcolaPrezzo(int primo, int secondo, int contorno, int dolce) {
        int curprimo;
        int cursecondo;
        int curcontorno;
        int curdolce;
        int numloop = Math.max(Math.max(primo, secondo),
                Math.max(contorno, dolce));
        float prezzo = 0;

        for (int index = 0; index < numloop; index++) {

            if (primo > 0) {
                primo = primo - 1;
                curprimo = 1;
            } else {
                curprimo = 0;
            }
            if (secondo > 0) {
                secondo = secondo - 1;
                cursecondo = 1;
            } else {
                cursecondo = 0;
            }
            if (contorno > 0) {
                contorno = contorno - 1;
                curcontorno = 1;
            } else {
                curcontorno = 0;
            }
            if (dolce > 0) {
                dolce = dolce - 1;
                curdolce = 1;
            } else {
                curdolce = 0;
            }

            // MappaPrezzoPasti prezzodapagare =
            for (int i = 0, size = mappaprezzopasti.size(); i < size; i++) {
                MappaPrezzoPasti element = mappaprezzopasti.get(i);
                if (element.primo == curprimo && element.secondo == cursecondo
                        && element.contorno == curcontorno
                        && element.dolce == curdolce) {
                    prezzo = prezzo + element.prezzo;
                    break;
                }
            }

			/*
             * $prezzopasti = Doctrine_Query::create() ->select('p.prezzo')
			 * ->from('prezziPasti p') ->where('p.primo = ?', $primo)
			 * ->andWhere('p.secondo = ?', $secondo) ->andWhere('p.contorno =
			 * ?', $contorno) ->andWhere('p.dolce = ?', $dolce) ->andWhere("'" .
			 * $this->giornoscelto->getDataOrdine() .
			 * "' between p.dal and IFNULL(p.al,'9999-12-31')") ->fetchOne() ;
			 */

			/*
             * $prezzopasticonsumati = $prezzopasticonsumati +
			 * $prezzopasti->getPrezzo();
			 */

        }
        return prezzo;
    }

}
