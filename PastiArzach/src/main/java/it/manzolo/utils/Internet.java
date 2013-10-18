package it.manzolo.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Internet {
    private String url;
    private int timeoutConnection;
    private int timeoutSocket;
    private static final int DEFAULT_TIMEOUT_CONNECTION = 5000;
    private static final int DEFAULT_TIMEOUT_SOCKET = 6000;

    /**
     * @param url
     */
    public Internet(String url) {
        setUrl(url);
        setTimeoutConnection(DEFAULT_TIMEOUT_CONNECTION);
        setTimeoutSocket(DEFAULT_TIMEOUT_SOCKET);


    }

    public Internet(String url, int timeoutConnection) {
        setUrl(url);
        setTimeoutConnection(timeoutConnection);
        setTimeoutSocket(DEFAULT_TIMEOUT_SOCKET);
    }

    public Internet(String url, int timeoutConnection, int timeoutSocket) {
        setUrl(url);
        setTimeoutConnection(timeoutConnection);
        setTimeoutSocket(timeoutSocket);
    }

    public JSONObject getJSONObject() throws Exception {
        try {
            return new JSONObject(getResponse());
        } catch (JSONException e) {
            Log.e(this.toString(), "La pagina " + getUrl() + " ritorna statusCode:" + e.getMessage());
            throw new Exception("La risposta del server non e' nel formato previsto:" + e.getMessage());
        }
    }

    public JSONArray getJSONArray() throws Exception {
        try {
            return new JSONArray(getResponse());
        } catch (JSONException e) {
            Log.e(this.toString(), "La pagina " + getUrl() + " ritorna statusCode:" + e.getMessage());
            throw new Exception("La risposta del server non e' nel formato previsto:" + e.getMessage());
        }
    }

    public String getResponse() throws Exception {
        StringBuilder builder = new StringBuilder();
        HttpGet httpGet = new HttpGet(getUrl());

        HttpParams httpParameters = new BasicHttpParams();
        // Set the timeout in milliseconds until a connection is established.
        // The default value is zero, that means the timeout is not used.
        int timeoutConnection = getTimeoutConnection();
        HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
        // Set the default socket timeout (SO_TIMEOUT)
        // in milliseconds which is the timeout for waiting for data.
        int timeoutSocket = getTimeoutSocket();
        HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
        DefaultHttpClient client = new DefaultHttpClient(httpParameters);

        try {
            HttpResponse response = client.execute(httpGet);
            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            switch (statusCode) {
                case 200:
                    HttpEntity entity = response.getEntity();
                    InputStream content = entity.getContent();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(content));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        builder.append(line);
                    }
                    break;
                case 404:
                    Log.e(this.toString(), "La pagina " + getUrl() + " non e' stata trovata");
                    throw new Exception("Pagina non trovata");
                case 401:
                    Log.e(this.toString(), "La pagina " + getUrl() + " richiede l'autenticazione");
                    throw new Exception("La pagina richiede l'autenticazione");
                case 500:
                    Log.e(this.toString(), "Il server alla pagina " + getUrl() + " e' andato in errore");
                    throw new Exception("La pagina richiesta al momento non funziona");
                default:
                    Log.e(this.toString(), "Il server alla pagina " + getUrl() + " ha restituito statusCode:" + statusCode);
                    throw new Exception("La pagina richiesta al momento non e' raggiungibile");
            }
        } catch (ClientProtocolException e) {
            Log.e(this.toString(), "La pagina " + getUrl() + " ritorna un errore nel protocollo:" + e.toString());
            throw new Exception("La pagina " + getUrl() + " ritorna un errore nel protocollo:" + e.toString());
        } catch (IOException e) {
            Log.e(this.toString(), "La pagina " + getUrl() + " ritorna un errore di Input/Output:" + e.toString());
            throw new Exception("La pagina " + getUrl() + " ritorna un errore di Input/Output:" + e.toString());
        }
        return builder.toString();
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getTimeoutSocket() {
        return timeoutSocket;
    }

    public void setTimeoutSocket(int timeoutSocket) {
        this.timeoutSocket = timeoutSocket;
    }

    public int getTimeoutConnection() {
        return timeoutConnection;
    }

    public void setTimeoutConnection(int timeoutConnection) {
        this.timeoutConnection = timeoutConnection;
    }

}
