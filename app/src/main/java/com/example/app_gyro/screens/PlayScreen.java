package com.example.app_gyro.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.example.app_gyro.Assets;
import com.example.app_gyro.GyroOrbitGame;
import com.example.app_gyro.Utils;

public class PlayScreen implements Screen {

    private GyroOrbitGame game;

    private OrthographicCamera camera;
    private FitViewport viewport;
    private ShapeRenderer shape;
    private GlyphLayout layout;

    private float cx = StartScreen.VWIDTH / 2f;
    private float cy = StartScreen.VHEIGHT / 2f;
    private float radius = 180f;

    // Franja segura (anillo)
    private float ringInner = 160f;
    private float ringOuter = 200f;
    private float ringShrinkPerStep = 2f; // reducción a lo largo de la dificultad

    // Jugador
    private float angle = 0f;          // ángulo actual de la órbita
    private float gzSmooth = 0f;       // suavizado EMA
    private float alpha = 0.18f;       // coeficiente suavizado
    private float angleSpeedLimit = 4f;

    private boolean gyroOk = false;
    private float angleOffset = 0f;    // para calibración

    // Asteroides
    private static class Asteroid {
        float x, y;
        float vx, vy;
        float r;
    }
    private Array<Asteroid> asteroids;
    private float asteroidSpeed = 120f;
    private float asteroidSpawnTimer = 0f;
    private float asteroidSpawnInterval = 2.0f;

    // Puntaje
    private float timeAlive = 0f;
    private int best = 0;

    // Dificultad
    private float diffTimer = 0f;
    private float diffStep = 15f;

    // Calibración: botón simple en esquina
    private float btnX = StartScreen.VWIDTH - 160f;
    private float btnY = StartScreen.VHEIGHT - 80f;
    private float btnW = 120f;
    private float btnH = 50f;

    public PlayScreen(GyroOrbitGame game) {
        this.game = game;
        camera = new OrthographicCamera();
        viewport = new FitViewport(StartScreen.VWIDTH, StartScreen.VHEIGHT, camera);
        shape = new ShapeRenderer();
        layout = new GlyphLayout();
        asteroids = new Array<Asteroid>();

        gyroOk = Gdx.input.isPeripheralAvailable(Input.Peripheral.Gyroscope);
        if (gyroOk) {
            Gdx.app.log("Gyro", "Giroscopio DISPONIBLE");
        } else {
            Gdx.app.log("Gyro", "Giroscopio NO disponible: usando acelerometro");
        }

        best = Assets.getHighScore();
    }

    @Override
    public void show() {
        // Spawnea algunos asteroides iniciales
        spawnAsteroid();
        spawnAsteroid();
        spawnAsteroid();
    }

    @Override
    public void render(float delta) {
        update(delta);
        draw();
    }

    private void update(float delta) {
        // Lectura sensor
        float gz;
        if (gyroOk) {
            gz = Gdx.input.getGyroscopeZ();      // rad/s
            gz = Utils.clamp(gz, -10f, 10f);
            gzSmooth = alpha * gz + (1f - alpha) * gzSmooth;
            gzSmooth = Utils.clamp(gzSmooth, -angleSpeedLimit, angleSpeedLimit);
            angle += gzSmooth * delta;
        } else {
            float ax = Gdx.input.getAccelerometerX();
            // factor simple para traducir inclinación a rotación
            angle += (-ax * 0.8f) * delta;
        }

        // Normalizar y aplicar offset de calibración
        angle = Utils.normalizeAngle0ToTwoPi(angle - angleOffset);

        // Puntaje
        timeAlive += delta;
        diffTimer += delta;

        // Dificultad cada diffStep
        if (diffTimer >= diffStep) {
            diffTimer = 0f;
            // aumentar velocidad de asteroides y apretar anillo
            asteroidSpeed += 15f;
            ringInner = MathUtils.clamp(ringInner + ringShrinkPerStep, 140f, 220f);
            ringOuter = MathUtils.clamp(ringOuter - ringShrinkPerStep, 180f, 240f);
            if (asteroidSpawnInterval > 0.9f) {
                asteroidSpawnInterval -= 0.1f;
            }
        }

        asteroidSpawnTimer += delta;
        if (asteroidSpawnTimer >= asteroidSpawnInterval && asteroids.size < 6) {
            asteroidSpawnTimer = 0f;
            spawnAsteroid();
        }

        // Actualizar asteroides
        int i;
        for (i = 0; i < asteroids.size; i++) {
            Asteroid a = asteroids.get(i);
            a.x += a.vx * delta;
            a.y += a.vy * delta;

            // Rebote simple en bordes de pantalla
            if (a.x < 20f || a.x > StartScreen.VWIDTH - 20f) a.vx = -a.vx;
            if (a.y < 20f || a.y > StartScreen.VHEIGHT - 20f) a.vy = -a.vy;
        }

        // Posición del jugador en la órbita
        float px = cx + MathUtils.cos(angle) * radius;
        float py = cy + MathUtils.sin(angle) * radius;

        // Validar franja segura: el jugador debe estar entre ringInner y ringOuter
        float distFromCenter = (float)Math.sqrt(Utils.dst2(px, py, cx, cy));
        boolean outside = distFromCenter < ringInner || distFromCenter > ringOuter;

        // Colisión con asteroides (círculo-círculo): jugador r=12
        boolean hit = false;
        for (i = 0; i < asteroids.size; i++) {
            Asteroid a = asteroids.get(i);
            float radSum = 12f + a.r;
            float d2 = Utils.dst2(px, py, a.x, a.y);
            if (d2 < radSum * radSum) {
                hit = true;
                break;
            }
        }

        if (outside || hit) {
            int score = (int) timeAlive;
            if (score > best) {
                Assets.setHighScore(score);
                best = score;
            }
            game.setScreen(new GameOverScreen(game, score, best));
        }

        // Input: tocar botón "Calibrar"
        if (Gdx.input.justTouched()) {
            float tx = Gdx.input.getX();
            float ty = Gdx.input.getY();
            // Convertir a coords de juego
            viewport.unproject(tmpVec.set(tx, ty, 0f));
            float gx = tmpVec.x;
            float gy = tmpVec.y;
            if (gx >= btnX && gx <= btnX + btnW && gy >= btnY && gy <= btnY + btnH) {
                // Calibrar: fijar offset para que el ángulo actual pase a 0
                angleOffset = angle;
                Gdx.app.log("Gyro", "Calibrado. angleOffset=" + angleOffset);
            }
        }
    }

    // Vector temporal para unproject
    private com.badlogic.gdx.math.Vector3 tmpVec = new com.badlogic.gdx.math.Vector3();

    private void draw() {
        Gdx.gl.glClearColor(0.062f, 0.062f, 0.094f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();

        // Dibujo con ShapeRenderer
        shape.setProjectionMatrix(camera.combined);
        shape.begin(ShapeRenderer.ShapeType.Line);

        // Franja segura (dos círculos)
        shape.setColor(0.40f, 0.77f, 0.42f, 1f); // verde
        shape.circle(cx, cy, ringInner);
        shape.circle(cx, cy, ringOuter);

        // Asteroides
        shape.setColor(0.90f, 0.22f, 0.21f, 1f); // rojo
        int i;
        for (i = 0; i < asteroids.size; i++) {
            Asteroid a = asteroids.get(i);
            shape.circle(a.x, a.y, a.r);
        }

        // Jugador (orbe) como punto sólido: cambiar a Fill temporalmente
        shape.end();

        shape.begin(ShapeRenderer.ShapeType.Filled);
        float px = cx + MathUtils.cos(angle) * radius;
        float py = cy + MathUtils.sin(angle) * radius;
        shape.setColor(Color.WHITE);
        shape.circle(px, py, 12f);

        // Botón calibrar
        shape.setColor(0.25f, 0.25f, 0.30f, 1f);
        shape.rect(btnX, btnY, btnW, btnH);
        shape.end();

        // Texto
        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();
        game.font.setColor(Color.WHITE);
        String t = "Tiempo: " + (int) timeAlive + "s   Record: " + best + "s";
        layout.setText(game.font, t);
        game.font.draw(game.batch, t, 20f, StartScreen.VHEIGHT - 30f);

        String c = "Calibrar";
        layout.setText(game.font, c);
        game.font.draw(game.batch, c, btnX + (btnW - layout.width) / 2f, btnY + 33f);
        game.batch.end();
    }

    private void spawnAsteroid() {
        Asteroid a = new Asteroid();
        a.r = MathUtils.random(10f, 22f);

        // Posición aleatoria fuera del anillo, para cruzar la zona
        // Elige un borde y genera dirección hacia el centro
        int edge = MathUtils.random(0, 3);
        if (edge == 0) { // arriba
            a.x = MathUtils.random(20f, StartScreen.VWIDTH - 20f);
            a.y = StartScreen.VHEIGHT - 30f;
        } else if (edge == 1) { // abajo
            a.x = MathUtils.random(20f, StartScreen.VWIDTH - 20f);
            a.y = 30f;
        } else if (edge == 2) { // izquierda
            a.x = 20f;
            a.y = MathUtils.random(20f, StartScreen.VHEIGHT - 20f);
        } else { // derecha
            a.x = StartScreen.VWIDTH - 20f;
            a.y = MathUtils.random(20f, StartScreen.VHEIGHT - 20f);
        }

        // Velocidad hacia el centro con leve ruido
        float dx = cx - a.x;
        float dy = cy - a.y;
        float len = (float)Math.sqrt(dx * dx + dy * dy);
        if (len == 0) len = 1f;
        dx /= len;
        dy /= len;

        float spd = asteroidSpeed + MathUtils.random(-40f, 40f);
        a.vx = dx * spd;
        a.vy = dy * spd;
        asteroids.add(a);
    }

    @Override public void resize(int width, int height) { viewport.update(width, height, true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        if (shape != null) shape.dispose();
    }
}
