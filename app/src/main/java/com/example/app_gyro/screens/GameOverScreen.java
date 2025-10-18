package com.example.app_gyro.screens;

import com.badlogic.gdx.graphics.Color;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.example.app_gyro.GyroOrbitGame;

public class GameOverScreen implements Screen {

    private GyroOrbitGame game;
    private OrthographicCamera camera;
    private FitViewport viewport;
    private GlyphLayout layout;

    private int score;
    private int best;

    public GameOverScreen(GyroOrbitGame game, int score, int best) {
        this.game = game;
        this.score = score;
        this.best = best;
        camera = new OrthographicCamera();
        viewport = new FitViewport(StartScreen.VWIDTH, StartScreen.VHEIGHT, camera);
        layout = new GlyphLayout();
    }

    @Override
    public void show() {}

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.10f, 0.10f, 0.15f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        game.batch.setProjectionMatrix(camera.combined);

        game.batch.begin();
        game.font.setColor(Color.WHITE);

        String title = "GAME OVER";
        layout.setText(game.font, title);
        game.font.draw(game.batch, title,
                (StartScreen.VWIDTH - layout.width) / 2f,
                StartScreen.VHEIGHT - 220f);

        String sc = "Puntaje: " + score + "s   Record: " + best + "s";
        layout.setText(game.font, sc);
        game.font.draw(game.batch, sc,
                (StartScreen.VWIDTH - layout.width) / 2f,
                StartScreen.VHEIGHT - 300f);

        String opt1 = "[ TOCAR PARA REINTENTAR ]";
        layout.setText(game.font, opt1);
        game.font.draw(game.batch, opt1,
                (StartScreen.VWIDTH - layout.width) / 2f,
                360f);

        String opt2 = "[ TOCAR ARRIBA PARA MENU ]";
        layout.setText(game.font, opt2);
        game.font.draw(game.batch, opt2,
                (StartScreen.VWIDTH - layout.width) / 2f,
                300f);

        game.batch.end();

        if (Gdx.input.justTouched()) {
            // Si el toque es en la parte superior, volver al menú; si no, reintentar
            float tx = Gdx.input.getX();
            float ty = Gdx.input.getY();
            com.badlogic.gdx.math.Vector3 v = new com.badlogic.gdx.math.Vector3(tx, ty, 0f);
            viewport.unproject(v);
            if (v.y > StartScreen.VHEIGHT / 2f + 100f) {
                game.setScreen(new StartScreen(game));
            } else {
                game.setScreen(new PlayScreen(game));
            }
        }
    }

    @Override public void resize(int width, int height) { viewport.update(width, height, true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() {}
}
