package it.manzolo.utils;

import android.content.Context;
import android.widget.Toast;

public class ToolTip {
    //private String msg;
    //private Activity curentActivity;
    //int defaultMessageLength = Toast.LENGTH_SHORT;

    public ToolTip(Context context, String message) {
        //this.setMsg(message);
        //this.setCurentActivity(activity);
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public ToolTip(Context context, String message, boolean longMessage) {
        //this.setMsg(message);
        //this.setCurentActivity(activity);
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

	/*
    public void longMessage() {
		defaultMessageLength = Toast.LENGTH_LONG;
		new ToolTip(getCurentActivity(),getMsg()).show();
	}
	public void show() {
	}

	private String getMsg() {
		return msg;
	}

	private void setMsg(String msg) {
		this.msg = msg;
	}


	private Activity getCurentActivity() {
		return curentActivity;
	}


	private void setCurentActivity(Activity curentActivity) {
		this.curentActivity = curentActivity;
	}
	 */

}
