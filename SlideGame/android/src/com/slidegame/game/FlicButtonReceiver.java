package com.slidegame.game;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class FlicButtonReceiver extends BroadcastReceiver {
    private static final String TAG = "SlideGame";
    private static ButtonPressedListener listener;

    public interface ButtonPressedListener {
        void onFlicButtonPressed();
    }

    public static void setListener(ButtonPressedListener l) {
        listener = l;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "========================================");
        Log.d(TAG, "Broadcast received! Action: " + action);

        if (intent.getExtras() != null) {
            for (String key : intent.getExtras().keySet()) {
                Log.d(TAG, "Extra: " + key + " = " + intent.getExtras().get(key));
            }
        }

        if (action != null) {
            Log.d(TAG, "Flic button pressed! Triggering explosion...");
            if (listener != null) {
                listener.onFlicButtonPressed();
            } else {
                Log.w(TAG, "No listener registered!");
            }
        }
        Log.d(TAG, "========================================");
    }
}
