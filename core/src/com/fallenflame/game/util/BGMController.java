package com.fallenflame.game.util;

import com.badlogic.gdx.audio.Sound;

public class BGMController {
    private static Sound activeBGM;

    private static String currentAssetName;

    private static long activeBGMID = -1;

    public static void stopBGM() {
        activeBGMID = -1;
        currentAssetName = null;
        activeBGM.dispose();
        activeBGM = null;
    }

    public static void stopBGMIfPlaying(String assetName) {
        if (assetName.equals(currentAssetName)) stopBGM();
    }

    public static void startBGM(String assetName) {
        if (assetName.equals(currentAssetName)) return;
        if (activeBGM != null) stopBGM();
        currentAssetName = assetName;
        activeBGM = JsonAssetManager.getInstance().getEntry(assetName, Sound.class);
        activeBGMID = -1;
        resumeBGM();
    }

    public static void pauseBGM() {
        if (activeBGMID < 0) return;
        activeBGM.pause(activeBGMID);
    }

    public static void resumeBGM() {
        if (activeBGMID >= 0) return;
        activeBGMID = activeBGM.loop();
    }
}
