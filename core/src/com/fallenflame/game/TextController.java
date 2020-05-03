package com.fallenflame.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonValue;
import com.fallenflame.game.util.JsonAssetManager;
import org.w3c.dom.css.Rect;

import java.util.*;

public class TextController {
    private Map<Rectangle, String> texts;
    private String nextMessage = null;
    private BitmapFont displayFont;
    private int screenWidth;
    private int screenHeight;

    public void initialize(JsonValue jsonValue) {
        if (jsonValue == null) return;
        texts = new HashMap<>();
        for (JsonValue textJson : jsonValue) {
            float[] i = textJson.get("rect").asFloatArray();
            Rectangle r = new Rectangle(i[0] - i[2] / 2 , i[1] - i[3] / 2, i[2], i[3]);
            texts.put(r, textJson.get("text").asString());
        }
        displayFont = JsonAssetManager.getInstance().getEntry("display", BitmapFont.class);
        screenWidth = Gdx.graphics.getWidth();
        screenHeight = Gdx.graphics.getHeight();
    }

    public void dispose() {
        texts.clear();
        texts = null;
    }

    public void update(PlayerModel player) {
        if (texts == null) return;
        for (Map.Entry<Rectangle, String> ele : texts.entrySet()) {
            if (ele.getKey().contains(player.getX(), player.getY())) {
                nextMessage = ele.getValue();
                return;
            }
        }
        nextMessage = null;
    }

    public void draw(GameCanvas canvas) {
        if (texts == null || nextMessage == null) return;
        canvas.beginWithoutCamera();
        displayFont.setColor(Color.WHITE);
        displayFont.getData().setScale(.5f);
        List<String> strs = splitLines(nextMessage);
        int i = 0;
        for (String str : strs) {
            canvas.drawTextFromCenter(str, displayFont, screenWidth / 2, 120 - i * 30);
            i ++;
        }
        displayFont.getData().setScale(1);
        canvas.end();
    }

    private List<String> splitLines(String nextMessage) {
        String[] texts = nextMessage.trim().split(" ");
        LinkedList<String> result = new LinkedList<>();
        result.add("");
        int start = 0;
        int end = 0;
        int all = texts.length;
        while (true) {
            end++;
            if (end > all) return result;
            String[] currentLine = Arrays.copyOfRange(texts, start, end);
            String cl = String.join(" ", currentLine);
            if (new GlyphLayout(displayFont, cl).width < screenWidth - 20) {
                result.removeLast();
                result.addLast(cl);
            } else {
                start = end = end - 1;
                result.addLast("");
            }
        }
    }
}
