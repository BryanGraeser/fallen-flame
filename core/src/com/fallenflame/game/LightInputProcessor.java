package com.fallenflame.game;

import com.badlogic.gdx.InputProcessor;
import com.fallenflame.game.GameEngine;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.delay;

public class LightInputProcessor implements InputProcessor {
   private GameEngine gameEngine;

   public LightInputProcessor(GameEngine g){
       gameEngine = g;
   }
    public boolean keyDown (int keycode) {
        return false;
    }

    public boolean keyUp (int keycode) {
        return false;
    }

    public boolean keyTyped (char character) {
        return false;
    }

    public boolean touchDown (int x, int y, int pointer, int button) {
        return false;
    }

    public boolean touchUp (int x, int y, int pointer, int button) {
        return false;
    }

    public boolean touchDragged (int x, int y, int pointer) {
        return false;
    }

    public boolean mouseMoved (int x, int y) {
        return false;
    }

    public boolean scrolled (int amount) {
       gameEngine.lightFromPlayer((float)amount);
       return true;
    }
}
