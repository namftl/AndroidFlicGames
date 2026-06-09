package com.slidegame.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

public class Platform {
    private Vector2 position;
    private float width;
    private float height;

    public Platform(float x, float y, float width, float height) {
        this.position = new Vector2(x, y);
        this.width = width;
        this.height = height;
    }

    public void render(ShapeRenderer shapeRenderer) {
        shapeRenderer.setColor(0.6f, 0.4f, 0.2f, 1f); // Brown platform
        shapeRenderer.rect(position.x, position.y, width, height);

        // Highlight edge
        shapeRenderer.setColor(0.8f, 0.6f, 0.3f, 1f);
        shapeRenderer.rect(position.x, position.y + height - 3, width, 3);
    }

    public Vector2 getPosition() {
        return position;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public float getTop() {
        return position.y + height;
    }

    public boolean contains(float x, float y) {
        return x >= position.x && x <= position.x + width &&
               y >= position.y && y <= position.y + height;
    }
}
