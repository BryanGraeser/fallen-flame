package com.fallenflame.game;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.utils.JsonValue;
import com.fallenflame.game.physics.obstacle.ObstacleCanvas;
import com.fallenflame.game.physics.obstacle.WheelObstacle;
import com.fallenflame.game.util.*;
import com.badlogic.gdx.graphics.*;

import java.lang.reflect.Field;

public class FlareModel extends WheelObstacle {
    // Physics constants
    /** The force with which flare is originally thrown */
    private float initialForce;
    /** The amount to slow the character down */
    private float damping;

    /** The current horizontal movement of the flare */
    private Vector2 movement = new Vector2();
    // TODO #1: Required?
    /** Whether or not to animate the current frame */
    private boolean animate = false;
    // TODO #1

    /** FilmStrip pointer to the texture region */
    private FilmStrip filmstrip;
    /** The current animation frame of the flare */
    private int startFrame;

    /**
     * Returns the directional movement of this flare.
     *
     * This is the result of initial force with damping over time.
     *
     * @return the directional movement of this flare.
     */
    public Vector2 getMovement() {
        return movement;
    }

    /**
     * Sets the directional movement of this flare.
     *
     * This is the result of initial force with damping over time.
     *
     * @param value the directional movement of this flare.
     */
    public void setMovement(Vector2 value) { setMovement(value.x,value.y); }

    /**
     * Sets the directional movement of this flare.
     *
     * This is the result of initial force with damping over time.
     *
     * @param dx the horizontal movement of this flare.
     * @param dy the horizontal movement of this flare.
     */
    public void setMovement(float dx, float dy) {
        movement.set(dx,dy);
    }

    /**
     * Returns how much force to apply to get the flare moving
     *
     * @return how much force to apply to get the flare moving
     */
    public float getInitialForce() {
        return initialForce;
    }

    /**
     * Sets how much force to apply to get the dude moving
     *
     * Multiply this by the input to get the movement value.
     *
     * @param value	how much force to apply to get the dude moving
     */
    public void setInitialForce(float value) {
        initialForce = value;
    }

    /**
     * Returns how hard the brakes are applied to get flare to stop moving
     *
     * @return how hard the brakes are applied to get flare to stop moving
     */
    public float getDamping() {
        return damping;
    }

    /**
     * Sets how hard the brakes are applied to get flare to stop moving
     *
     * @param value	how hard the brakes are applied to get flare to stop moving
     */
    public void setDamping(float value) {
        damping = value;
    }

    /**
     * Returns the current animation frame of this flare.
     *
     * @return the current animation frame of this flare.
     */
    public float getStartFrame() {
        return startFrame;
    }

    /**
     * Sets the animation frame of this flare.
     *
     * @param value	animation frame of this flare.
     */
    public void setStartFrame(int value) {
        startFrame = value;
    }

    /**
     * Creates a new dude with input position and json settings
     *
     * The main purpose of this constructor is to set the initial capsule orientation.
     */
    public FlareModel(float[] pos, JsonValue json) {
        super(pos[0],pos[1],1.0f);
        setFixedRotation(false);
        initialize(json);
    }

    /**
     * Initializes the flare via the given JSON value
     *
     * The JSON value has been parsed and is part of a bigger level file.  However,
     * this JSON value is limited to the dude subtree
     *
     * @param json	the JSON subtree defining the dude
     */
    public void initialize(JsonValue json) {
        setName(json.name());
        float radius = json.get("radius").asFloat();
        setRadius(radius);

        // TODO #2: Technically, we should do error checking here.
        // A JSON field might accidentally be missing
        setBodyType(json.get("bodytype").asString().equals("static") ? BodyDef.BodyType.StaticBody : BodyDef.BodyType.DynamicBody);
        setDensity(json.get("density").asFloat());
        setFriction(json.get("friction").asFloat());
        setRestitution(json.get("restitution").asFloat());
        setInitialForce(json.get("initialforce").asFloat());
        setDamping(json.get("damping").asFloat());
        setStartFrame(json.get("startframe").asInt());

        // Create the collision filter (used for light penetration)
        short collideBits = JsonAssetManager.bitStringToShort(json.get("collideBits").asString());
        short excludeBits = JsonAssetManager.bitStringToComplement(json.get("excludeBits").asString());
        Filter filter = new Filter();
        filter.categoryBits = collideBits;
        filter.maskBits = excludeBits;
        setFilterData(filter);

        // Create debug color
        // Reflection is best way to convert name to color
        Color debugColor;
        try {
            String cname = json.get("debugcolor").asString().toUpperCase();
            Field field = Class.forName("com.badlogic.gdx.graphics.Color").getField(cname);
            debugColor = new Color((Color)field.get(null));
        } catch (Exception e) {
            debugColor = null; // Not defined
        }
        int opacity = json.get("debugopacity").asInt();
        debugColor.mul(opacity/255.0f);
        setDebugColor(debugColor);

        // Get the texture from the AssetManager singleton
        String key = json.get("texture").asString();
        TextureRegion texture = JsonAssetManager.getInstance().getEntry(key, TextureRegion.class);
        try {
            filmstrip = (FilmStrip)texture;
        } catch (Exception e) {
            filmstrip = null;
        }
        setTexture(texture);
    }

    /**
     * Applies the force to the body of this dude
     *
     * This method should be called after the force attribute is set.
     */
    public void applyInitialForce() {
        if (!isActive()) {
            return;
        }

//        // Only walk or spin if we allow it
//        setLinearVelocity(Vector2.Zero);
//        setAngularVelocity(0.0f);

        // Apply force for movement
        body.applyForce(initialForce,getPosition(),true);
        animate = true;
    }

    /**
     * Updates the object's physics state (NOT GAME LOGIC).
     *
     * We use this method to reset cooldowns.
     *
     * @param dt Number of seconds since last animation frame
     */
    public void update(float dt) {
        // Animate if necessary
        if (animate) {
            if (filmstrip != null) {
                int next = (filmstrip.getFrame()+1) % filmstrip.getSize();
                filmstrip.setFrame(next);
            }
        } else {
            if (filmstrip != null) {
                filmstrip.setFrame(startFrame);
            }
        }

        super.update(dt);
    }

    /**
     * Draws the physics object.
     *
     * @param canvas Drawing context
     */
    public void draw(ObstacleCanvas canvas) {
        if (texture != null) {
            canvas.draw(texture,Color.WHITE,origin.x,origin.y,getX()*drawScale.x,getY()*drawScale.y,getAngle(),1.0f,1.0f);
        }
    }
}
