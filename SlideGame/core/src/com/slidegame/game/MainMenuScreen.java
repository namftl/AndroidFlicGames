package com.slidegame.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class MainMenuScreen implements Screen {
    private final SlideGame game;
    private OrthographicCamera camera;
    private Viewport viewport;

    private float normalModeButtonX;
    private float normalModeButtonY;
    private float normalModeButtonWidth;
    private float normalModeButtonHeight;

    private float flicModeButtonX;
    private float flicModeButtonY;
    private float flicModeButtonWidth;
    private float flicModeButtonHeight;

    private static final float BUTTON_WIDTH = 300f;
    private static final float BUTTON_HEIGHT = 100f;
    private static final float BUTTON_SPACING = 40f;

    public MainMenuScreen(final SlideGame game) {
        this.game = game;

        camera = new OrthographicCamera();
        camera.setToOrtho(false, SlideGame.VIRTUAL_WIDTH, SlideGame.VIRTUAL_HEIGHT);
        viewport = new FitViewport(SlideGame.VIRTUAL_WIDTH, SlideGame.VIRTUAL_HEIGHT, camera);

        float centerX = SlideGame.VIRTUAL_WIDTH / 2f;
        float centerY = SlideGame.VIRTUAL_HEIGHT / 2f;

        normalModeButtonWidth = BUTTON_WIDTH;
        normalModeButtonHeight = BUTTON_HEIGHT;
        normalModeButtonX = centerX - BUTTON_WIDTH / 2f;
        normalModeButtonY = centerY + BUTTON_SPACING / 2f;

        flicModeButtonWidth = BUTTON_WIDTH;
        flicModeButtonHeight = BUTTON_HEIGHT;
        flicModeButtonX = centerX - BUTTON_WIDTH / 2f;
        flicModeButtonY = centerY - BUTTON_HEIGHT - BUTTON_SPACING / 2f;
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.15f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        game.batch.setProjectionMatrix(camera.combined);
        game.shapeRenderer.setProjectionMatrix(camera.combined);

        handleInput();

        renderButtons();

        game.batch.begin();
        game.font.getData().setScale(2.0f);
        String title = "Brick Hit Game";
        float titleWidth = game.font.getRegion().getRegionWidth() * title.length() * 0.8f;
        game.font.draw(game.batch, title,
                SlideGame.VIRTUAL_WIDTH / 2f - titleWidth / 2f,
                SlideGame.VIRTUAL_HEIGHT - 80);

        game.font.getData().setScale(1.0f);
        game.font.draw(game.batch, "Normal Mode",
                normalModeButtonX + 75,
                normalModeButtonY + normalModeButtonHeight / 2f + 10);

        game.font.draw(game.batch, "Flic Mode",
                flicModeButtonX + 90,
                flicModeButtonY + flicModeButtonHeight / 2f + 10);

        game.batch.end();
    }

    private void renderButtons() {
        game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        game.shapeRenderer.setColor(0.2f, 0.6f, 1.0f, 0.8f);
        game.shapeRenderer.rect(normalModeButtonX, normalModeButtonY, normalModeButtonWidth, normalModeButtonHeight);

        game.shapeRenderer.setColor(1.0f, 0.5f, 0.0f, 0.8f);
        game.shapeRenderer.rect(flicModeButtonX, flicModeButtonY, flicModeButtonWidth, flicModeButtonHeight);

        game.shapeRenderer.setColor(1, 1, 1, 0.2f);
        game.shapeRenderer.rect(normalModeButtonX + 5, normalModeButtonY + 5,
                normalModeButtonWidth - 10, normalModeButtonHeight - 10);
        game.shapeRenderer.rect(flicModeButtonX + 5, flicModeButtonY + 5,
                flicModeButtonWidth - 10, flicModeButtonHeight - 10);

        game.shapeRenderer.end();
    }

    private void handleInput() {
        if (Gdx.input.justTouched()) {
            Vector3 touchPos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touchPos);

            if (touchPos.x >= normalModeButtonX && touchPos.x <= normalModeButtonX + normalModeButtonWidth &&
                touchPos.y >= normalModeButtonY && touchPos.y <= normalModeButtonY + normalModeButtonHeight) {
                startGame(false);
            }

            if (touchPos.x >= flicModeButtonX && touchPos.x <= flicModeButtonX + flicModeButtonWidth &&
                touchPos.y >= flicModeButtonY && touchPos.y <= flicModeButtonY + flicModeButtonHeight) {
                startGame(true);
            }
        }
    }

    private void startGame(boolean isFlicMode) {
        game.setScreen(new GameScreen(game, isFlicMode));
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
