package it.manzolo.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.util.Pair;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class Internet {
    private String url;
    private int timeoutConnection;
    private int timeoutSocket;
    private static final int DEFAULT_TIMEOUT_CONNECTION = 5000;
    private static final int DEFAULT_TIMEOUT_SOCKET = 6000;

    /**
     * @param url URL della richiesta da inviare su internet
     *
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
        StringBuilder chaine = new StringBuilder("");
        try{
            URL url = new URL(getUrl());
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestProperty("User-Agent", "");
            connection.setRequestMethod("POST");
            connection.setDoInput(true);
            connection.connect();

            InputStream inputStream = connection.getInputStream();

            BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = rd.readLine()) != null) {
                chaine.append(line);
            }

        } catch (IOException e) {
            Log.e(this.toString(), "La pagina " + getUrl() + " ritorna un errore: " + e.toString());
            throw new Exception("La pagina " + getUrl() + " ritorna un errore: " + e.toString());
        }

        return chaine.toString();

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

    public String performPostCall(List<Pair<String, String>> postDataParams) throws Exception {

        URL url;
        String response = "";
        try {
            url = new URL(getUrl());

            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
            httpConn.setUseCaches(false);

            httpConn.setDoInput(true); // true indicates the server returns response

            StringBuilder requestParams = new StringBuilder();
            if (postDataParams != null && postDataParams.size() > 0) {

                httpConn.setDoOutput(true); // true indicates POST request

                // creates the params string, encode them using URLEncoder
                Iterator<Pair<String, String>> paramIterator = postDataParams.iterator();
                boolean first = true;
                while (paramIterator.hasNext()) {
                    Pair<String, String> obj = paramIterator.next();
                    String key = obj.first;
                    String value = obj.second;
                    if (first)
                        first = false;
                    else
                        requestParams.append("&");

                    requestParams.append(key).append("=").append(value);
                }

                OutputStream os = httpConn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(requestParams.toString());

                writer.flush();
                writer.close();
                os.close();
                int responseCode=httpConn.getResponseCode();

                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    String line;
                    BufferedReader br=new BufferedReader(new InputStreamReader(httpConn.getInputStream()));
                    while ((line=br.readLine()) != null) {
                        response+=line;
                    }
                }
                else {
                    throw new Exception("La pagina " + getUrl() + " ritorna un errore: " + String.valueOf(responseCode));
                }
            }
        } catch (Exception e) {
            Log.e(this.toString(), "La pagina " + getUrl() + " ritorna un errore: " + e.toString());
            throw new Exception("La pagina " + getUrl() + " ritorna un errore: " + e.toString());
        }
        return response;
    }



}
