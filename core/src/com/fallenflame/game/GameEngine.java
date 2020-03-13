package com.fallenflame.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.fallenflame.game.util.JsonAssetManager;
import com.fallenflame.game.util.ScreenListener;

public class GameEngine implements Screen, ContactListener {
    /**Enum to determine if we are loading or not
     * @author: Professor Whie*/
    protected enum AssetState {
        /** No assets loaded */
        EMPTY,
        /** Still loading assets */
        LOADING,
        /** Assets are complete */
        COMPLETE
    }
 /**@author: Professor White */
    private JsonReader jsonReader;
    /**@author: Professor White */
    /** The JSON asset directory */
    private JsonValue assetJson;
    /** The JSON save directory. This will be used to determine what level to */
    private JsonValue saveJson;
    /**@author: Professor White */
    /** The JSON defining the level model */
    private JsonValue levelJson;

    /**@author: Professor White */
    /**What actually keeps track of the assetState. Initially set to empty, as no resources will be in at that point*/
    private AssetState currentAssetState = AssetState.EMPTY;

    /**@author: Professor White */
    /**Main game canvas*/
    protected GameCanvas canvas;
    /**@author: Professor White */
    /** Listener that will update the player mode */
    private ScreenListener listener;

    /** Reference to the level controller */
    protected LevelController level;

    /**Boolean to keep track if the player won the level*/
    private boolean isComplete;

    /**Boolean to keep track if the player died*/
    private boolean isFailed;

    /**Boolean to keep track if the player paused the game*/
    private boolean isPaused;

    /**Boolean to keep track if the screen is active*/
    private boolean isScreenActive;
    /**
     * Preloads the assets for this controller.
     *
     * To make the game modes more for-loop friendly, we opted for nonstatic loaders
     * this time.  However, we still want the assets themselves to be static.  So
     * we have an AssetState that determines the current loading state.  If the
     * assets are already loaded, this method will do nothing.
     *
     * @param manager Reference to global asset manager.
     * @author Professor White
     */
    public void preLoadContent() {
        if (currentAssetState != AssetState.EMPTY) {
            return;
        }

        currentAssetState = AssetState.LOADING;

        jsonReader = new JsonReader();
        assetJson = jsonReader.parse(Gdx.files.internal("jsons/assets.json"));
        saveJson = jsonReader.parse(Gdx.files.internal("jsons/save.json"));

        JsonAssetManager.getInstance().loadDirectory(assetJson);
    }
    /**
     * Load the assets for this controller.
     *
     * To make the game modes more for-loop friendly, we opted for nonstatic loaders
     * this time.  However, we still want the assets themselves to be static.  So
     * we have an AssetState that determines the current loading state.  If the
     * assets are already loaded, this method will do nothing.
     *
     * @param manager Reference to global asset manager.
     * @author: Professor White
     */
    public void loadContent() {
        if (currentAssetState != AssetState.LOADING) {
            return;
        }

        JsonAssetManager.getInstance().allocateDirectory();
        currentAssetState = AssetState.COMPLETE;
    }

    /**
     * Unloads the assets for this game.
     *
     * This method erases the static variables.  It also deletes the associated textures
     * from the asset manager. If no assets are loaded, this method does nothing.
     *
     * @param manager Reference to global asset manager.
     * @author: Professor White
     */
    public void unloadContent() {
        JsonAssetManager.getInstance().unloadDirectory();
        JsonAssetManager.clearInstance();
    }

    /**Getters and setters*/
    /**Return true if the level is complete
     * @return: boolean that is true if the level is completed*/
    public boolean isComplete(){return isComplete;}

    /**Set if the level has been completed
     * @param: boolean isComplete that is true if the level is completed*/

    public void setIsComplete(boolean isComplete){
        this.isComplete = isComplete;
    }
    /**Return true if the level has failed
     * @return: boolean that is true if the level is failed*/
    public boolean isFailed(){return isFailed;}

    /**Set if the level has been failed
     * @param: boolean isFailed that is true if the level is completed*/

    public void setIsFailed(boolean isFailed){
        this.isFailed = isFailed;
    }

    /**Return true if the level has paused
     * @return: boolean that is true if the level is failed*/
    public boolean isPaused(){return isPaused;}

    /**Set if the level has been paused
     * @param: boolean isPaused that is true if the level is completed*/

    public void setIsPaused(boolean isPaused){
        this.isPaused = isPaused;
    }

    /**Return true if that screen is currently active
     * @return: boolean that is true if the level is failed*/
    public boolean isScreenActive(){return isScreenActive;}

    /**Set if the screen is currently active
     * @param: boolean isScreenActive that is true if the level is completed*/

    public void setIsScreenActive(boolean isScreenActive){
        this.isScreenActive = isScreenActive;
    }

    /** Return the current GameCanvas. This GameCanvas will be used across all models and views
     * @return GameCanvas Canvas
     */
    public GameCanvas getCanvas(){return canvas;}

    /** Set the new GameCanvas*/
    public void setCanvas(GameCanvas canvas){this.canvas = canvas;}

    /**
     * Creates a new game world
     *
     * The physics bounds and drawing scale are now stored in the levelController and
     * defined by the appropriate JSON file.
     *
     * Game does not start out as active or paused, but rather in loading
     *
     * Source: Professor White
     */
    public GameEngine() {
        jsonReader = new JsonReader();
        level = new LevelController();
        isComplete = false;
        isFailed = false;
        isScreenActive = false;
        isPaused = false;
    }

    /**
     * Dispose of all (non-static) resources allocated to this mode.
     */
    public void dispose() {
        level.dispose();
        level  = null;
        canvas = null;
    }

    /**
     * Resets the status of the game so that we can play again.
     *
     * This method disposes of the level and creates a new one. It will
     * reread from the JSON file, allowing us to make changes on the fly.
     */
    public void reset() {
        level.dispose();

        setComplete(false);
        setFailure(false);
        countdown = -1;

        // Reload the json each time
        String currentLevelPath = "jsons/" + saveJson.getString("current");
        levelJson = jsonReader.parse(Gdx.files.internal("jsons/level.json"));
        level.populate(levelJson);
        level.getWorld().setContactListener(this);
    }

    /**
     * Returns whether to process the update loop
     *
     * At the start of the update loop, we check if it is time
     * to switch to a new game mode.  If not, the update proceeds
     * normally.
     *
     * @param delta Number of seconds since last animation frame
     *
     * @return whether to process the update loop
     */
    public boolean preUpdate(float dt) {
        InputController input = InputController.getInstance();
        input.readInput();
        if (listener == null) {
            return true;
        }

        // Toggle debug
        if (input.didDebug()) {
            level.setDebug(!level.getDebug());
        }

        // Handle resets
        if (input.didReset()) {
            reset();
        }

        // Now it is time to maybe switch screens.
        if (input.didExit()) {
            listener.exitScreen(this, EXIT_QUIT);
            return false;
        }

        return true;
    }

    private Vector2 angleCache = new Vector2();
    /**
     * The core gameplay loop of this world. This checks if the level has ended
     *
     * @param delta Number of seconds since last animation frame
     * @author: Professor White
     */
    public void update(float delta) {

        InputController input = InputController.getInstance();
        isComplete = level.getLevelState() == level.WIN;
        isFailed = level.getLevelState() == level.LOSS;
        level.update(delta);
    }

    /**
     * Draw the physics objects to the canvas
     *
     * For simple worlds, this method is enough by itself.  It will need
     * to be overriden if the world needs fancy backgrounds or the like.
     *
     * The method draws all objects in the order that they were added.
     *
     * @param canvas The drawing context
     */
    public void draw(float delta) {
        canvas.clear();

        level.draw(canvas);

        // Final message
        if (isComplete) {
            //TODO: Print some message here about winning
        } else if (isFailed) {
            //TODO: Print some message here about winning
        }
    }

    /**
     * Called when the Screen is resized.
     *
     * This can happen at any point during a non-paused state but will never happen
     * before a call to show().
     *
     * @param width  The new width in pixels
     * @param height The new height in pixels
     */
    public void resize(int width, int height) {
        // IGNORE FOR NOW
    }

    /**
     * Called when the Screen should render itself.
     *
     * We defer to the other methods update() and draw().  However, it is VERY important
     * that we only quit AFTER a draw.
     *
     * @param delta Number of seconds since last animation frame
     */
    public void render(float delta) {
        if (isScreenActive) {
            if (preUpdate(delta)) {
                update(delta);
            }
            draw(delta);
        }
    }

    /**
     * Called when the Screen is paused.
     *
     * This is usually when it's not active or visible on screen. An Application is
     * also paused before it is destroyed.
     */
    public void pause() {
       isPaused = true;
    }

    /**
     * Called when the Screen is resumed from a paused state.
     *
     * This is usually when it regains focus.
     */
    public void resume() {
        // TODO Auto-generated method stub
    }

    /**
     * Called when this screen becomes the current screen for a Game.
     * @author: Professor White
     */
    public void show() {
        // Useless if called in outside animation loop
        isScreenActive = true;
    }

    /**
     * Called when this screen is no longer the current screen for a Game.
     * @author: Professor White
     */
    public void hide() {
        // Useless if called in outside animation loop
        isScreenActive = false;
    }

    /**
     * Sets the ScreenListener for this mode
     *
     * The ScreenListener will respond to requests to quit.
     */
    public void setScreenListener(ScreenListener listener) {
        this.listener = listener;
    }

    /**
     * Callback method for the start of a collision
     *
     * This method is called when we first get a collision between two objects.  We use
     * this method to test if it is the "right" kind of collision.  In particular, we
     * use it to test if we made it to the win door.
     *
     * @param contact The two bodies that collided
     */
    public void beginContact(Contact contact) {
    }

    /** Unused ContactListener method */
    public void endContact(Contact contact) {}
    /** Unused ContactListener method */
    public void postSolve(Contact contact, ContactImpulse impulse) {}
    /** Unused ContactListener method */
    public void preSolve(Contact contact, Manifold oldManifold) {}

}