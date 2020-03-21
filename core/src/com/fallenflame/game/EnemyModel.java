package com.fallenflame.game;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.utils.JsonValue;
import com.fallenflame.game.util.FilmStrip;
import com.fallenflame.game.util.JsonAssetManager;

public class EnemyModel extends CharacterModel {
    protected boolean activated;

    protected Vector2 playerLastKnown = new Vector2();

    /**
     * Initializes the player via the given JSON value
     *
     * The JSON value has been parsed and is part of a bigger level file.  However,
     * this JSON value is limited to the player subtree
     *
     * @param json	the JSON subtree defining the player
     */
    public void initialize(JsonValue json) {
        super.initialize(json);
        // Enemy specific initialization
        // Now get the texture from the AssetManager singleton
        String key = getDefaultTexture(); // TODO: should get from JSON?
        TextureRegion texture = JsonAssetManager.getInstance().getEntry(key, TextureRegion.class);
        try {
            filmstrip = (FilmStrip)texture;
        } catch (Exception e) {
            filmstrip = null;
        }
        setTexture(texture);
    }

    protected float getDefaultMaxSpeed() {
        return super.getDefaultMaxSpeed() * 1.25f;
    }

    protected String getDefaultTexture() {
        return "enemy-walking";
    }

    public boolean getActivated() {
        return this.activated;
    }

    /**
     * Sets enemy's active status
     * @param activated
     */
    public void setActivated(boolean activated) {
        this.activated = true;
    }

    /**
     * Get player's last known position
     * @return copy of player's last known position
     */
    public Vector2 getPlayerLastKnown() {
        return playerLastKnown.cpy();
    }

    /**
     * Get x-coor of player's last known position
     * @return x-coordinate
     */
    public float getPlayerLastKnownX() {
        return playerLastKnown.x;
    }

    /**
     * Get y-coor of player's last known position
     * @return y-coordinate
     */
    public float getPlayerLastKnownY() {
        return playerLastKnown.y;
    }

    /**
     * Set player's last known position
     * @param v player's last known position
     */
    public void setPlayerLastKnown(Vector2 v) {
        playerLastKnown.set(v);
    }

    /**
     * Set player's last known position
     * @param x player's last known position x-coor
     * @param y player's last known position y-coor
     */
    public void setPlayerLastKnown(float x, float y) {
        this.setPlayerLastKnown(new Vector2(x, y));
    }
}
