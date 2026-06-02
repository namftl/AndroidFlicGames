package com.slidegame.game;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

public class AndroidLauncher extends AndroidApplication implements FlicButtonReceiver.ButtonPressedListener {
    private static final String TAG = "SlideGame";
    private SlideGame game;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        config.useAccelerometer = false;
        config.useCompass = false;
        config.useWakelock = true;
        config.r = 8;
        config.g = 8;
        config.b = 8;
        config.a = 8;
        config.depth = 16;
        config.stencil = 8;

        game = new SlideGame();
        initialize(game, config);

        FlicButtonReceiver.setListener(this);
        Log.d(TAG, "Flic broadcast receiver initialized. Configure your Flic button to send broadcasts!");

        checkIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG, "onNewIntent called");
        checkIntent(intent);
    }

    private void checkIntent(Intent intent) {
        if (intent != null) {
            String action = intent.getAction();
            Log.d(TAG, "Activity intent received. Action: " + action);

            if (action != null && action.equals("com.slidegame.game.FLIC_BUTTON_PRESSED")) {
                Log.d(TAG, "Flic intent detected via Activity!");
                onFlicButtonPressed();
            }

            if (intent.hasExtra("flic_button")) {
                Log.d(TAG, "Flic extra detected!");
                onFlicButtonPressed();
            }
        }
    }

    @Override
    public void onFlicButtonPressed() {
        Log.d(TAG, "Flic button broadcast received!");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (game != null) {
                    game.triggerButtonPress();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FlicButtonReceiver.setListener(null);
        game = null;
    }
}
