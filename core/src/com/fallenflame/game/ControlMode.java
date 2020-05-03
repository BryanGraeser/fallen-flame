package com.fallenflame.game;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ScreenUtils;
import com.fallenflame.game.util.InputBindings;
import com.fallenflame.game.util.JsonAssetManager;
import com.fallenflame.game.util.ScreenListener;

import java.util.Arrays;
import java.util.OptionalInt;
import java.util.stream.IntStream;

public class ControlMode implements Screen, InputProcessor {
    private static final String BACKGROUND_FILE = "textures/control_background.png";
    private final Texture background = new Texture(BACKGROUND_FILE);
    private static final String MODAL = "textures/modal.png";
    private final Texture modal = new Texture(MODAL);
    private TextureRegion screenshotTexture;
    private final GameCanvas canvas;
    private final int[] controlStates;
    private final Rectangle[][] controlRects;
    private int screenWidth;
    private int screenHeight;
    private BitmapFont displayFont;
    private ScreenListener listener;
    private boolean backHover;
    private boolean resetHover;
    private static final int BACK_BTN_WIDTH = 60;
    private static final int BACK_BTN_HEIGHT = 30;
    private static final int BACK_BTN_X = 10;
    private static final int BACK_BTN_Y = 10;
    private static final int RESET_BTN_WIDTH = 190;
    private static final int RESET_BTN_HEIGHT = 30;
    private static final int RESET_BTN_RIGHT = 10;
    private static final int RESET_BTN_Y = 10;

    public ControlMode(GameCanvas canvas)
    {
        this.canvas = canvas;
        controlStates = new int[InputBindings.Control.values().length];
        controlRects = new Rectangle[InputBindings.Control.values().length][2];
        backHover = false;
        resetHover = false;
        Arrays.fill(controlStates, 0);
    }

    public void setScreenListener(ScreenListener listener) { this.listener = listener; }

    @Override
    public void render(float delta) {
        canvas.beginWithoutCamera();
        if (hasScreenshot()) {
            canvas.draw(screenshotTexture, 0, 0);
            canvas.draw(new TextureRegion(modal, screenWidth, screenHeight), 0, 0);
        } else {
            canvas.draw(background, 0, 0);
        }
        displayFont.setColor(Color.WHITE);
        displayFont.getData().setScale(1);
        canvas.drawTextFromCenter("Controls", displayFont, screenWidth / 2 ,screenHeight - 50);
        displayFont.getData().setScale(0.4f);
        int totalControls = InputBindings.Control.values().length;
        int ind = 0;
        for (InputBindings.Control i : InputBindings.Control.values()) {
            float ry = screenHeight - (((ind + 1) / (float) totalControls) * (screenHeight - 160) + 80);
            displayFont.setColor(controlStates[ind] == 1 ? Color.YELLOW :
                    (controlStates[ind] == 2 ? Color.RED : Color.WHITE));
            String str2 = InputBindings.controlToString(i);
            GlyphLayout box2 = new GlyphLayout(displayFont, str2);
            canvas.drawText(str2, displayFont,
                    20 ,ry);
            String str = InputBindings.keyToString(InputBindings.getBindingOf(i));
            GlyphLayout box = new GlyphLayout(displayFont, str);
            float rx = screenWidth - 20 - box.width;
            controlRects[ind] = new Rectangle[]{
                    new Rectangle(20, screenHeight - ry, box2.width, box2.height + 10),
                    new Rectangle(rx, screenHeight - ry, box.width, box.height + 10)
            };
            canvas.drawText(str, displayFont,
                    rx ,ry);
            ind ++;
        }
        displayFont.getData().setScale(0.4f);
        displayFont.setColor(Color.WHITE);
        if (Arrays.stream(controlStates).anyMatch(i -> i == 2)) {
            canvas.drawTextFromCenter("Input new key. Press ESC or click anywhere to cancel.", displayFont,
                    screenWidth / 2, 20);
        } else {
            canvas.drawTextFromCenter("Click on a key to change it.", displayFont,
                    screenWidth / 2, 20);
        }
        displayFont.setColor(backHover ? Color.YELLOW : Color.WHITE);
        displayFont.getData().setScale(0.5f);
        canvas.drawText("Back", displayFont,BACK_BTN_X, screenHeight - BACK_BTN_Y);
        String s = "Reset to Default";
        displayFont.setColor(resetHover ? Color.YELLOW : Color.WHITE);
        canvas.drawText(s, displayFont,
                screenWidth - new GlyphLayout(displayFont, s).width - RESET_BTN_RIGHT,
                screenHeight - RESET_BTN_Y);
        displayFont.getData().setScale(1);
        canvas.end();
    }

    public void screenshot() {
        screenshotTexture = ScreenUtils.getFrameBufferTexture();
    }

    public boolean hasScreenshot() {
        return screenshotTexture != null;
    }

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        OptionalInt ind = IntStream.range(0, controlStates.length)
                .filter(i -> controlStates[i] == 2)
                .findFirst();
        if (!ind.isPresent()) {
            if (keycode == Input.Keys.ESCAPE) {
                listener.exitScreen(this, 0);
                return true;
            }
            return false;
        }
        InputBindings.setBindingOf(InputBindings.Control.values()[ind.getAsInt()], keycode);
        for (int i = 0, j = controlRects.length; i < j; i++) {
            controlStates[i] = 0;
        }
        return true;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (Arrays.stream(controlStates).anyMatch(i -> i == 2)) {
            for (int i = 0, j = controlRects.length; i < j; i++) {
                controlStates[i] = 0;
            }
            return false;
        } else {
            for (int i = 0, j = controlRects.length; i < j; i++) {
                for (Rectangle rec: controlRects[i]) {
                    if (rec.contains(screenX, screenY)) {
                        controlStates[i] = 2;
                        return true;
                    } else {
                        controlStates[i] = 0;
                    }
                }
            }
            if (screenX >= BACK_BTN_X && screenX <= BACK_BTN_X + BACK_BTN_WIDTH &&
                    screenY >= BACK_BTN_Y && screenY <= BACK_BTN_Y + BACK_BTN_HEIGHT) {
                listener.exitScreen(this, 0);
            }
            if (screenX >= screenWidth - RESET_BTN_RIGHT - RESET_BTN_WIDTH &&
                    screenX <= screenWidth - RESET_BTN_RIGHT &&
                    screenY >= RESET_BTN_Y && screenY <= RESET_BTN_Y + RESET_BTN_HEIGHT) {
                InputBindings.reset();
            }
            return true;
        }
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        if (Arrays.stream(controlStates).anyMatch(i -> i == 2)) return false;
        for (int i = 0, j = controlRects.length; i < j; i++) {
            for (Rectangle rec: controlRects[i]) {
                if (rec.contains(screenX, screenY)) {
                    controlStates[i] = 1;
                    return true;
                } else {
                    controlStates[i] = 0;
                }
            }
        }
        backHover = (screenX >= BACK_BTN_X && screenX <= BACK_BTN_X + BACK_BTN_WIDTH &&
                screenY >= BACK_BTN_Y && screenY <= BACK_BTN_Y + BACK_BTN_HEIGHT);
        resetHover = (screenX >= screenWidth - RESET_BTN_RIGHT - RESET_BTN_WIDTH &&
                screenX <= screenWidth - RESET_BTN_RIGHT &&
                screenY >= RESET_BTN_Y && screenY <= RESET_BTN_Y + RESET_BTN_HEIGHT);
        return true;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }

    @Override
    public void show() {
        displayFont = JsonAssetManager.getInstance().getEntry("display", BitmapFont.class);
    }

    @Override
    public void resize(int width, int height) {
        screenWidth = width;
        screenHeight = height;
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {
        screenshotTexture = null;
    }

    @Override
    public void dispose() {

    }
}
