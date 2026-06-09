package com.slidegame.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

import java.util.List;

public class Character {
    private static final float RADIUS = 20f;
    private static final float MOVE_SPEED = 150f; // pixels per second (slower as requested)

    private Vector2 position;
    private Texture texture;
    private boolean isMoving;
    private List<Vector2> path;
    private int currentPathIndex;
    private float distanceToNext;

    public Character(float x, float y) {
        this.position = new Vector2(x, y);
        this.texture = createCharacterTexture();
        this.isMoving = false;
        this.currentPathIndex = 0;
    }

    private Texture createCharacterTexture() {
        int size = 128;
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);

        int centerX = size / 2;
        int centerY = size / 2;
        int radius = size / 2 - 2;

        // Draw filled circle for character
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                float dx = x - centerX;
                float dy = y - centerY;
                float distance = (float) Math.sqrt(dx * dx + dy * dy);

                if (distance <= radius) {
                    // Purple/magenta character
                    float alpha = 1.0f;
                    if (distance > radius - 2) {
                        alpha = (radius - distance) / 2f;
                    }
                    Color color = new Color(0.8f, 0.2f, 0.8f, alpha);
                    pixmap.setColor(color);
                    pixmap.drawPixel(x, y);
                }
            }
        }

        Texture tex = new Texture(pixmap);
        pixmap.dispose();
        return tex;
    }

    public void startMoving(List<Vector2> path) {
        if (path != null && path.size() > 0) {
            this.path = path;
            this.currentPathIndex = 0;
            this.isMoving = true;
            this.distanceToNext = 0f;
        }
    }

    public void update(float delta) {
        if (!isMoving || path == null || currentPathIndex >= path.size()) {
            isMoving = false;
            return;
        }

        Vector2 targetPoint = path.get(currentPathIndex);
        Vector2 direction = new Vector2(targetPoint).sub(position);
        float distanceToTarget = direction.len();

        if (distanceToTarget < 2f) {
            // Reached this point, move to next
            currentPathIndex++;
            if (currentPathIndex >= path.size()) {
                isMoving = false;
            }
        } else {
            // Move towards target
            direction.nor();
            float moveDistance = MOVE_SPEED * delta;

            if (moveDistance >= distanceToTarget) {
                position.set(targetPoint);
                currentPathIndex++;
                if (currentPathIndex >= path.size()) {
                    isMoving = false;
                }
            } else {
                position.add(direction.scl(moveDistance));
            }
        }
    }

    public void render(SpriteBatch batch) {
        float size = RADIUS * 2;
        batch.draw(texture, position.x - RADIUS, position.y - RADIUS, size, size);
    }

    public Vector2 getPosition() {
        return position;
    }

    public float getRadius() {
        return RADIUS;
    }

    public boolean isMoving() {
        return isMoving;
    }

    public void dispose() {
        texture.dispose();
    }
}
