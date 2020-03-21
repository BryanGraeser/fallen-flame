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
        setName(json.name());
        float[] pos  = json.get("pos").asFloatArray();
        float radius = 0.4f;//json.get("radius").asFloat();
        setPosition(pos[0],pos[1]);
        setRadius(radius);

        // Technically, we should do error checking here.
        // A JSON field might accidentally be missing
        setBodyType(BodyDef.BodyType.DynamicBody);
        setDensity(1);
        setFriction(0);
        setRestitution(0);
        setForce(80);
        setDamping(10);
        setMaxSpeed(getDefaultMaxSpeed());
        setStartFrame(0);
        setWalkLimit(4);

        // Reflection is best way to convert name to color
//        Color debugColor;
//        try {
//            String cname = json.get("debugcolor").asString().toUpperCase();
//            Field field = Class.forName("com.badlogic.gdx.graphics.Color").getField(cname);
//            debugColor = new Color((Color)field.get(null));
//        } catch (Exception e) {
//            debugColor = null; // Not defined
//        }
//        int opacity = json.get("debugopacity").asInt();
//        debugColor.mul(opacity/255.0f);
//        setDebugColor(debugColor);

        // Now get the texture from the AssetManager singleton
        String key = getDefaultTexture();
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
