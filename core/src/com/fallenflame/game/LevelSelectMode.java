package com.fallenflame.game;

import com.badlogic.gdx.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.assets.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.controllers.*;
import com.fallenflame.game.util.*;

public class LevelSelectMode implements Screen, InputProcessor {

    /** Textures temporarily loaded in so I have something to look at as I set up this class */
    private static final String PLAY_BTN_FILE = "textures/fireplay.png";
    private Texture playButton = new Texture(PLAY_BTN_FILE);
    private static final String BACKGROUND_FILE = "textures/firelevelselect.png";
    private Texture background = new Texture(BACKGROUND_FILE);
    /** Amount to scale the play button */
    private static float BUTTON_SCALE  = 0.75f;

    /** Reference to GameCanvas created by the root */
    private GameCanvas canvas;

    /** Listener that will update the player mode when we are done */
    private ScreenListener listener;

    /** Standard window size (for scaling) */
    private static int STANDARD_WIDTH  = 800;
    /** Standard window height (for scaling) */
    private static int STANDARD_HEIGHT = 700;

    /** The height of the canvas window (necessary since sprite origin != screen origin) */
    private int heightY;

    /** Scaling factor for when the student changes the resolution. */
    private float scale;

    /** The current state of the play button */
    private int   pressState;


    public LevelSelectMode(GameCanvas canvas)
    {
        this.canvas  = canvas;
        pressState = 0;

    }

    @Override
    public void show() {
        System.out.println("Showing LevelSelect");
    }

    @Override
    public void render(float delta) {
        canvas.begin();
        canvas.draw(background, 0, 0);
        canvas.draw(playButton, Color.WHITE, playButton.getWidth()/2, playButton.getHeight()/2,
                100, 100, 0, 1, 1);
        canvas.end();
        System.out.println(isReady());
        // We are are ready, notify our listener
        if (isReady() && listener != null) {
            System.out.println("Ready");
            listener.exitScreen(this, 0);
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
        // Compute the drawing scale
        float sx = ((float)width)/STANDARD_WIDTH;
        float sy = ((float)height)/STANDARD_HEIGHT;
        scale = (sx < sy ? sx : sy);


        heightY = height;
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }

    /**
     * Returns true if all assets are loaded and the player is ready to go.
     *
     * @return true if the player is ready to go
     */
    public boolean isReady() {
        return pressState == 2;
    }

    public void setScreenListener(ScreenListener listener) { this.listener = listener; }

    // PROCESSING PLAYER INPUT

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (playButton == null || pressState == 2) {
            return true;
        }

        // Flip to match graphics coordinates
        screenY = heightY-screenY;

        // TODO: Fix scaling
        // Play button is a circle.
        float radius = BUTTON_SCALE*scale*playButton.getWidth()/2.0f;
        float dist = (screenX-(playButton.getWidth()/2))*(screenX-playButton.getWidth()/2)+(screenY-playButton.getHeight()/2)*(screenY-playButton.getHeight()/2);
        if (dist < radius*radius) {
            pressState = 2;
            System.out.println("Clicked");
        }
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }
}
