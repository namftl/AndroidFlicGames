package com.slidegame.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class GameScreen implements Screen, GestureDetector.GestureListener, ButtonListener {
    private final SlideGame game;
    private OrthographicCamera camera;
    private Viewport viewport;

    private Ball ball;
    private List<Brick> bricks;
    private boolean isFlicMode;

    private Vector2 flickStart;
    private Vector2 flickEnd;
    private boolean isDragging;
    private Vector2 ballStartPosition;

    private int strikes;
    private int currentTarget;
    private static final int MAX_STRIKES = 3;

    private static final float BALL_RADIUS = SlideGame.VIRTUAL_HEIGHT / 14f;
    private static final float BRICK_RADIUS = 40f;
    private static final int BRICK_COUNT = 5;
    private static final int MIN_NUMBER = 1;
    private static final int MAX_NUMBER = 15;
    private static final float MIN_DISTANCE_FROM_CENTER = 150f;
    private static final float MIN_BRICK_SPACING = 120f;

    public GameScreen(final SlideGame game, boolean isFlicMode) {
        this.game = game;
        this.isFlicMode = isFlicMode;

        camera = new OrthographicCamera();
        camera.setToOrtho(false, SlideGame.VIRTUAL_WIDTH, SlideGame.VIRTUAL_HEIGHT);
        viewport = new FitViewport(SlideGame.VIRTUAL_WIDTH, SlideGame.VIRTUAL_HEIGHT, camera);

        ballStartPosition = new Vector2(SlideGame.VIRTUAL_WIDTH / 2f, SlideGame.VIRTUAL_HEIGHT / 2f);
        ball = new Ball(ballStartPosition.x, ballStartPosition.y, BALL_RADIUS);

        flickStart = new Vector2();
        flickEnd = new Vector2();
        isDragging = false;

        strikes = 0;
        bricks = new ArrayList<Brick>();
        spawnBricks();
        updateCurrentTarget();

        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.SPACE && GameScreen.this.isFlicMode) {
                    onButtonPressed();
                    return true;
                }
                return false;
            }
        });
        multiplexer.addProcessor(new GestureDetector(GameScreen.this));

        Gdx.input.setInputProcessor(multiplexer);
        game.setButtonListener(this);
    }

    private void spawnBricks() {
        Random random = new Random();
        Set<Integer> usedNumbers = new HashSet<Integer>();
        List<Vector2> positions = new ArrayList<Vector2>();

        int attempts = 0;
        int maxAttempts = 1000;

        while (bricks.size() < BRICK_COUNT && attempts < maxAttempts) {
            attempts++;

            int number = random.nextInt(MAX_NUMBER - MIN_NUMBER + 1) + MIN_NUMBER;
            if (usedNumbers.contains(number)) {
                continue;
            }

            float x = random.nextFloat() * (SlideGame.VIRTUAL_WIDTH - 2 * BRICK_RADIUS) + BRICK_RADIUS;
            float y = random.nextFloat() * (SlideGame.VIRTUAL_HEIGHT - 2 * BRICK_RADIUS) + BRICK_RADIUS;
            Vector2 newPos = new Vector2(x, y);

            float distanceFromCenter = newPos.dst(ballStartPosition);
            if (distanceFromCenter < MIN_DISTANCE_FROM_CENTER) {
                continue;
            }

            boolean tooClose = false;
            for (Vector2 existingPos : positions) {
                if (newPos.dst(existingPos) < MIN_BRICK_SPACING) {
                    tooClose = true;
                    break;
                }
            }

            if (tooClose) {
                continue;
            }

            usedNumbers.add(number);
            positions.add(newPos);
            bricks.add(new Brick(x, y, BRICK_RADIUS, number));
        }
    }

    private void updateCurrentTarget() {
        int lowest = Integer.MAX_VALUE;
        for (Brick brick : bricks) {
            if (brick.getNumber() < lowest && !brick.isExploding()) {
                lowest = brick.getNumber();
            }
        }
        currentTarget = lowest;
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.15f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        game.batch.setProjectionMatrix(camera.combined);
        game.shapeRenderer.setProjectionMatrix(camera.combined);

        ball.update(delta);

        for (int i = bricks.size() - 1; i >= 0; i--) {
            Brick brick = bricks.get(i);
            brick.update(delta);

            if (brick.isExplosionComplete()) {
                bricks.remove(i);
                updateCurrentTarget();
            }
        }

        if (ball.isVisible()) {
            checkBrickCollisions();
        }

        if (bricks.isEmpty()) {
            game.setScreen(new CelebrationScreen(game));
            return;
        }

        if (strikes >= MAX_STRIKES) {
            game.setScreen(new GameOverScreen(game));
            return;
        }

        for (Brick brick : bricks) {
            brick.render(game.shapeRenderer, game.batch, game.font);
        }

        ball.render(game.batch, game.shapeRenderer);

        if (isDragging && ball.isVisible()) {
            renderFlickIndicator();
        }

        renderUI();
    }

    private void checkBrickCollisions() {
        for (Brick brick : bricks) {
            if (brick.checkCollision(ball)) {
                if (brick.getNumber() == currentTarget) {
                    brick.explode();
                    ball.hide();
                    respawnBall();
                } else {
                    ball.hide();
                    respawnBall();
                    strikes++;
                }
                break;
            }
        }
    }

    private void renderFlickIndicator() {
        game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        game.shapeRenderer.setColor(1, 1, 0, 0.5f);
        game.shapeRenderer.rectLine(flickStart.x, flickStart.y, flickEnd.x, flickEnd.y, 5);

        game.shapeRenderer.setColor(1, 0, 0, 0.7f);
        game.shapeRenderer.circle(flickEnd.x, flickEnd.y, 10, 16);
        game.shapeRenderer.end();
    }

    private void renderUI() {
        game.batch.begin();
        game.font.getData().setScale(1.0f);

        String mode = isFlicMode ? "Flic Mode" : "Normal Mode";
        game.font.draw(game.batch, mode, 10, SlideGame.VIRTUAL_HEIGHT - 10);

        game.font.draw(game.batch, "Next: " + currentTarget, 10, SlideGame.VIRTUAL_HEIGHT - 40);

        game.font.draw(game.batch, "Strikes: " + strikes + "/" + MAX_STRIKES, 10, SlideGame.VIRTUAL_HEIGHT - 70);

        if (isFlicMode && isDragging) {
            game.font.setColor(1, 1, 0, 1);
            game.font.draw(game.batch, "Press Flic to shoot!",
                    SlideGame.VIRTUAL_WIDTH / 2f - 80,
                    SlideGame.VIRTUAL_HEIGHT - 10);
            game.font.setColor(1, 1, 1, 1);
        }

        game.batch.end();
    }

    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        return false;
    }

    @Override
    public boolean tap(float x, float y, int count, int button) {
        return false;
    }

    @Override
    public boolean longPress(float x, float y) {
        return false;
    }

    @Override
    public boolean fling(float velocityX, float velocityY, int button) {
        if (!isFlicMode && !ball.isMoving() && ball.isVisible()) {
            Vector3 worldCoords = camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
            float distanceToBall = Vector2.dst(worldCoords.x, worldCoords.y, ball.getPosition().x, ball.getPosition().y);

            if (distanceToBall < ball.getRadius() + 50) {
                ball.applyFlick(-velocityX / 60f, velocityY / 60f);
                isDragging = false;
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        if (!ball.isMoving() && ball.isVisible()) {
            Vector3 worldCoords = camera.unproject(new Vector3(x, y, 0));

            if (!isDragging) {
                float distanceToBall = Vector2.dst(worldCoords.x, worldCoords.y, ball.getPosition().x, ball.getPosition().y);
                if (distanceToBall < ball.getRadius() + 50) {
                    isDragging = true;
                    flickStart.set(ball.getPosition());
                }
            }

            if (isDragging) {
                flickEnd.set(worldCoords.x, worldCoords.y);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean panStop(float x, float y, int pointer, int button) {
        if (!isFlicMode && isDragging && !ball.isMoving() && ball.isVisible()) {
            Vector2 flickVector = new Vector2(flickStart.x - flickEnd.x, flickStart.y - flickEnd.y);
            ball.applyFlick(flickVector.x, flickVector.y);
            isDragging = false;
            return true;
        }

        if (isFlicMode && isDragging) {
            isDragging = false;
            return true;
        }

        return false;
    }

    @Override
    public boolean zoom(float initialDistance, float distance) {
        return false;
    }

    @Override
    public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
        return false;
    }

    @Override
    public void pinchStop() {
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
        ball.dispose();
    }

    private void respawnBall() {
        ball.setPosition(ballStartPosition.x, ballStartPosition.y);
    }

    @Override
    public void onButtonPressed() {
        if (isFlicMode && isDragging && !ball.isMoving() && ball.isVisible()) {
            Vector2 flickVector = new Vector2(flickStart.x - flickEnd.x, flickStart.y - flickEnd.y);
            ball.applyFlick(flickVector.x, flickVector.y);
            isDragging = false;
        }
    }
}
