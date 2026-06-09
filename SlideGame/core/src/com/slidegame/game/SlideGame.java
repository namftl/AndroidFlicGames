package com.slidegame.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class SlideGame extends Game {
    public SpriteBatch batch;
    public ShapeRenderer shapeRenderer;
    public BitmapFont font;
    public Texture backgroundTexture;
    public Texture ballTexture;

    public static final int VIRTUAL_WIDTH = 800;
    public static final int VIRTUAL_HEIGHT = 480;

    private ButtonListener buttonListener;

    @Override
    public void create() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        font = new BitmapFont();
        font.getData().setScale(2f);
        font.setColor(1, 1, 1, 1);

        backgroundTexture = new Texture("background.png");
        ballTexture = new Texture("img.png");

        setScreen(new MainMenuScreen(this));
    }

    @Override
    public void render() {
        super.render();
    }

    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        font.dispose();
        backgroundTexture.dispose();
        ballTexture.dispose();
    }

    public void setButtonListener(ButtonListener listener) {
        this.buttonListener = listener;
    }

    public void triggerButtonPress() {
        if (buttonListener != null) {
            buttonListener.onButtonPressed();
        }
    }
}
