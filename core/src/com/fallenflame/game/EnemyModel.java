package com.fallenflame.game;

import com.badlogic.gdx.math.Vector2;

public class EnemyModel extends PlayerModel {
    protected boolean activated;

    protected Vector2 playerLastKnown = new Vector2();

    @Override
    protected float defaultMaxSpeed() {
        return super.defaultMaxSpeed() * 1.25f;
    }

    @Override
    protected String defaultTexture() {
        return "enemy-walking";
    }

    public boolean getActivated() {
        return this.activated;
    }

    public void setActivated(boolean activated) {
        this.activated = true;
    }

    public Vector2 getPlayerLastKnown() {
        return playerLastKnown.cpy();
    }

    public float getPlayerLastKnownX() {
        return playerLastKnown.x;
    }

    public float getPlayerLastKnownY() {
        return playerLastKnown.y;
    }

    public void setPlayerLastKnown(Vector2 v) {
        playerLastKnown.set(v);
    }

    public void setPlayerLastKnown(float x, float y) {
        this.setPlayerLastKnown(new Vector2(x, y));
    }

    @Override
    public float getLightRadius() {
        return getActivated() ? 1 : 0;
    }
}
