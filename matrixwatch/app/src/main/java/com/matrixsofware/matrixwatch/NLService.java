package com.matrixsofware.matrixwatch;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class NLService extends NotificationListenerService {

    private String TAG = "lol";
    private NLServiceReceiver nlservicereciver;
    @Override
    public void onCreate() {
        super.onCreate();
        nlservicereciver = new NLServiceReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.matrixsofware.matrixwatch.NOTIFICATION_SERVICE_LISTENER");
        registerReceiver(nlservicereciver,filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(nlservicereciver);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        ApplicationInfo ai;
        try {
            ai = this.getPackageManager().getApplicationInfo(sbn.getPackageName(), 0);
        }
        catch (final PackageManager.NameNotFoundException e) {
            ai = null;
        }
        final String applicationName = (String) (ai != null ? this.getPackageManager().getApplicationLabel(ai) : "(unknown)");
        Log.i("lol","ID :" + applicationName + "\t" + sbn.getNotification().tickerText + "\t" + sbn.getPackageName() + sbn.toString());
        Intent i = new  Intent("com.matrixsofware.matrixwatch.NOTIFICATION_LISTENER");
        JSONObject j = new JSONObject();
        try {
            j.put("name", "notify");
            j.put("applicationName", applicationName);
            j.put("text", sbn.getNotification().tickerText);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        i.putExtra("notification_event", j.toString());
        sendBroadcast(i);

    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Log.i(TAG,"********** onNOtificationRemoved");
        Log.i(TAG,"ID :" + sbn.getId() + "\t" + sbn.getNotification().tickerText +"\t" + sbn.getPackageName());
        Intent i = new  Intent("com.matrixsofware.matrixwatch.NOTIFICATION_LISTENER");
        i.putExtra("notification_event","onNotificationRemoved :" + sbn.getPackageName() + "\n");

        sendBroadcast(i);
    }

    class NLServiceReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getStringExtra("command").equals("clearall")){
                NLService.this.cancelAllNotifications();
            }
            else if(intent.getStringExtra("command").equals("list")){
                Intent i1 = new  Intent("com.matrixsofware.matrixwatch.NOTIFICATION_LISTENER");
                i1.putExtra("notification_event","=====================");
                sendBroadcast(i1);
                int i=1;
                for (StatusBarNotification sbn : NLService.this.getActiveNotifications()) {
                    Intent i2 = new  Intent("com.matrixsofware.matrixwatch.NOTIFICATION_LISTENER");
                    i2.putExtra("notification_event",i +" " + sbn.getPackageName() + "\n");
                    sendBroadcast(i2);
                    i++;
                }
                Intent i3 = new  Intent("com.matrixsofware.matrixwatch.NOTIFICATION_LISTENER");
                i3.putExtra("notification_event","===== Notification List ====");
                sendBroadcast(i3);

            }

        }
    }

}
