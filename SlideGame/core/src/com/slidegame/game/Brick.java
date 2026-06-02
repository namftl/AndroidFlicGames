package com.slidegame.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

public class Brick {
    private Vector2 position;
    private float radius;
    private int number;
    private Color color;
    private boolean exploding;
    private float explosionTime;
    private float explosionRadius;
    private static final float EXPLOSION_DURATION = 0.5f;
    private static final float MAX_EXPLOSION_RADIUS = 100f;

    public Brick(float x, float y, float radius, int number) {
        this.position = new Vector2(x, y);
        this.radius = radius;
        this.number = number;
        this.color = new Color(0.8f, 0.4f, 0.1f, 1.0f); // Orange-brown
        this.exploding = false;
        this.explosionTime = 0;
    }

    public void update(float delta) {
        if (exploding) {
            explosionTime += delta;
            explosionRadius = radius + (MAX_EXPLOSION_RADIUS - radius) * (explosionTime / EXPLOSION_DURATION);
        }
    }

    public void render(ShapeRenderer shapeRenderer, SpriteBatch batch, BitmapFont font) {
        if (exploding) {
            renderExplosion(shapeRenderer);
        } else {
            renderNormal(shapeRenderer, batch, font);
        }
    }

    private void renderNormal(ShapeRenderer shapeRenderer, SpriteBatch batch, BitmapFont font) {
        // Draw brick circle
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(color);
        shapeRenderer.circle(position.x, position.y, radius, 32);
        shapeRenderer.end();

        // Draw number on brick
        batch.begin();
        String text = String.valueOf(number);
        GlyphLayout layout = new GlyphLayout(font, text);
        float textX = position.x - layout.width / 2;
        float textY = position.y + layout.height / 2;
        font.setColor(Color.WHITE);
        font.draw(batch, text, textX, textY);
        batch.end();
    }

    private void renderExplosion(ShapeRenderer shapeRenderer) {
        float progress = explosionTime / EXPLOSION_DURATION;
        float alpha = 1.0f - progress;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Outer ring - orange
        shapeRenderer.setColor(1.0f, 0.5f, 0.0f, alpha * 0.6f);
        shapeRenderer.circle(position.x, position.y, explosionRadius, 32);

        // Middle ring - yellow
        shapeRenderer.setColor(1.0f, 1.0f, 0.0f, alpha * 0.8f);
        shapeRenderer.circle(position.x, position.y, explosionRadius * 0.7f, 32);

        // Inner ring - red
        shapeRenderer.setColor(1.0f, 0.0f, 0.0f, alpha);
        shapeRenderer.circle(position.x, position.y, explosionRadius * 0.4f, 32);

        shapeRenderer.end();
    }

    public void explode() {
        exploding = true;
        explosionTime = 0;
        explosionRadius = radius;
    }

    public boolean checkCollision(Ball ball) {
        if (exploding) return false;
        float distance = position.dst(ball.getPosition());
        return distance < (radius + ball.getRadius());
    }

    public boolean isExploding() {
        return exploding;
    }

    public boolean isExplosionComplete() {
        return exploding && explosionTime >= EXPLOSION_DURATION;
    }

    public Vector2 getPosition() {
        return position;
    }

    public float getRadius() {
        return radius;
    }

    public int getNumber() {
        return number;
    }
}
