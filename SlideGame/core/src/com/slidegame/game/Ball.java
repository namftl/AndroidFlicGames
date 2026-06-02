package com.slidegame.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
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
        this.texture = new Texture(Gdx.files.internal("ball.png"));
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
