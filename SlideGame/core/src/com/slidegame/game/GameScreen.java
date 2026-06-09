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
    private String gameMode;
    private boolean isFlicMode;
    private boolean isRocketMode;
    private boolean isPlatformerMode;

    private Vector2 flickStart;
    private Vector2 flickEnd;
    private boolean isDragging;
    private Vector2 ballStartPosition;

    private List<Rocket> rockets;
    private float rocketCooldown;
    private static final float ROCKET_COOLDOWN_TIME = 0.3f;

    // Platformer mode fields
    private Character character;
    private List<Bullseye> bullseyes;
    private List<Platform> platforms;
    private List<Vector2> drawnPath;
    private int bullseyesCollected;
    private Set<Integer> taggedTargets;
    private int nextLaserTarget;
    private boolean showingLaser;
    private float laserTimer;
    private Vector2 cannonPosition;
    private Vector2 laserTarget;
    private static final float LASER_DURATION = 0.3f;
    private float goButtonX, goButtonY, goButtonWidth, goButtonHeight;
    private static final float GO_BUTTON_WIDTH = 100f;
    private static final float GO_BUTTON_HEIGHT = 60f;

    private int strikes;
    private int currentTarget;
    private static final int MAX_STRIKES = 3;

    private static final float BALL_RADIUS = SlideGame.VIRTUAL_HEIGHT / 14f;
    private static final float BRICK_RADIUS = 40f;
    private static final float PLATFORMER_BRICK_RADIUS = 20f; // Smaller for platformer mode
    private static final int BRICK_COUNT = 5;
    private static final int MIN_NUMBER = 1;
    private static final int MAX_NUMBER = 15;
    private static final float MIN_DISTANCE_FROM_CENTER = 150f;
    private static final float MIN_BRICK_SPACING = 120f;

    public GameScreen(final SlideGame game, String gameMode) {
        this.game = game;
        this.gameMode = gameMode;
        this.isFlicMode = gameMode.equals("flic");
        this.isRocketMode = gameMode.equals("rocket");
        this.isPlatformerMode = gameMode.equals("platformer");

        camera = new OrthographicCamera();
        camera.setToOrtho(false, SlideGame.VIRTUAL_WIDTH, SlideGame.VIRTUAL_HEIGHT);
        viewport = new FitViewport(SlideGame.VIRTUAL_WIDTH, SlideGame.VIRTUAL_HEIGHT, camera);

        ballStartPosition = new Vector2(SlideGame.VIRTUAL_WIDTH / 2f, SlideGame.VIRTUAL_HEIGHT / 2f);
        ball = new Ball(ballStartPosition.x, ballStartPosition.y, BALL_RADIUS);

        flickStart = new Vector2();
        flickEnd = new Vector2();
        isDragging = false;

        rockets = new ArrayList<Rocket>();
        rocketCooldown = 0f;

        strikes = 0;
        bricks = new ArrayList<Brick>();

        // Platformer mode initialization
        if (isPlatformerMode) {
            platforms = new ArrayList<Platform>();
            createPlatformerLevel();
            spawnPlatformerBricks();

            character = new Character(50f, 50f); // Start at bottom-left
            bullseyes = new ArrayList<Bullseye>();
            drawnPath = new ArrayList<Vector2>();
            bullseyesCollected = 0;
            taggedTargets = new HashSet<Integer>();
            nextLaserTarget = 1;
            showingLaser = false;
            laserTimer = 0f;
            cannonPosition = new Vector2(SlideGame.VIRTUAL_WIDTH / 2f, 30f);
            laserTarget = new Vector2();

            // GO button position (bottom-right)
            goButtonX = SlideGame.VIRTUAL_WIDTH - GO_BUTTON_WIDTH - 20;
            goButtonY = 20;
            goButtonWidth = GO_BUTTON_WIDTH;
            goButtonHeight = GO_BUTTON_HEIGHT;

            spawnBullseyes();
        } else {
            spawnBricks();
        }

        updateCurrentTarget();

        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.SPACE) {
                    if (GameScreen.this.isFlicMode || GameScreen.this.isPlatformerMode) {
                        onButtonPressed();
                        return true;
                    }
                }
                // G key also triggers GO in platformer mode (for desktop)
                if (keycode == Input.Keys.G && GameScreen.this.isPlatformerMode) {
                    executeCharacterMovement();
                    return true;
                }
                return false;
            }

            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                if (isPlatformerMode) {
                    Vector3 worldCoords = camera.unproject(new Vector3(screenX, screenY, 0));

                    // Check GO button
                    if (worldCoords.x >= goButtonX && worldCoords.x <= goButtonX + goButtonWidth &&
                        worldCoords.y >= goButtonY && worldCoords.y <= goButtonY + goButtonHeight) {
                        executeCharacterMovement();
                        return true;
                    }
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

    private void createPlatformerLevel() {
        // Donkey Kong style platforms - multiple levels
        float platformHeight = 15f;

        // Ground platform
        platforms.add(new Platform(0, 0, SlideGame.VIRTUAL_WIDTH, platformHeight));

        // Level 1 - low platforms
        platforms.add(new Platform(50, 100, 200, platformHeight));
        platforms.add(new Platform(350, 100, 200, platformHeight));
        platforms.add(new Platform(650, 100, 120, platformHeight));

        // Level 2 - mid platforms
        platforms.add(new Platform(100, 200, 180, platformHeight));
        platforms.add(new Platform(400, 200, 250, platformHeight));

        // Level 3 - high platforms
        platforms.add(new Platform(50, 300, 150, platformHeight));
        platforms.add(new Platform(300, 300, 200, platformHeight));
        platforms.add(new Platform(600, 300, 170, platformHeight));

        // Level 4 - top platform
        platforms.add(new Platform(200, 400, 400, platformHeight));
    }

    private void spawnPlatformerBricks() {
        Random random = new Random();
        Set<Integer> usedNumbers = new HashSet<Integer>();

        // Place bricks on platforms (not ground)
        for (int i = 1; i < platforms.size() && bricks.size() < BRICK_COUNT; i++) {
            Platform platform = platforms.get(i);

            int number = random.nextInt(MAX_NUMBER - MIN_NUMBER + 1) + MIN_NUMBER;
            while (usedNumbers.contains(number)) {
                number = random.nextInt(MAX_NUMBER - MIN_NUMBER + 1) + MIN_NUMBER;
            }
            usedNumbers.add(number);

            // Place brick on platform
            float x = platform.getPosition().x + random.nextFloat() * (platform.getWidth() - 50) + 25;
            float y = platform.getTop() + PLATFORMER_BRICK_RADIUS + 5;

            bricks.add(new Brick(x, y, PLATFORMER_BRICK_RADIUS, number));
        }
    }

    private void spawnBullseyes() {
        Random random = new Random();

        // Spawn bullseyes on platforms, NOT near bricks
        for (Platform platform : platforms) {
            if (platform.getPosition().y == 0) continue; // Skip ground

            // Try to place 2-3 bullseyes per platform
            int bullseyeCount = random.nextInt(2) + 1;
            for (int i = 0; i < bullseyeCount && bullseyes.size() < BRICK_COUNT; i++) {
                float x = platform.getPosition().x + random.nextFloat() * (platform.getWidth() - 40) + 20;
                float y = platform.getTop() + 15;

                // Make sure not too close to any brick
                boolean tooClose = false;
                for (Brick brick : bricks) {
                    if (Vector2.dst(x, y, brick.getPosition().x, brick.getPosition().y) < 60) {
                        tooClose = true;
                        break;
                    }
                }

                if (!tooClose) {
                    bullseyes.add(new Bullseye(x, y));
                }
            }
        }
    }

    private void executeCharacterMovement() {
        if (drawnPath.size() > 0 && !character.isMoving()) {
            character.startMoving(new ArrayList<Vector2>(drawnPath));
        }
    }

    private void updatePlatformerMode(float delta) {
        if (character != null) {
            character.update(delta);

            // Check bullseye collection
            for (Bullseye bullseye : bullseyes) {
                if (bullseye.checkCollection(character.getPosition(), character.getRadius())) {
                    bullseyesCollected++;
                }
            }

            // Check target tagging
            for (Brick brick : bricks) {
                if (!taggedTargets.contains(brick.getNumber()) && bullseyesCollected > 0) {
                    float distance = character.getPosition().dst(brick.getPosition());
                    if (distance < brick.getRadius() + character.getRadius()) {
                        taggedTargets.add(brick.getNumber());
                        bullseyesCollected--;
                    }
                }
            }
        }

        // Update laser
        if (showingLaser) {
            laserTimer -= delta;
            if (laserTimer <= 0) {
                showingLaser = false;
            }
        }
    }

    private void fireLaser() {
        if (!showingLaser && taggedTargets.size() > 0) {
            // Fire at the lowest tagged target number
            int targetToShoot = Integer.MAX_VALUE;
            for (Integer taggedNum : taggedTargets) {
                if (taggedNum < targetToShoot) {
                    targetToShoot = taggedNum;
                }
            }

            // Find and shoot the brick
            for (Brick brick : bricks) {
                if (brick.getNumber() == targetToShoot) {
                    laserTarget.set(brick.getPosition());
                    showingLaser = true;
                    laserTimer = LASER_DURATION;

                    // Hit correct target
                    if (brick.getNumber() == currentTarget) {
                        brick.explode();
                        taggedTargets.remove(targetToShoot);
                    } else {
                        // Wrong target
                        strikes++;
                        taggedTargets.remove(targetToShoot);
                    }
                    break;
                }
            }
        }
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

        if (rocketCooldown > 0) {
            rocketCooldown -= delta;
        }

        if (isPlatformerMode) {
            updatePlatformerMode(delta);
        } else if (!isRocketMode) {
            ball.update(delta);
        }

        updateRockets(delta);

        for (int i = bricks.size() - 1; i >= 0; i--) {
            Brick brick = bricks.get(i);
            brick.update(delta);

            if (brick.isExplosionComplete()) {
                bricks.remove(i);
                updateCurrentTarget();
            }
        }

        if (ball.isVisible() && !isRocketMode) {
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

        if (!isPlatformerMode) {
            ball.render(game.batch, game.shapeRenderer);
        }

        for (Rocket rocket : rockets) {
            rocket.render(game.batch);
        }

        if (isPlatformerMode) {
            renderPlatformerMode();
        } else if (isDragging && ball.isVisible()) {
            renderFlickIndicator();
        }

        renderUI();
    }

    private void updateRockets(float delta) {
        for (int i = rockets.size() - 1; i >= 0; i--) {
            Rocket rocket = rockets.get(i);
            rocket.update(delta);

            if (!rocket.isActive()) {
                rocket.dispose();
                rockets.remove(i);
                continue;
            }

            if (rocket.isReturningHome() && rocket.hasReachedHome()) {
                rocket.deactivate();
                strikes++;
                continue;
            }

            if (!rocket.isReturningHome()) {
                checkRocketBrickCollision(rocket);
            }
        }
    }

    private void checkRocketBrickCollision(Rocket rocket) {
        for (Brick brick : bricks) {
            float dx = rocket.getPosition().x - brick.getPosition().x;
            float dy = rocket.getPosition().y - brick.getPosition().y;
            float distance = (float) Math.sqrt(dx * dx + dy * dy);

            if (distance < brick.getRadius() + rocket.getWidth() / 2) {
                if (brick.getNumber() == currentTarget) {
                    brick.explode();
                    rocket.deactivate();
                } else {
                    rocket.returnHome();
                }
                break;
            }
        }
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

    private void renderPlatformerMode() {
        game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Render platforms
        for (Platform platform : platforms) {
            platform.render(game.shapeRenderer);
        }

        // Render bullseyes
        for (Bullseye bullseye : bullseyes) {
            bullseye.render(game.shapeRenderer);
        }

        // Render cannon
        game.shapeRenderer.setColor(0.5f, 0.5f, 0.5f, 1f);
        game.shapeRenderer.rect(cannonPosition.x - 15, cannonPosition.y, 30, 20);

        // Render tagged indicators on bricks
        for (Brick brick : bricks) {
            if (taggedTargets.contains(brick.getNumber())) {
                game.shapeRenderer.setColor(1f, 0f, 0f, 0.5f);
                game.shapeRenderer.circle(brick.getPosition().x, brick.getPosition().y, brick.getRadius() + 5);
            }
        }

        // Render laser
        if (showingLaser) {
            game.shapeRenderer.setColor(1f, 0f, 0f, 0.8f);
            game.shapeRenderer.rectLine(cannonPosition.x, cannonPosition.y + 20, laserTarget.x, laserTarget.y, 5);
        }

        // Render drawn path
        if (drawnPath.size() > 1) {
            game.shapeRenderer.setColor(1f, 1f, 1f, 0.7f);
            for (int i = 0; i < drawnPath.size() - 1; i++) {
                Vector2 p1 = drawnPath.get(i);
                Vector2 p2 = drawnPath.get(i + 1);
                game.shapeRenderer.rectLine(p1.x, p1.y, p2.x, p2.y, 3);
            }
        }

        // Render GO button
        game.shapeRenderer.setColor(0.2f, 0.8f, 0.2f, 0.8f);
        game.shapeRenderer.rect(goButtonX, goButtonY, goButtonWidth, goButtonHeight);
        game.shapeRenderer.setColor(1, 1, 1, 0.2f);
        game.shapeRenderer.rect(goButtonX + 3, goButtonY + 3, goButtonWidth - 6, goButtonHeight - 6);

        game.shapeRenderer.end();

        // Render character
        game.batch.begin();
        if (character != null) {
            character.render(game.batch);
        }

        // GO button text
        game.font.getData().setScale(1.2f);
        game.font.draw(game.batch, "GO", goButtonX + 30, goButtonY + 40);
        game.font.getData().setScale(1.0f);

        game.batch.end();
    }

    private void renderUI() {
        game.batch.begin();
        game.font.getData().setScale(1.0f);

        String mode = isPlatformerMode ? "Platformer Mode" : (isRocketMode ? "Rocket Mode" : (isFlicMode ? "Flic Mode" : "Normal Mode"));
        game.font.draw(game.batch, mode, 10, SlideGame.VIRTUAL_HEIGHT - 10);

        game.font.draw(game.batch, "Next: " + currentTarget, 10, SlideGame.VIRTUAL_HEIGHT - 40);

        game.font.draw(game.batch, "Strikes: " + strikes + "/" + MAX_STRIKES, 10, SlideGame.VIRTUAL_HEIGHT - 70);

        if (isPlatformerMode) {
            game.font.draw(game.batch, "Bullseyes: " + bullseyesCollected, 10, SlideGame.VIRTUAL_HEIGHT - 100);
            game.font.draw(game.batch, "Tagged: " + taggedTargets.size(), 10, SlideGame.VIRTUAL_HEIGHT - 130);

            // Desktop controls hint
            game.font.getData().setScale(0.8f);
            game.font.setColor(0.7f, 0.7f, 0.7f, 1f);
            game.font.draw(game.batch, "Draw path -> G to GO -> SPACE to shoot", 10, 30);
            game.font.setColor(1, 1, 1, 1);
            game.font.getData().setScale(1.0f);
        }

        if (isFlicMode && isDragging) {
            game.font.setColor(1, 1, 0, 1);
            game.font.draw(game.batch, "Press Flic to shoot!",
                    SlideGame.VIRTUAL_WIDTH / 2f - 80,
                    SlideGame.VIRTUAL_HEIGHT - 10);
            game.font.setColor(1, 1, 1, 1);
        }

        if (isRocketMode && rocketCooldown > 0) {
            game.font.setColor(1, 0.5f, 0, 1);
            game.font.draw(game.batch, "Cooldown: " + String.format("%.1f", rocketCooldown),
                    SlideGame.VIRTUAL_WIDTH - 150,
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
        Vector3 worldCoords = camera.unproject(new Vector3(x, y, 0));

        if (isPlatformerMode && !character.isMoving()) {
            // Don't draw over GO button
            if (worldCoords.x >= goButtonX && worldCoords.x <= goButtonX + goButtonWidth &&
                worldCoords.y >= goButtonY && worldCoords.y <= goButtonY + goButtonHeight) {
                return false;
            }

            if (!isDragging) {
                // Start new path
                drawnPath.clear();
                drawnPath.add(new Vector2(worldCoords.x, worldCoords.y));
                isDragging = true;
            } else {
                // Add point to path
                drawnPath.add(new Vector2(worldCoords.x, worldCoords.y));
            }
            return true;
        }

        if (isRocketMode) {
            if (!isDragging) {
                isDragging = true;
                flickStart.set(ballStartPosition);
            }
            flickEnd.set(worldCoords.x, worldCoords.y);
            return true;
        }

        if (!ball.isMoving() && ball.isVisible()) {
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
        if (isPlatformerMode && isDragging) {
            isDragging = false;
            return true;
        }

        if (isRocketMode && isDragging && rocketCooldown <= 0) {
            Vector2 flickVector = new Vector2(flickEnd.x - flickStart.x, flickEnd.y - flickStart.y);
            if (flickVector.len() > 10) {
                shootRocket(flickVector.x, flickVector.y);
                rocketCooldown = ROCKET_COOLDOWN_TIME;
            }
            isDragging = false;
            return true;
        }

        if (!isFlicMode && !isRocketMode && isDragging && !ball.isMoving() && ball.isVisible()) {
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

    private void shootRocket(float dirX, float dirY) {
        Rocket rocket = new Rocket(ballStartPosition.x, ballStartPosition.y, dirX, dirY);
        rockets.add(rocket);
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
        for (Rocket rocket : rockets) {
            rocket.dispose();
        }
        if (character != null) {
            character.dispose();
        }
    }

    private void respawnBall() {
        ball.setPosition(ballStartPosition.x, ballStartPosition.y);
    }

    @Override
    public void onButtonPressed() {
        if (isPlatformerMode) {
            Gdx.app.log("SlideGame", "Fire laser! Tagged targets: " + taggedTargets.size() + ", showing laser: " + showingLaser);
            fireLaser();
        } else if (isFlicMode && isDragging && !ball.isMoving() && ball.isVisible()) {
            Vector2 flickVector = new Vector2(flickStart.x - flickEnd.x, flickStart.y - flickEnd.y);
            ball.applyFlick(flickVector.x, flickVector.y);
            isDragging = false;
        }
    }
}
