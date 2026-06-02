package com.slidegame.game;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

public class DesktopLauncher {
    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("SlideGame - Desktop Test");
        config.setWindowedMode(800, 480);
        config.setResizable(false);
        config.setBackBufferConfig(8, 8, 8, 8, 16, 8, 0);
        new Lwjgl3Application(new SlideGame(), config);
    }
}
