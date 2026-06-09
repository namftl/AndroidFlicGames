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

    private float rocketModeButtonX;
    private float rocketModeButtonY;
    private float rocketModeButtonWidth;
    private float rocketModeButtonHeight;

    private float platformerModeButtonX;
    private float platformerModeButtonY;
    private float platformerModeButtonWidth;
    private float platformerModeButtonHeight;

    private static final float BUTTON_WIDTH = 300f;
    private static final float BUTTON_HEIGHT = 80f;
    private static final float BUTTON_SPACING = 20f;

    public MainMenuScreen(final SlideGame game) {
        this.game = game;

        camera = new OrthographicCamera();
        camera.setToOrtho(false, SlideGame.VIRTUAL_WIDTH, SlideGame.VIRTUAL_HEIGHT);
        viewport = new FitViewport(SlideGame.VIRTUAL_WIDTH, SlideGame.VIRTUAL_HEIGHT, camera);

        float centerX = SlideGame.VIRTUAL_WIDTH / 2f;
        float centerY = SlideGame.VIRTUAL_HEIGHT / 2f;

        float totalHeight = (BUTTON_HEIGHT * 4) + (BUTTON_SPACING * 3);
        float startY = centerY + totalHeight / 2f - BUTTON_HEIGHT;

        normalModeButtonWidth = BUTTON_WIDTH;
        normalModeButtonHeight = BUTTON_HEIGHT;
        normalModeButtonX = centerX - BUTTON_WIDTH / 2f;
        normalModeButtonY = startY;

        flicModeButtonWidth = BUTTON_WIDTH;
        flicModeButtonHeight = BUTTON_HEIGHT;
        flicModeButtonX = centerX - BUTTON_WIDTH / 2f;
        flicModeButtonY = startY - BUTTON_HEIGHT - BUTTON_SPACING;

        rocketModeButtonWidth = BUTTON_WIDTH;
        rocketModeButtonHeight = BUTTON_HEIGHT;
        rocketModeButtonX = centerX - BUTTON_WIDTH / 2f;
        rocketModeButtonY = startY - (BUTTON_HEIGHT + BUTTON_SPACING) * 2;

        platformerModeButtonWidth = BUTTON_WIDTH;
        platformerModeButtonHeight = BUTTON_HEIGHT;
        platformerModeButtonX = centerX - BUTTON_WIDTH / 2f;
        platformerModeButtonY = startY - (BUTTON_HEIGHT + BUTTON_SPACING) * 3;
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        game.batch.setProjectionMatrix(camera.combined);
        game.shapeRenderer.setProjectionMatrix(camera.combined);

        game.batch.begin();
        game.batch.draw(game.backgroundTexture, 0, 0, SlideGame.VIRTUAL_WIDTH, SlideGame.VIRTUAL_HEIGHT);
        game.batch.end();

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

        game.font.draw(game.batch, "Rocket Mode",
                rocketModeButtonX + 70,
                rocketModeButtonY + rocketModeButtonHeight / 2f + 10);

        game.font.draw(game.batch, "Platformer Mode",
                platformerModeButtonX + 50,
                platformerModeButtonY + platformerModeButtonHeight / 2f + 10);

        game.batch.end();
    }

    private void renderButtons() {
        game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        game.shapeRenderer.setColor(0.2f, 0.6f, 1.0f, 0.8f);
        game.shapeRenderer.rect(normalModeButtonX, normalModeButtonY, normalModeButtonWidth, normalModeButtonHeight);

        game.shapeRenderer.setColor(1.0f, 0.5f, 0.0f, 0.8f);
        game.shapeRenderer.rect(flicModeButtonX, flicModeButtonY, flicModeButtonWidth, flicModeButtonHeight);

        game.shapeRenderer.setColor(1.0f, 0.2f, 0.2f, 0.8f);
        game.shapeRenderer.rect(rocketModeButtonX, rocketModeButtonY, rocketModeButtonWidth, rocketModeButtonHeight);

        game.shapeRenderer.setColor(0.5f, 0.0f, 0.8f, 0.8f);
        game.shapeRenderer.rect(platformerModeButtonX, platformerModeButtonY, platformerModeButtonWidth, platformerModeButtonHeight);

        game.shapeRenderer.setColor(1, 1, 1, 0.2f);
        game.shapeRenderer.rect(normalModeButtonX + 5, normalModeButtonY + 5,
                normalModeButtonWidth - 10, normalModeButtonHeight - 10);
        game.shapeRenderer.rect(flicModeButtonX + 5, flicModeButtonY + 5,
                flicModeButtonWidth - 10, flicModeButtonHeight - 10);
        game.shapeRenderer.rect(rocketModeButtonX + 5, rocketModeButtonY + 5,
                rocketModeButtonWidth - 10, rocketModeButtonHeight - 10);
        game.shapeRenderer.rect(platformerModeButtonX + 5, platformerModeButtonY + 5,
                platformerModeButtonWidth - 10, platformerModeButtonHeight - 10);

        game.shapeRenderer.end();
    }

    private void handleInput() {
        if (Gdx.input.justTouched()) {
            Vector3 touchPos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touchPos);

            if (touchPos.x >= normalModeButtonX && touchPos.x <= normalModeButtonX + normalModeButtonWidth &&
                touchPos.y >= normalModeButtonY && touchPos.y <= normalModeButtonY + normalModeButtonHeight) {
                startGame("normal");
            }

            if (touchPos.x >= flicModeButtonX && touchPos.x <= flicModeButtonX + flicModeButtonWidth &&
                touchPos.y >= flicModeButtonY && touchPos.y <= flicModeButtonY + flicModeButtonHeight) {
                startGame("flic");
            }

            if (touchPos.x >= rocketModeButtonX && touchPos.x <= rocketModeButtonX + rocketModeButtonWidth &&
                touchPos.y >= rocketModeButtonY && touchPos.y <= rocketModeButtonY + rocketModeButtonHeight) {
                startGame("rocket");
            }

            if (touchPos.x >= platformerModeButtonX && touchPos.x <= platformerModeButtonX + platformerModeButtonWidth &&
                touchPos.y >= platformerModeButtonY && touchPos.y <= platformerModeButtonY + platformerModeButtonHeight) {
                startGame("platformer");
            }
        }
    }

    private void startGame(String mode) {
        game.setScreen(new GameScreen(game, mode));
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
