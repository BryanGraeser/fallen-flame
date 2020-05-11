package com.fallenflame.game;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonValue;
import com.fallenflame.game.util.FilmStrip;
import com.fallenflame.game.util.JsonAssetManager;

/**
 * Player avatar for the plaform game.
 *
 * Note that the constructor does very little.  The true initialization happens
 * by reading the JSON value.
 */
public class PlayerModel extends CharacterModel {
    /** Player Life types enum */
    protected enum LifeState {
        ALIVE,
        DYING,
        DEAD
    }
    private LifeState life;

    /** Max flares player can hold. Also determines UI flare indicators */
    private int maxFlareCount;
    /** Number of flares the player has left */
    private int flareCount;

    /** Radius of player's light */
    protected float lightRadius;
    protected float minLightRadius;
    protected float lightRadiusSaved;
    protected float lightRadiusSprint;
    protected float lightRadiusSneak;
    /** Player sneak and spring left (once hits 0, a ghost is deployed on the map)
     *  powerVal must be greater than or equal to 0 */
    protected int powerVal;
    /** Max sneak and spring player can have at a given level */
    protected int maxPowerVal;

    /**Tint of player light */
    protected Color tint;

    /**Player walk sound */
    protected Sound walkSound;

    /**Player is making walk sound */
    protected boolean playingSound;

    /** Fire buddy filmstrip*/
    private FilmStrip fireBuddyFilmstrip;
    private FilmStrip fireBuddyLeft;
    private FilmStrip fireBuddyRight;
    private FilmStrip fireBuddyUp;
    private FilmStrip fireBuddyDown;
    private FilmStrip fireBuddyThrow;

    /** Origin of fire buddy when drawing not in sneak mode */
    protected Vector2 fireBuddyOrigin;

    /** Origin of drawing for fire buddy when player is in sneak mode */
    protected Vector2 fireBuddySneak;

    /** Filmstrip of player death */
    private FilmStrip deathFilmstripRight;
    private FilmStrip deathFilmstripLeft;

    /**
     * Initializes the character via the given JSON value
     *
     * @param globalJson	the JSON subtree defining global player data
     * @param levelJson     the JSON subtree defining level data
     */
    public void initialize(JsonValue globalJson, JsonValue levelJson) {
        super.initialize(globalJson, levelJson.get("playerpos").asFloatArray());
        // Global json data
        lightRadiusSprint = globalJson.get("sprintlightrad").asInt();
        lightRadiusSneak = globalJson.get("sneaklightrad").asInt();
        minLightRadius = globalJson.get("minlightradius").asInt();
        lightRadius = minLightRadius;
        float[] tintValues = globalJson.get("tint").asFloatArray();//RGBA
        tint = new Color(tintValues[0], tintValues[1], tintValues[2], tintValues[3]);

        // Level json data
        flareCount = levelJson.has("startFlareCount") ?
                levelJson.get("startFlareCount").asInt() : globalJson.get("standardflarecount").asInt();
        maxFlareCount = flareCount;
        powerVal = levelJson.has("startSneakVal") ?
                levelJson.get("startSneakVal").asInt() : globalJson.get("defaultStartSneakVal").asInt();
        maxPowerVal = powerVal;

        String walkSoundKey = globalJson.get("walksound").asString();
        walkSound = JsonAssetManager.getInstance().getEntry(walkSoundKey, Sound.class);

        life = LifeState.ALIVE;
    }

    @Override
    public void initializeTextures(JsonValue json){
        super.initializeTextures(json);

        JsonValue textureJson = json.get("texture");

        String key = textureJson.get("death-left").asString();
        texture = JsonAssetManager.getInstance().getEntry(key, TextureRegion.class);
        try {
            deathFilmstripLeft = (FilmStrip) texture;
            deathFilmstripLeft.setFrame(0); //reset filmstrips in cases where the player dies and the level resets
        } catch (Exception e){
            deathFilmstripLeft = null;
        }

        key = textureJson.get("death-right").asString();
        texture = JsonAssetManager.getInstance().getEntry(key, TextureRegion.class);
        try {
            deathFilmstripRight = (FilmStrip) texture;
            deathFilmstripRight.setFrame(0); //reset filmstrips in cases where the player dies and the level resets
        } catch (Exception e){
            deathFilmstripRight = null;
        }

        JsonValue firebuddy = json.get("firebuddy");
        textureJson = firebuddy.get("texture");

        key = textureJson.get("left").asString();
        texture = JsonAssetManager.getInstance().getEntry(key, TextureRegion.class);
        try {
            fireBuddyLeft = (FilmStrip) texture;
        } catch (Exception e) {
            fireBuddyLeft = null;
        }

        key = textureJson.get("right").asString();
        TextureRegion texture = JsonAssetManager.getInstance().getEntry(key, TextureRegion.class);
        try {
            fireBuddyRight = (FilmStrip) texture;
        } catch (Exception e) {
            fireBuddyRight = null;
        }

        key = textureJson.get("down").asString();
        texture = JsonAssetManager.getInstance().getEntry(key, TextureRegion.class);
        try {
            fireBuddyDown = (FilmStrip) texture;
        } catch (Exception e) {
            fireBuddyDown = null;
        }

        key = textureJson.get("up").asString();
        texture = JsonAssetManager.getInstance().getEntry(key, TextureRegion.class);
        try {
            fireBuddyUp = (FilmStrip) texture;
        } catch (Exception e) {
            fireBuddyUp = null;
        }

        //pick default direction
        fireBuddyFilmstrip = fireBuddyRight;

        float offsetX = firebuddy.get("textureoffset").get("x").asFloat();
        float offsetY = firebuddy.get("textureoffset").get("y").asFloat();
        //set fire buddy origin;
        setFireBuddyOrigin (((TextureRegion)fireBuddyFilmstrip).getRegionWidth()/2.0f + offsetX * drawScale.x,
                                ((TextureRegion)fireBuddyFilmstrip).getRegionHeight()/2.0f + offsetY * drawScale.y);

        offsetX = firebuddy.get("sneaktextureoffset").get("x").asFloat();
        offsetY = firebuddy.get("sneaktextureoffset").get("y").asFloat();
        //set fire buddy sneak origin
        setFireBuddySneak(((TextureRegion)fireBuddyFilmstrip).getRegionWidth()/2.0f + offsetX * drawScale.x,
                ((TextureRegion)fireBuddyFilmstrip).getRegionHeight()/2.0f + offsetY * drawScale.y);
    }

    /**
     * @return the origin of the firebuddy texture
     * as a Vector 2
     */
    protected Vector2 getFireBuddyOrigin() { return new Vector2(fireBuddyOrigin); }

    /**
     * @return the origin of the firebuddy texture
     * along the x-axis
     */
    protected float getFireBuddyOriginX() { return fireBuddyOrigin.x; }

    /**
     * @return the origin of the firebuddy texture
     * along the y-axis
     */
    protected float getFireBuddyOriginY() {return fireBuddyOrigin.y; }

    /**
     * Sets the origin of the fire buddy to be (x, y)
     * The y-axis is positive downwards, and the x-axis is positive rightwards
     *
     * @param x the offset of the texture on the x-axis
     * @param y the offset of the texture on the y-axis
     */
    protected void setFireBuddyOrigin(float x, float y){
        fireBuddyOrigin = new Vector2(x, y);
    }

    /**
     * @return the origin of the firebuddy texture when sneaking
     * as a Vector 2
     */
    protected Vector2 getFireBuddySneak() { return new Vector2(fireBuddySneak); }

    /**
     * @return the origin of the firebuddy texture when sneaking
     * along the x-axis
     */
    protected float getFireBuddySneakX() { return fireBuddySneak.x; }

    /**
     * @return the origin of the firebuddy texture when sneaking
     * along the y-axis
     */
    protected float getFireBuddySneakY() {return fireBuddySneak.y; }

    /**
     * Sets the origin of the fire buddy to be (x, y) when sneaking
     * The y-axis is positive downwards, and the x-axis is positive rightwards
     *
     * @param x the offset of the texture on the x-axis
     * @param y the offset of the texture on the y-axis
     */
    protected void setFireBuddySneak(float x, float y){
        fireBuddySneak = new Vector2(x, y);
    }

    /**
     * Returns the minimum light radius the player can have
     *
     * @return minimum light radius
     */
    public float getMinLightRadius() { return minLightRadius; }

    /**
     * Returns max flare count
     */
    public int getMaxFlareCount() { return maxFlareCount; }

    /**
     * Returns the number of flares the player has left
     *
     * @return the number of flares the player has left
     */
    public int getFlareCount() { return flareCount; }

    /**
     * Decrement flare count (for firing a flare)
     */
    public void decFlareCount() { flareCount--; }

    /**
     * Increment flare count (for picking up a flare) iff player not at max flares
     * @return True if player is able to pick it up, else false
     */
    public boolean incFlareCount() {
        if(flareCount < maxFlareCount){
            flareCount++;
            return true;
        }
        return false;
    }

    /**
     * Gets player light radius
     * @return light radius
     */
    public float getLightRadius() {
        return lightRadius;
    }

    /** Get amount of sneak and spring updates left for player */
    public int getPowerVal() { return powerVal; }

    /** Get maximum amount of sneak and spring updates left for player on this level*/
    public int getMaxPowerVal() { return maxPowerVal; }

    /** Decrement sneak and spring value by 1 (for 1 update of sneaking) */
    public void decPowerVal() { powerVal--; }

    /**
     * Gets player color tint
     * @return light color
     */
    public Color getLightColor() {
        return tint;
    }

    /**
     * Sets player light radius (does not include sneak speed)
     * @param r light radius
     */
    public void setLightRadius(float r) {
        lightRadius = Math.max(r, minLightRadius);
    }

    /**
     * Sets player to sneak light radius (not reachable by scrolling)
     */
    public void setLightRadiusSneak() { lightRadius = lightRadiusSneak; }

    /**
     * Sets the player's life state to dying. Once the dying animation
     * concludes the player is then set as dead.
     */
    public void die() { life = LifeState.DYING; }

    /**
     * Returns whether the player is dead.
     * @return true if the player is dead and false if player is alive or dying
     */
    public boolean isDead() { return life == LifeState.DEAD; }

    /**
     * Returns whether the player is dying
     * @return true if the player is dying and false if player is alive or dead
     */
    public boolean isDying() { return life == LifeState.DYING; }

    /**
     * Returns whether the player is alive
     * @return true if the player is alive and false if player is dead or dying
     */
    public boolean isAlive() { return life == LifeState.ALIVE; }

    /**
     * Returns the walk sound
     *
     * @return the walk sound
     */
    public Sound getWalkSound() {
        return walkSound;
    }

    /**
     * Gets player light radius when not sprinting or sneaking
     * @return light radius
     */
    public float getLightRadiusSaved() {
        return lightRadiusSaved;
    }

    /**
     * Sets player light radius when not sprinting or sneaking
     * @param r light radius
     */
    public void setLightRadiusSaved(float r) {
        lightRadiusSaved = r;
    }

    /**
     * Gets player light radius when sprinting
     * @return light radius
     */
    public float getLightRadiusSprint() {
        return lightRadiusSprint;
    }

    /**
     * Increments light radius by i (can be positive or negative) ensuring lightRadius is never less than 0.
     * @param i value to increment radius by
     */
    public void incrementLightRadius(float i) { setLightRadius(lightRadius + i); }

    public boolean isPlayingSound() {return playingSound;}

    public void setPlayingSound(boolean status) {playingSound = status;}

    /**
     * Updates the object's physics state (NOT GAME LOGIC).
     *
     * We use this method to reset cooldowns.
     *
     * @param dt Number of seconds since last animation frame
     */
    @Override
    public void update(float dt) {
        //getAngle has up as 0 radians, down as pi radians, pi/2 is left, -pi/2 is right.
        double angle = getAngle();
        if(angle < 0) angle = angle + 2 * Math.PI;
        int angle100 = (int) (angle * 100);

        animateFireBuddy(angle100);

        if(isDying()) {
            if (angle100 > 0 && angle100 < 314 && deathFilmstripLeft != null) {
                filmstrip = deathFilmstripLeft;
            } else if (deathFilmstripRight != null) { //angle between pi and 2pi/0 (inclusive)
                filmstrip = deathFilmstripRight;
            }

            setTexture(filmstrip, textureOffset.x, textureOffset.y);

            int frame = filmstrip.getFrame();
            if (walkCool == 0 && frame < filmstrip.getSize() - 1) {
                walkCool = walkLimit;
                filmstrip.setFrame(frame + 1);
            } else if (walkCool > 0) {
                walkCool--;
            } else if (frame == filmstrip.getSize() - 1){
                life = LifeState.DEAD;
            }
        } else if (isAlive()) {
            super.update(dt);
        }
    }

    /**
     * A helper method for drawing the fire buddy
     * @param angle100 the angle which the player is facing rounded down to the nearest int
     */
    protected void animateFireBuddy(int angle100){
        if(true) { //temporary placeholder for whether the fire buddy is throwing or not
            if (angle100 == 0) {
                fireBuddyFilmstrip = fireBuddyUp;
            } else if (angle100 > 0 && angle100 < 314) {
                fireBuddyFilmstrip = fireBuddyLeft;
            } else if (angle100 == 314) {
                fireBuddyFilmstrip = fireBuddyDown;
            } else {
                fireBuddyFilmstrip = fireBuddyRight;
            }

            // Animate if necessary
            // Do not change values of walkCool and animate, to be done in parent.
            if (animate && walkCool == 0 && fireBuddyFilmstrip != null) {
                int next = (fireBuddyFilmstrip.getFrame() + 1) % fireBuddyFilmstrip.getSize();
                fireBuddyFilmstrip.setFrame(next);
            } else if (!animate && fireBuddyFilmstrip != null) {
                fireBuddyFilmstrip.setFrame(startFrame);
            }
        } else {

        }
    }

    public void draw(GameCanvas canvas) {
        super.draw(canvas);
        if (fireBuddyFilmstrip != null) {
            canvas.draw(fireBuddyFilmstrip, Color.WHITE,
                    isSneaking() ? getFireBuddySneakX() : getFireBuddyOriginX(),
                    isSneaking() ? getFireBuddySneakY() : getFireBuddyOriginY(),
                    getX()*drawScale.x,getY()*drawScale.y,0 ,1,1);
        }
    }
}