package it.manzolo.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import it.manzolo.pastiarzach.service.ScheduleNotificationService;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent scheduleService = new Intent(context, ScheduleNotificationService.class);
        context.startService(scheduleService);
    }
} 