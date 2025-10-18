package com.example.app_gyro;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.example.app_gyro.screens.StartScreen;

public class GyroOrbitGame extends Game {

    public SpriteBatch batch;
    public BitmapFont font;

    @Override
    public void create() {
        batch = new SpriteBatch();
        font = new BitmapFont(); // fuente por defecto de libGDX
        setScreen(new StartScreen(this));
    }

    @Override
    public void dispose() {
        if (font != null) font.dispose();
        if (batch != null) batch.dispose();
        super.dispose();
    }
}
