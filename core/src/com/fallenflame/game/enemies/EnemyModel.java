package com.fallenflame.game.enemies;

import com.fallenflame.game.CharacterModel;

public abstract class EnemyModel extends CharacterModel {

    private enum ActivationStates {
        Calm,
        Alert,
        Aggressive
    }

    // Active status
    protected ActivationStates state = ActivationStates.Calm;

    // Constants for the control codes
    // We would normally use an enum here, but Java enums do not bitmask nicely
    /** Do not do anything */
    public static final int CONTROL_NO_ACTION  = 0x00;
    /** Move the ship to the left */
    public static final int CONTROL_MOVE_LEFT  = 0x01;
    /** Move the ship to the right */
    public static final int CONTROL_MOVE_RIGHT = 0x02;
    /** Move the ship to the up */
    public static final int CONTROL_MOVE_UP    = 0x04;
    /** Move the ship to the down */
    public static final int CONTROL_MOVE_DOWN  = 0x08;
    /** Move the ship to the down and left */
    public static final int CONTROL_MOVE_DOWN_LEFT = 0x10;
    /** Move the ship to the down and right */
    public static final int CONTROL_MOVE_DOWN_RIGHT = 0x20;
    /** Move the ship to the up and left */
    public static final int CONTROL_MOVE_UP_LEFT = 0x40;
    /** Move the ship to the up and right */
    public static final int CONTROL_MOVE_UP_RIGHT = 0x80;

    /**
     * @return whether enemy is aggressive
     */
    public boolean isAgressive() {
        return this.state.equals(ActivationStates.Aggressive);
    }

    /**
     * @return whether enemy is alert
     */
    public boolean isAlert() {
        return this.state.equals(ActivationStates.Alert);
    }

    /**
     * @return whether enemy is calm
     */
    public boolean isCalm() {
        return this.state.equals(ActivationStates.Calm);
    }

    /**
     * @return false if the enemy is calm and true otherwise
     */
    public boolean isActivated() {
        return !isCalm();
    }

    /**
     *  Sets the enemy's activation state to calm
     */
    public void makeCalm() {
        this.state = ActivationStates.Calm;
    }

    /**
     *  Sets the enemy's activation state to alert
     */
    public void makeAlert() {
        this.state = ActivationStates.Alert;
    }

    /**
     * Sets the enemy's activation state to aggressive
     */
    public void makeAggressive() {
        this.state = ActivationStates.Aggressive;
    }


    /**
     * Gets light radius for enemy. MAY BE OVERWRITTEN BY CHILD for different light behavior
     * @return light radius
     */
    public float getLightRadius() {
        return isActivated() ? 1.0f : 0.0f;
    }

    public com.badlogic.gdx.graphics.Color getLightColor() {
        if(isAgressive()){return com.badlogic.gdx.graphics.Color.RED;}
        else if (isAlert()){return com.badlogic.gdx.graphics.Color.GREEN;}
        else {return com.badlogic.gdx.graphics.Color.WHITE;}
    }

    /**
     * Executes enemy action
     * @param ctrlCode action for enemy to execute. can be left, right, up, down movement or no action
     * @return true if enemy has moved
     */
    public abstract boolean executeAction(int ctrlCode);
}
