package com.fallenflame.game.enemies;

import com.badlogic.gdx.math.Vector2;

public class EnemyTypeBModel extends EnemyModel{
    /** Position to sustain fire towards. Player last known location */
    protected Vector2 firingTarget;

    /**
     * Set enemy's firing target position
     * @param v Vector representing enemy's firing target position
     */
    public void setFiringTarget(Vector2 v) {
        firingTarget = v;
    }

    /**
     * Set enemy's firing target position
     * @param x x-coor of enemy's target
     * @param y y-coor of enemy's target
     */
    public void setFiringTarget(float x, float y) {
        setFiringTarget(new Vector2(x, y));
    }

    /**
     * Get enemy's firing target
     * @return Vector2 target coordinates
     */
    public Vector2 getFiringTarget() { return firingTarget; }

    /**
     * Executes enemy action
     * @param ctrlCode action for enemy to execute. can be left, right, up, down movement or no action
     * @return true if enemy has moved
     */
    public boolean executeAction(int ctrlCode) {

        return false;
    }
}
