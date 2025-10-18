package com.example.app_gyro.screens;

// ❌ quita este import: import android.graphics.Color;
import com.badlogic.gdx.graphics.Color; // ✅ usa el de libGDX

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.example.app_gyro.GyroOrbitGame;

public class StartScreen implements Screen {

    private GyroOrbitGame game;
    private OrthographicCamera camera;
    private FitViewport viewport;
    private GlyphLayout layout;

    public static final int VWIDTH = 720;
    public static final int VHEIGHT = 1280;

    public StartScreen(GyroOrbitGame game) {
        this.game = game;
        camera = new OrthographicCamera();
        viewport = new FitViewport(VWIDTH, VHEIGHT, camera);
        layout = new GlyphLayout();
    }

    @Override
    public void show() {
        Gdx.app.log("StartScreen", "Bienvenido a Gyro Orbit");
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.062f, 0.062f, 0.094f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        game.batch.setProjectionMatrix(camera.combined);

        game.batch.begin();
        game.font.setColor(Color.WHITE);
        layout.setText(game.font, "Gyro Orbit");
        game.font.draw(game.batch, "Gyro Orbit",
                (VWIDTH - layout.width) / 2f,
                VHEIGHT - 200f);

        String help = "Gira el telefono para ajustar tu orbita.\n" +
                "Mantente en la franja segura y evita los asteroides.";
        layout.setText(game.font, help);
        game.font.draw(game.batch, help,
                (VWIDTH - layout.width) / 2f,
                VHEIGHT - 270f);

        String btn = "[ TOCAR PARA INICIAR ]";
        layout.setText(game.font, btn);
        game.font.draw(game.batch, btn,
                (VWIDTH - layout.width) / 2f,
                300f);
        game.batch.end();

        if (Gdx.input.justTouched()) {
            game.setScreen(new PlayScreen(game));
        }
    }

    @Override public void resize(int width, int height) { viewport.update(width, height, true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() {}
}
