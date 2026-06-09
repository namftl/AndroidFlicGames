package com.slidegame.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

public class Ball {
    private Vector2 position;
    private Vector2 velocity;
    private float radius;
    private boolean visible;
    private Texture texture;

    private static final float FRICTION = 0.98f;
    private static final float BOUNCE_DAMPING = 0.7f;
    private static final float MIN_VELOCITY = 0.5f;

    public Ball(float x, float y, float radius) {
        this.position = new Vector2(x, y);
        this.velocity = new Vector2(0, 0);
        this.radius = radius;
        this.visible = true;
        this.texture = createGlowingBallTexture(512);
    }

    private Texture createGlowingBallTexture(int size) {
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);

        int centerX = size / 2;
        int centerY = size / 2;
        float maxRadius = size / 2f;

        // Draw from outside to inside for proper layering
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                float dx = x - centerX;
                float dy = y - centerY;
                float distance = (float) Math.sqrt(dx * dx + dy * dy);
                float normalizedDist = distance / maxRadius;

                if (normalizedDist <= 1.0f) {
                    // Create glow effect with multiple layers
                    float alpha = 1.0f - normalizedDist;
                    alpha = (float) Math.pow(alpha, 0.5); // Soften falloff

                    // Color gradient: bright cyan core to deep blue edges
                    float r, g, b;
                    if (normalizedDist < 0.3f) {
                        // Bright cyan/white core
                        r = 0.5f + (1.0f - normalizedDist / 0.3f) * 0.5f;
                        g = 0.8f + (1.0f - normalizedDist / 0.3f) * 0.2f;
                        b = 1.0f;
                    } else if (normalizedDist < 0.7f) {
                        // Mid blue glow
                        float t = (normalizedDist - 0.3f) / 0.4f;
                        r = 0.2f * (1.0f - t);
                        g = 0.6f * (1.0f - t);
                        b = 1.0f;
                    } else {
                        // Outer glow (soft edge)
                        float t = (normalizedDist - 0.7f) / 0.3f;
                        r = 0.1f * (1.0f - t);
                        g = 0.4f * (1.0f - t);
                        b = 0.8f;
                        alpha *= (1.0f - t); // Fade out at edges
                    }

                    pixmap.setColor(r, g, b, alpha);
                    pixmap.drawPixel(x, y);
                }
            }
        }

        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    public void update(float delta) {
        if (!visible) {
            return;
        }

        position.x += velocity.x * delta;
        position.y += velocity.y * delta;

        velocity.scl(FRICTION);

        if (velocity.len() < MIN_VELOCITY) {
            velocity.set(0, 0);
        }

        handleScreenBounds();
    }

    private void handleScreenBounds() {
        if (position.x - radius < 0) {
            position.x = radius;
            velocity.x = -velocity.x * BOUNCE_DAMPING;
        } else if (position.x + radius > SlideGame.VIRTUAL_WIDTH) {
            position.x = SlideGame.VIRTUAL_WIDTH - radius;
            velocity.x = -velocity.x * BOUNCE_DAMPING;
        }

        if (position.y - radius < 0) {
            position.y = radius;
            velocity.y = -velocity.y * BOUNCE_DAMPING;
        } else if (position.y + radius > SlideGame.VIRTUAL_HEIGHT) {
            position.y = SlideGame.VIRTUAL_HEIGHT - radius;
            velocity.y = -velocity.y * BOUNCE_DAMPING;
        }
    }

    public void applyFlick(float flickX, float flickY) {
        float flickStrength = 3.0f;
        velocity.set(flickX * flickStrength, flickY * flickStrength);

        float maxSpeed = 800f;
        if (velocity.len() > maxSpeed) {
            velocity.nor().scl(maxSpeed);
        }
    }

    public void render(SpriteBatch batch, ShapeRenderer shapeRenderer) {
        if (!visible) {
            return;
        }

        // Enable stencil buffer
        Gdx.gl.glEnable(GL20.GL_STENCIL_TEST);
        Gdx.gl.glClear(GL20.GL_STENCIL_BUFFER_BIT);

        // Step 1: Write circle to stencil buffer
        Gdx.gl.glStencilFunc(GL20.GL_ALWAYS, 1, 0xFF);
        Gdx.gl.glStencilOp(GL20.GL_KEEP, GL20.GL_KEEP, GL20.GL_REPLACE);
        Gdx.gl.glStencilMask(0xFF);
        Gdx.gl.glColorMask(false, false, false, false);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.circle(position.x, position.y, radius, 32);
        shapeRenderer.end();

        // Step 2: Draw texture only where stencil = 1
        Gdx.gl.glColorMask(true, true, true, true);
        Gdx.gl.glStencilFunc(GL20.GL_EQUAL, 1, 0xFF);
        Gdx.gl.glStencilMask(0x00);

        float size = radius * 2;
        batch.begin();
        batch.draw(texture, position.x - radius, position.y - radius, size, size);
        batch.end();

        // Step 3: Disable stencil test
        Gdx.gl.glDisable(GL20.GL_STENCIL_TEST);
    }

    public void dispose() {
        texture.dispose();
    }

    public Vector2 getPosition() {
        return position;
    }

    public float getRadius() {
        return radius;
    }

    public boolean isMoving() {
        return velocity.len() > MIN_VELOCITY;
    }

    public void setPosition(float x, float y) {
        position.set(x, y);
        velocity.set(0, 0);
        visible = true;
    }

    public void hide() {
        visible = false;
        velocity.set(0, 0);
    }

    public boolean isVisible() {
        return visible;
    }
}
