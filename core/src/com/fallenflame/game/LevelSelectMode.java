package com.fallenflame.game;

import com.badlogic.gdx.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.assets.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.controllers.*;
import com.fallenflame.game.util.*;

public class LevelSelectMode implements Screen {

    /** Textures temporarily loaded in so I have something to look at as I set up this class */
    private static final String PLAY_BTN_FILE = "textures/fireplay.png";
    private Texture playButton = new Texture(PLAY_BTN_FILE);

    private static final String BACKGROUND_FILE = "textures/firelevelselect.png";
    private Texture background = new Texture(BACKGROUND_FILE);

    /** Reference to GameCanvas created by the root */
    private GameCanvas canvas;

    /** Listener that will update the player mode when we are done */
    private ScreenListener listener;

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

        // We are are ready, notify our listener
        if (isReady() && listener != null) {
            listener.exitScreen(this, 0);
        }
    }

    @Override
    public void resize(int width, int height) {

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
}
