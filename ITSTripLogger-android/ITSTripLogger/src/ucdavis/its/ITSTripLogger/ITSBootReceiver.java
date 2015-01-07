package ucdavis.its.ITSTripLogger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
 
/**
 * Author: Navid Ghahramani
 * You can contact to me with ghahramani.navid@gmail.com
 */
public class ITSBootReceiver extends BroadcastReceiver {
 
    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            Class aClass = Class.forName("ucdavis.its.ITSTripLogger.ITSService");
            Intent serviceIntent = new Intent(context, aClass);
            context.startService(serviceIntent);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
 
}