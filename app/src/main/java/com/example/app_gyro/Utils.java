package com.example.app_gyro;

public class Utils {

    public static final float TWO_PI = 6.2831855f;

    public static float clamp(float v, float min, float max) {
        if (v < min) return min;
        if (v > max) return max;
        return v;
    }

    public static float normalizeAngle0ToTwoPi(float a) {
        while (a >= TWO_PI) a -= TWO_PI;
        while (a < 0f) a += TWO_PI;
        return a;
    }

    public static float dst2(float x1, float y1, float x2, float y2) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        return dx * dx + dy * dy;
    }
}
