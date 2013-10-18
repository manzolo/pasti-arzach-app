package it.manzolo.pastiarzach;

import org.json.JSONArray;
import org.json.JSONObject;

import it.manzolo.utils.Internet;

public class Ordine {
    private String stato;
    private String messaggio;
    private JSONArray listaPiatti;

    public Ordine() {
        try {
            JSONObject jsonObject = new Internet(ArzachUrls.CHECK_MENU_STATUS_PAGE).getJSONObject();
            setStato(jsonObject.getString("retcode"));
            setMessaggio(jsonObject.getString("messaggio"));
            setListaPiatti(new Internet(ArzachUrls.MENU_DAY_PAGE).getJSONArray());

        } catch (Exception e) {
            new Exception("Impossibile accedere allo stato dell'ordine");
        }

    }


    public boolean isAperto() {
        return (getStato().equals("0") ? true : false);
    }

    public boolean isChiuso() {
        return (getStato().equals("1") ? true : false);
    }

    public boolean isSospeso() {
        return (getStato().equals("2") ? true : false);
    }

    public boolean isDisponibile() {
        return (Integer.parseInt(getStato()) >= 0 ? true : false);
    }


    public String getMessaggio() {
        return messaggio;
    }


    public void setMessaggio(String messaggio) {
        this.messaggio = messaggio;
    }


    public String getStato() {
        return stato;
    }


    public void setStato(String stato) {
        this.stato = stato;
    }


    public JSONArray getListaPiatti() {
        return listaPiatti;
    }


    public void setListaPiatti(JSONArray listaPiatti) {
        this.listaPiatti = listaPiatti;
    }


}
