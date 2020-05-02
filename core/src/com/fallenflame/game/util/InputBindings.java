package com.fallenflame.game.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;

import java.util.HashMap;
import java.util.Map;

public class InputBindings {
    private static final Preferences prefs = Gdx.app.getPreferences("fallen_flame_key_binding");
    public enum Control {
        GO_UP,
        GO_DOWN,
        GO_LEFT,
        GO_RIGHT,
        RESET_LEVEL,
        SNEAKING,
        SPRINTING,
        INCREASE_LIGHT,
        DECREASE_LIGHT,
    }
    private static final Map<Control, Integer> bindings;
    static {
        bindings = new HashMap<>();
        reset(false);
        readFromPrefs();
    }
    public static int getBindingOf(Control c) {
        return bindings.get(c);
    }
    private static boolean allowed(int i) {
        switch (i) {
            case Input.Keys.A:
            case Input.Keys.B:
            case Input.Keys.C:
            case Input.Keys.D:
            case Input.Keys.E:
            case Input.Keys.F:
            case Input.Keys.G:
            case Input.Keys.H:
            case Input.Keys.I:
            case Input.Keys.J:
            case Input.Keys.K:
            case Input.Keys.L:
            case Input.Keys.M:
            case Input.Keys.N:
            case Input.Keys.O:
            case Input.Keys.P:
            case Input.Keys.Q:
            case Input.Keys.R:
            case Input.Keys.S:
            case Input.Keys.T:
            case Input.Keys.U:
            case Input.Keys.V:
            case Input.Keys.W:
            case Input.Keys.X:
            case Input.Keys.Y:
            case Input.Keys.Z:
            case Input.Keys.ALT_LEFT:
            case Input.Keys.ALT_RIGHT:
            case Input.Keys.SHIFT_LEFT:
            case Input.Keys.SHIFT_RIGHT:
            case Input.Keys.CONTROL_LEFT:
            case Input.Keys.CONTROL_RIGHT:
            case Input.Keys.COMMA:
            case Input.Keys.PERIOD:
            case Input.Keys.SLASH:
            case Input.Keys.BACKSLASH:
            case Input.Keys.LEFT_BRACKET:
            case Input.Keys.RIGHT_BRACKET:
            case Input.Keys.COLON:
            case Input.Keys.UP:
            case Input.Keys.LEFT:
            case Input.Keys.DOWN:
            case Input.Keys.RIGHT:
                return true;
            default:
                return false;
        }
    }
    private static void readFromPrefs() {
        for (Control c : Control.values()) {
            String id = controlToID(c);
            if (prefs.contains(id)) {
                setBindingOfWithoutCheck(c, prefs.getInteger(id));
            }
        }
    }
    private static void switchControlIfNeeded(Control c, int i) {
        Control con = null;
        for (Map.Entry<Control, Integer> c2 : bindings.entrySet()) {
            if (c == c2.getKey()) continue;
            if (i == c2.getValue()) {
                con = c2.getKey();
                break;
            }
        }
        if (con != null) {
            bindings.put(con, getBindingOf(c));
        }
    }
    public static void reset(boolean resetPrefs) {
        bindings.put(Control.GO_UP, Input.Keys.W);
        bindings.put(Control.GO_DOWN, Input.Keys.S);
        bindings.put(Control.GO_LEFT, Input.Keys.A);
        bindings.put(Control.GO_RIGHT, Input.Keys.D);
        bindings.put(Control.RESET_LEVEL, Input.Keys.R);
        bindings.put(Control.SNEAKING, Input.Keys.CONTROL_LEFT);
        bindings.put(Control.SPRINTING, Input.Keys.SHIFT_LEFT);
        bindings.put(Control.INCREASE_LIGHT, Input.Keys.PERIOD);
        bindings.put(Control.DECREASE_LIGHT, Input.Keys.COMMA);
        if (resetPrefs) {
            prefs.clear();
            prefs.flush();
        }
    }
    public static void reset() {
        reset(true);
    }
    public static String keyToString(int k) {
        switch(k) {
            case Input.Keys.COMMA: return "Comma";
            case Input.Keys.PERIOD: return "Period";
            case Input.Keys.SLASH: return "Slash";
            case Input.Keys.BACKSLASH: return "Backslash";
            case Input.Keys.LEFT_BRACKET: return "L-Bracket";
            case Input.Keys.RIGHT_BRACKET: return "R-Bracket";
            case Input.Keys.COLON: return "Colon";
            default:
                return Input.Keys.toString(k);
        }
    }
    private static void setBindingOfWithoutCheck(Control c, int i) {
        if (!allowed(i)) return;
        bindings.put(c, i);
    }
    public static void setBindingOf(Control c, int i) {
        if (!allowed(i)) return;
        switchControlIfNeeded(c, i);
        bindings.put(c, i);
        prefs.putInteger(controlToID(c), i);
        prefs.flush();
    }
    public static String controlToID(Control c) {
        switch (c) {
            case GO_UP: return "up";
            case GO_DOWN: return "down";
            case GO_LEFT: return "left";
            case GO_RIGHT: return "right";
            case RESET_LEVEL: return "reset";
            case SNEAKING: return "sneak";
            case SPRINTING: return "sprint";
            case INCREASE_LIGHT: return "incLightRad";
            case DECREASE_LIGHT: return "decLightRad";
        }
        return null;
    }
    public static Control idToControl(String c) {
        switch (c) {
            case "up": return Control.GO_UP;
            case "down": return Control.GO_DOWN;
            case "left": return Control.GO_LEFT;
            case "right": return Control.GO_RIGHT;
            case "reset": return Control.RESET_LEVEL;
            case "sneak": return Control.SNEAKING;
            case "sprint": return Control.SPRINTING;
            case "incLightrad": return Control.INCREASE_LIGHT;
            case "decLightRad": return Control.DECREASE_LIGHT;
        }
        return null;
    }
    public static String controlToString(Control c) {
        switch (c) {
            case GO_UP: return "Go up";
            case GO_DOWN: return "Go down";
            case GO_LEFT: return "Go left";
            case GO_RIGHT: return "Go right";
            case RESET_LEVEL: return "Reset level";
            case SNEAKING: return "Sneaking";
            case SPRINTING: return "Sprinting";
            case INCREASE_LIGHT: return "Increase light radius (alternative)";
            case DECREASE_LIGHT: return "Decrease light radius (alternative)";
        }
        return null;
    }
}
