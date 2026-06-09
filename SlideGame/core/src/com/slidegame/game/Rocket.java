package com.slidegame.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

public class Rocket {
    private Vector2 position;
    private Vector2 velocity;
    private float width;
    private float height;
    private boolean active;
    private Texture texture;
    private Vector2 homePosition;
    private boolean returningHome;

    private static final float ROCKET_SPEED = 600f;
    private static final float RETURN_SPEED = 800f;
    private static final float WIDTH = 12f;
    private static final float HEIGHT = 30f;

    public Rocket(float startX, float startY, float dirX, float dirY) {
        this.position = new Vector2(startX, startY);
        this.homePosition = new Vector2(startX, startY);

        Vector2 direction = new Vector2(dirX, dirY).nor();
        this.velocity = direction.scl(ROCKET_SPEED);

        this.width = WIDTH;
        this.height = HEIGHT;
        this.active = true;
        this.returningHome = false;
        this.texture = createRocketTexture();
    }

    private Texture createRocketTexture() {
        int w = (int) WIDTH * 2;
        int h = (int) HEIGHT * 2;
        Pixmap pixmap = new Pixmap(w, h, Pixmap.Format.RGBA8888);

        // Draw rocket body (gradient from orange/red at back to yellow at tip)
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                float centerX = w / 2f;
                float dx = Math.abs(x - centerX);
                float normalizedY = y / (float) h;

                // Rocket shape (narrower at tip)
                float maxWidth = (w / 2f) * (0.5f + normalizedY * 0.5f);

                if (dx < maxWidth) {
                    float r, g, b;
                    // Color gradient from back (red/orange) to tip (bright yellow)
                    if (normalizedY < 0.3f) {
                        // Tip - bright yellow/white
                        r = 1.0f;
                        g = 1.0f;
                        b = 0.7f;
                    } else if (normalizedY < 0.7f) {
                        // Mid - orange
                        r = 1.0f;
                        g = 0.5f;
                        b = 0.1f;
                    } else {
                        // Back - red with glow
                        r = 1.0f;
                        g = 0.2f;
                        b = 0.0f;
                    }

                    // Edge fade for glow effect
                    float edgeFade = 1.0f - (dx / maxWidth);
                    edgeFade = (float) Math.pow(edgeFade, 0.5);

                    pixmap.setColor(r, g, b, edgeFade);
                    pixmap.drawPixel(x, y);
                }
            }
        }

        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    public void update(float delta) {
        if (!active) {
            return;
        }

        position.x += velocity.x * delta;
        position.y += velocity.y * delta;

        // Check if off screen
        if (position.x < -width || position.x > SlideGame.VIRTUAL_WIDTH + width ||
            position.y < -height || position.y > SlideGame.VIRTUAL_HEIGHT + height) {
            active = false;
        }
    }

    public void returnHome() {
        if (!returningHome) {
            returningHome = true;
            Vector2 directionHome = new Vector2(homePosition.x - position.x, homePosition.y - position.y).nor();
            velocity = directionHome.scl(RETURN_SPEED);
        }
    }

    public boolean hasReachedHome() {
        if (!returningHome) {
            return false;
        }
        float distance = position.dst(homePosition);
        return distance < 10f;
    }

    public void render(SpriteBatch batch) {
        if (!active) {
            return;
        }

        batch.begin();

        // Calculate rotation angle based on velocity direction
        float angle = (float) Math.toDegrees(Math.atan2(velocity.y, velocity.x)) + 90f;

        batch.draw(texture,
            position.x - width / 2,
            position.y - height / 2,
            width / 2,  // origin x
            height / 2, // origin y
            width,
            height,
            1f, 1f,     // scale
            angle,      // rotation
            0, 0,       // src x, y
            texture.getWidth(),
            texture.getHeight(),
            false, false);

        batch.end();
    }

    public void dispose() {
        texture.dispose();
    }

    public Vector2 getPosition() {
        return position;
    }

    public boolean isActive() {
        return active;
    }

    public void deactivate() {
        active = false;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public boolean isReturningHome() {
        return returningHome;
    }
}
