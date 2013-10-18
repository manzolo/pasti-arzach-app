package it.manzolo.utils;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import it.manzolo.pastiarzach.Dipendente;

public class Parameters {
    private Context context;
    static private String FILENAME = ".arzachparms";

    public Parameters(Context ctx) {
        context = ctx;
    }

    public void saveMatricola() {
        File file = new File(context.getCacheDir(), FILENAME);
        OutputStream outputStream;

        try {
            outputStream = new FileOutputStream(file);
            outputStream.write(Dipendente.MATRICOLA.getBytes());
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            Log.e("Salvataggio matricola",
                    "Impossibile salvare la matricola nel file"
                            + e.getMessage());
        }

    }

    public String loadMatricola() {

        try {
            File file = new File(context.getCacheDir(), FILENAME);
            InputStream inputStream = null;
            BufferedReader br = null;

            if (file.exists()) {
                // read this file into InputStream
                inputStream = new FileInputStream(file);

                br = new BufferedReader(new InputStreamReader(inputStream));

                StringBuilder sb = new StringBuilder();

                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                br.close();
                inputStream.close();
                return sb.toString();
            }
            return "";

        } catch (IOException e) {
            Log.e("Lettura matricola",
                    "Impossibile leggere la matricola nel file"
                            + e.getMessage());
        }
        return "";
    }
}
