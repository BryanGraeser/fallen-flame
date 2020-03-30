package com.fallenflame.game;

import com.badlogic.gdx.utils.JsonValue;

/**
 * Player avatar for the plaform game.
 *
 * Note that the constructor does very little.  The true initialization happens
 * by reading the JSON value.
 */
public class PlayerModel extends CharacterModel {
    /** Radius of player's light */
    protected float lightRadius = 0;
    protected float forceSneak = 0;
    protected float forceSprint = 0;
    protected float forceWalk = 0;
    protected float lightRadiusNotSprint = 0;
    protected float lightRadiusSprint = 0;

    public void initialize(JsonValue json) {
        super.initialize(json);
        setFlareCount(json.get("flarecount").asInt());
        setForceSprint(getForce() * 2);
        setForceSneak(getForce() / 2);
        setForceWalk(getForce());
        setLightRadiusSprint(json.get("sprintlightrad").asInt());
    }

    /**
     * Gets player light radius
     * @return light radius
     */
    public float getLightRadius() {
        return lightRadius;
    }

    /**
     * Sets player light radius
     * @param r light radius
     */
    public void setLightRadius(float r) {
        lightRadius = r;
    }

    /**
     * Increments light radius by i (can be positive or negative) ensuring lightRadius is never less than 0.
     * @param i value to increment radius by
     */
    public void incrementLightRadius(float i) { lightRadius = Math.max(lightRadius + i, 0); }

    /**
     * Gets player force for sneaking
     * @return player force for sneaking
     */
    public float getForceSneak() {
        return forceSneak;
    }

    /**
     * Sets player force for sneaking
     * @param r player force for sneaking
     */
    public void setForceSneak(float r) {
        forceSneak = r;
    }

    /**
     * Gets player force for sprinting
     * @return player force for sprinting
     */
    public float getForceSprint() {
        return forceSprint;
    }

    /**
     * Sets player force for sprinting
     * @param r player force for sprinting
     */
    public void setForceSprint(float r) {
        forceSprint = r;
    }

    /**
     * Gets player force for walking
     * @return player force for walking
     */
    public float getForceWalk() {
        return forceWalk;
    }

    /**
     * Sets player force for walking
     * @param r player force for walking
     */
    public void setForceWalk(float r) {
        forceWalk = r;
    }

    /**
     * Gets player light radius when not sprinting
     * @return light radius
     */
    public float getLightRadiusNotSprint() {
        return lightRadiusNotSprint;
    }

    /**
     * Sets player light radius when not sprinting
     * @param r light radius
     */
    public void setLightRadiusNotSprint(float r) {
        lightRadiusNotSprint = r;
    }

    /**
     * Gets player light radius when sprinting
     * @return light radius
     */
    public float getLightRadiusSprint() {
        return lightRadiusSprint;
    }

    /**
     * Sets player light radius when sprinting
     * @param r light radius
     */
    public void setLightRadiusSprint(float r) {
        lightRadiusSprint = r;
    }
}