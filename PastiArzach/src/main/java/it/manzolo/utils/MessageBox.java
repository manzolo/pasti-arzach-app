package it.manzolo.utils;

import android.app.Activity;
import android.app.AlertDialog;

public class MessageBox {

    /**
     * @param activity, title, message
     */
    public MessageBox(Activity activity, String title, String message) {

        AlertDialog.Builder miaAlert = new AlertDialog.Builder(activity);
        miaAlert.setTitle(title);
        miaAlert.setMessage(message);
        AlertDialog alert = miaAlert.create();
        alert.setCanceledOnTouchOutside(true);
        alert.show();

		/*
         * new AlertDialog.Builder(this) .setTitle("Arzach")
		 * .setMessage(jsonObject.getString("messaggio"))
		 * .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		 * public void onClick(DialogInterface dialog, int which) { // Sul SI
		 * System.exit(0); } }) .setNegativeButton("No", new
		 * DialogInterface.OnClickListener() { public void
		 * onClick(DialogInterface dialog, int which) { // Sul NO } }) .show();
		 * System.exit(0);
		 */

    }


}

/*
 * new AlertDialog.Builder(this) .setTitle("Arzach")
 * .setMessage(jsonObject.getString("messaggio")) .setPositiveButton("Yes", new
 * DialogInterface.OnClickListener() { public void onClick(DialogInterface
 * dialog, int which) { // Sul SI System.exit(0); } }) .setNegativeButton("No",
 * new DialogInterface.OnClickListener() { public void onClick(DialogInterface
 * dialog, int which) { // Sul NO } }) .show(); System.exit(0);
 */

