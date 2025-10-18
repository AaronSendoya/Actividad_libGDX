package com.example.app_gyro;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences; // <-- libGDX, NO java.util.prefs

public class Assets {

    private static final String PREFS = "gyro_orbit_prefs";
    private static final String KEY_HIGH = "high_score";

    public static Preferences prefs() {
        return Gdx.app.getPreferences(PREFS);
    }

    public static int getHighScore() {
        return prefs().getInteger(KEY_HIGH, 0);
    }

    public static void setHighScore(int value) {
        Preferences p = prefs();
        p.putInteger(KEY_HIGH, value);
        p.flush();
    }
}
