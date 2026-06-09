package com.slidegame.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

public class Bullseye {
    private static final float RADIUS = 15f;
    private Vector2 position;
    private boolean collected;

    public Bullseye(float x, float y) {
        this.position = new Vector2(x, y);
        this.collected = false;
    }

    public void render(ShapeRenderer shapeRenderer) {
        if (collected) return;

        shapeRenderer.setColor(Color.YELLOW);
        shapeRenderer.circle(position.x, position.y, RADIUS);
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.circle(position.x, position.y, RADIUS / 2);
    }

    public boolean checkCollection(Vector2 characterPos, float characterRadius) {
        if (collected) return false;

        float distance = position.dst(characterPos);
        if (distance < RADIUS + characterRadius) {
            collected = true;
            return true;
        }
        return false;
    }

    public Vector2 getPosition() {
        return position;
    }

    public boolean isCollected() {
        return collected;
    }

    public float getRadius() {
        return RADIUS;
    }
}
