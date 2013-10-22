package it.manzolo.utils;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by d59495 on 22/10/13.
 */
public class DateFunctions {
    static SimpleDateFormat inputParser = new SimpleDateFormat("HH:mm", Locale.ITALY);

    public static java.util.Date parseDate(String date) {

        try {
            return inputParser.parse(date);
        } catch (java.text.ParseException e) {
            return new java.util.Date(0);
        }
    }
}
