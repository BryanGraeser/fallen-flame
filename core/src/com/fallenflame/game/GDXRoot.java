package com.fallenflame.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;

/*
 * GDXRoot.java
 *
 * This is the primary class file for running the game.  It is the "static main" of
 * LibGDX.  In the first lab, we extended ApplicationAdapter.  In previous lab
 * we extended Game.  This is because of a weird graphical artifact that we do not
 * understand.  Transparencies (in 3D only) is failing when we use ApplicationAdapter.
 * There must be some undocumented OpenGL code in setScreen.
 *
 * This class differs slightly from the labs in that the AssetManager is now a
 * singleton and is not constructed by this class.
 *
 * Author: Walker M. White
 * Version: 3/2/2016
 */
/**Unofficial core root of the game (excluding DesktopLauncher).
 * @author: Walker M. White */
public class GDXRoot extends Game implements ScreenListener {
	/** Drawing context to display graphics */
	private GameCanvas canvas;
	/** Asset Loading Screen. What will show  */
	private LoadingMode loading;
	/** Player mode for the the game */
	private GameEngine engine;

	/**
	 * Creates a new game from the configuration settings.
	 */
	public GDXRoot() {}

	/**
	 * Called when the Application is first created.
	 *
	 * This is method immediately loads assets for the loading screen, and prepares
	 * the asynchronous loader for all other assets.
	 */
	public void create() {
		canvas  = new GameCanvas();
		loading = new LoadingMode(canvas,1);

		// Initialize the three game worlds
		engine = new GameEngine();
		engine.preLoadContent();
		loading.setScreenListener(this);
		setScreen(loading);
	}

	/**
	 * Called when the Application is destroyed.
	 *
	 * This is preceded by a call to pause().
	 */
	public void dispose() {
		// Call dispose on our children
		setScreen(null);
		engine.unloadContent();
		engine.dispose();

		canvas.dispose();
		canvas = null;

		// Unload all of the resources
		super.dispose();
	}

	/**
	 * Called when the Application is resized.
	 *
	 * This can happen at any point during a non-paused state but will never happen
	 * before a call to create().
	 *
	 * @param width  The new width in pixels
	 * @param height The new height in pixels
	 */
	public void resize(int width, int height) {
		canvas.resize();
		super.resize(width,height);
	}

	/**
	 * The given screen has made a request to exit its player mode.
	 *
	 * The value exitCode can be used to implement menu options.
	 *
	 * @param screen   The screen requesting to exit
	 * @param exitCode The state of the screen upon exit
	 */
	public void exitScreen(Screen screen, int exitCode) {
		if (screen == loading) {
			//Still finish loading everything before shutting down
			engine.loadContent();
			engine.setScreenListener(this);
			engine.setCanvas(canvas);
			engine.reset();
			setScreen(engine);

			loading.dispose();
			loading = null;
		} else if (exitCode == engine.EXIT_QUIT) {
			// We quit the main application
			Gdx.app.exit();
		}
	}

}