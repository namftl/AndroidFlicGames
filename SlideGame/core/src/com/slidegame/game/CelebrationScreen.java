package com.slidegame.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class CelebrationScreen implements Screen {
    private final SlideGame game;
    private OrthographicCamera camera;
    private Viewport viewport;
    private float timer;
    private static final float DISPLAY_DURATION = 3.0f;

    public CelebrationScreen(final SlideGame game) {
        this.game = game;
        camera = new OrthographicCamera();
        camera.setToOrtho(false, SlideGame.VIRTUAL_WIDTH, SlideGame.VIRTUAL_HEIGHT);
        viewport = new FitViewport(SlideGame.VIRTUAL_WIDTH, SlideGame.VIRTUAL_HEIGHT, camera);
        timer = 0;
    }

    @Override
    public void render(float delta) {
        timer += delta;

        if (timer >= DISPLAY_DURATION) {
            game.setScreen(new MainMenuScreen(game));
            return;
        }

        Gdx.gl.glClearColor(0.0f, 0.5f, 0.0f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        game.batch.setProjectionMatrix(camera.combined);

        game.batch.begin();
        game.font.getData().setScale(3.0f);
        String message = "SUCCESS!";
        float messageWidth = game.font.getRegion().getRegionWidth() * message.length() * 1.2f;
        game.font.draw(game.batch, message,
                SlideGame.VIRTUAL_WIDTH / 2f - messageWidth / 2f,
                SlideGame.VIRTUAL_HEIGHT / 2f + 50);

        game.font.getData().setScale(1.5f);
        String subtitle = "All bricks hit in order!";
        float subtitleWidth = game.font.getRegion().getRegionWidth() * subtitle.length() * 0.6f;
        game.font.draw(game.batch, subtitle,
                SlideGame.VIRTUAL_WIDTH / 2f - subtitleWidth / 2f,
                SlideGame.VIRTUAL_HEIGHT / 2f - 30);

        game.font.getData().setScale(1.0f);
        game.batch.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void show() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void dispose() {
    }
}
