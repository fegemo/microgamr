package br.microgamr.microgames;

import br.microgamr.graphics.MultiAnimatedSprite;
import br.microgamr.microgames.util.DifficultyCurve;
import br.microgamr.microgames.util.MicroGameStateObserver;
import br.microgamr.microgames.util.TimeoutBehavior;
import br.microgamr.screens.BaseScreen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Timer.Task;
import java.util.HashMap;
import net.dermetfan.gdx.graphics.g2d.AnimatedSprite;

/**
 * MicroGame ExpelTheMonsters. O jogador deve espantar os monstrinhos que tentam
 * se aproximar dos dentes, que ficam no centro, para destruí-los. Com uma
 * escova de dentes, o jogador deve afugentá-los até que o tempo acabe para
 * ganhar.
 *
 * @author Flávio Coutinho <fegemo@cefetmg.br>
 */
public class ExpelTheMonsters extends MicroGame {

    // texturas
    private Texture toothbrushTexture;
    private ToothBrush toothBrush;
    private Texture monsterTexture;
    private Texture toothTexture;
    
    // efeitos sonoros e música de fundo
    private Array<Sound> monsterAppearingSound;
    private Sound toothBreakingSound;
    private Music backgroundMusic;
    
    // objetos do jogo
    private Array<Tartarus> enemies;
    private Array<Tooth> teeth;
    
    // estado do jogo
    private int numberOfBrokenTeeth;

    // variáveis do desafio - variam com a dificuldade do microgame
    private float minimumEnemySpeed;
    private float maximumEnemySpeed;
    private float spawnInterval;
    private int totalTeeth;

    public ExpelTheMonsters(BaseScreen screen,
            MicroGameStateObserver observer, float difficulty) {
        super(screen, observer, difficulty, 10f,
                TimeoutBehavior.WINS_WHEN_MICROGAME_ENDS);
    }

    @Override
    protected void configureDifficultyParameters(float difficulty) {
        // dada a dificuldade solicitada para este microgame 
        // (difficulty, entre 0 e 1), define o valor de alguns parâmetros
        // deste microgame, como a velocidade dos monstros (há uma mínima e
        // outra máxima), o intervalo de surgimento dos monstros e a quantidade
        // total de dentes que devem ser protegidos.
        this.minimumEnemySpeed = DifficultyCurve.LINEAR
                .getCurveValueBetween(difficulty, 120, 220);
        this.maximumEnemySpeed = DifficultyCurve.LINEAR
                .getCurveValueBetween(difficulty, 240, 340);
        this.spawnInterval = DifficultyCurve.LINEAR_NEGATIVE
                .getCurveValueBetween(difficulty, 0.25f, 1.5f);
        this.totalTeeth = (int) Math.ceil(DifficultyCurve.LINEAR
                .getCurveValueBetween(difficulty, 0, 2)) + 1;
    }

    @Override
    protected void onStart() {
        // texturas
        toothbrushTexture = assets.get(
                "expel-the-monsters/toothbrush-spritesheet.png", Texture.class);
        toothBrush = new ToothBrush(toothbrushTexture);
        monsterTexture = assets.get(
                "expel-the-monsters/monster-spritesheet.png", Texture.class);
        toothTexture = assets.get(
                "expel-the-monsters/tooth.png", Texture.class);

        // efeitos sonoros
        monsterAppearingSound = new Array<Sound>(3);
        monsterAppearingSound.addAll(assets.get(
                "expel-the-monsters/appearing1.wav", Sound.class),
                assets.get(
                        "expel-the-monsters/appearing2.wav", Sound.class),
                assets.get(
                        "expel-the-monsters/appearing3.wav", Sound.class));
        toothBreakingSound = assets.get(
                "expel-the-monsters/tooth-breaking.wav", Sound.class);

        // música
        backgroundMusic = assets.get("expel-the-monsters/music.mp3", Music.class);
        backgroundMusic.play();

        // objetos do jogo
        enemies = new Array<Tartarus>();
        teeth = new Array<Tooth>();
        
        // estado inicial do jogo
        numberOfBrokenTeeth = 0;
        
        // cria os dentes que devem ser protegidos e registra o surgimento do
        // primeiro monstro
        initializeTeeth();
        timer.scheduleTask(new Task() {
            @Override
            public void run() {
                spawnEnemy();
            }

        }, 0, this.spawnInterval);
    }

    @Override
    protected void onEnd() {
        backgroundMusic.stop();
    }

    @Override
    public void onHandlePlayingInput() {
        // atualiza a posição do alvo de acordo com o mouse
        Vector3 click = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        viewport.unproject(click);
        toothBrush.setCenter(click.x, click.y);

        // verifica se a cabeça da escova está próxima dos tártarus
        for (Tartarus tart : this.enemies) {
            float distance = toothBrush.getHeadDistanceTo(
                    tart.getX(), tart.getY());
            if (distance <= 30) {
                tart.startFleeing(toothBrush.getHeadPosition());
            }
        }
    }

    @Override
    public void onUpdate(float dt) {
        // atualiza a escova (quadro da animação)
        toothBrush.update(dt);

        // atualiza os inimigos (quadro de animação + colisão com dentes)
        for (int i = 0; i < this.enemies.size; i++) {
            Tartarus tart = this.enemies.get(i);
            tart.update(dt);

            // verifica se este inimigo está colidindo com algum dente
            for (Tooth tooth : this.teeth) {
                if (tart.getBoundingRectangle()
                        .overlaps(tooth.getBoundingRectangle())) {
                    toothWasHurt(tooth, tart);
                }
            }
        }
    }

    @Override
    public void onDrawGame() {
        // desenha os dentes, os inimigos e a escova
        for (Tooth tooth : this.teeth) {
            tooth.draw(batch);
        }
        for (Tartarus tart : this.enemies) {
            tart.draw(batch);
        }
        toothBrush.draw(batch);
    }

    @Override
    public String getInstructions() {
        return "Espante os monstros";
    }

    @Override
    public boolean shouldHideMousePointer() {
        return true;
    }

    private void initializeTeeth() {
        // posiciona e cria os dentes
        TextureRegion[][] frames = TextureRegion.split(toothTexture,
                Tooth.FRAME_WIDTH, Tooth.FRAME_HEIGHT);

        switch (this.totalTeeth) {
            case 1: // coloca o único dente no centro da tela
            {
                Tooth tooth = new Tooth(
                        frames[0][0],
                        frames[0][1],
                        frames[0][2],
                        2);
                tooth.setCenter(
                        viewport.getWorldWidth() / 2f,
                        viewport.getWorldHeight() / 2f);
                this.teeth.add(tooth);
            }
            break;
            case 2:
                // coloca os dois dentes verticalmente no centro, mas o
                // primeiro em 25% da largura e o segundo em 75%
                for (int i = 0; i < this.totalTeeth; i++) {
                    Tooth tooth = new Tooth(
                            frames[0][0],
                            frames[0][1],
                            frames[0][2],
                            2);
                    tooth.setCenter(
                            // 3/7 e 4/7 da largura da tela
                            viewport.getWorldWidth() / 7f * (i + 3),
                            viewport.getWorldHeight() / 2f);
                    this.teeth.add(tooth);
                }
                break;
            case 3:
            default:
                // coloca os 3 ou mais dentes em um círculo ao redor do centro
                for (int i = 0; i < this.totalTeeth; i++) {
                    float angle = (360f / this.totalTeeth) * i;
                    final float radius = 90f;
                    Tooth tooth = new Tooth(
                            frames[0][0],
                            frames[0][1],
                            frames[0][2],
                            2);
                    tooth.setCenter(
                            viewport.getWorldWidth() / 2
                            + MathUtils.cosDeg(angle) * radius,
                            viewport.getWorldHeight() / 2
                            + MathUtils.sinDeg(angle) * radius);
                    this.teeth.add(tooth);
                }
                break;
        }
    }

    private void spawnEnemy() {
        // cria um inimigo em uma posição aleatória fora da tela e com um
        // alvo que é um dos dentes
        Vector2 goalCenter = new Vector2();
        Vector2 tartarusGoal = this.teeth.random()
                .getBoundingRectangle()
                .getCenter(goalCenter);
        Vector2 tartarusPosition = new Vector2();
        boolean appearFromSides = MathUtils.randomBoolean();
        if (appearFromSides) {
            tartarusPosition.x = MathUtils.randomBoolean()
                    ? -Tartarus.FRAME_WIDTH
                    : viewport.getWorldWidth();
            tartarusPosition.y = MathUtils.random(
                    -Tartarus.FRAME_HEIGHT,
                    viewport.getWorldHeight());
        } else {
            tartarusPosition.y = MathUtils.randomBoolean()
                    ? -Tartarus.FRAME_HEIGHT
                    : viewport.getWorldHeight();
            tartarusPosition.x = MathUtils.random(
                    -Tartarus.FRAME_WIDTH,
                    viewport.getWorldWidth());
        }
        Vector2 tartarusSpeed = tartarusGoal
                .sub(tartarusPosition)
                .nor()
                .scl(this.minimumEnemySpeed);

        Tartarus enemy = new Tartarus(monsterTexture);
        enemy.setPosition(tartarusPosition.x, tartarusPosition.y);
        enemy.setSpeed(tartarusSpeed);
        enemies.add(enemy);

        // toca um efeito sonoro
        Sound sound = monsterAppearingSound.random();
        long id = sound.play(0.5f);
        sound.setPan(id, tartarusPosition.x < viewport.getWorldWidth()
                ? -1 : 1, 1);
    }

    private void toothWasHurt(Tooth tooth, Tartarus enemy) {
        enemies.removeValue(enemy, false);
        numberOfBrokenTeeth += tooth.wasHurt() ? 1 : 0;

        if (numberOfBrokenTeeth >= totalTeeth) {
            super.challengeFailed();
        }
        toothBreakingSound.play();
    }

    /**
     * A escova controlada pelo jogador.
     */
    class ToothBrush extends AnimatedSprite {

        // largura e altura de cada quadro da spritesheet da escova
        static final int FRAME_WIDTH = 120;
        static final int FRAME_HEIGHT = 280;

        ToothBrush(final Texture toothbrushTexture) {
            super(new Animation(0.1f, new Array<TextureRegion>() {
                {
                    TextureRegion[][] frames = TextureRegion.split(
                            toothbrushTexture, FRAME_WIDTH, FRAME_HEIGHT);
                    super.addAll(new TextureRegion[]{
                        frames[0][0],
                        frames[0][1],
                        frames[0][2],
                        frames[0][3]
                    });
                }
            }));
            super.getAnimation().setPlayMode(Animation.PlayMode.LOOP);
            super.setAutoUpdate(false);
        }

        Vector2 getHeadPosition() {
            return new Vector2(
                    this.getX() + this.getWidth() * 0.5f,
                    this.getY() + this.getHeight() * 0.8f);
        }

        float getHeadDistanceTo(float enemyX, float enemyY) {
            return getHeadPosition().dst(enemyX, enemyY);
        }
    }

    /**
     * Um monstrinho.
     */
    class Tartarus extends MultiAnimatedSprite {

        private Vector2 speed;
        private boolean isFleeing = false;

        // largura e altura de cada quadro da spritesheet
        static final int FRAME_WIDTH = 28;
        static final int FRAME_HEIGHT = 36;

        public Tartarus(final Texture tartarusSpritesheet) {
            super(new HashMap<String, Animation>() {
                {
                    TextureRegion[][] frames = TextureRegion
                            .split(tartarusSpritesheet,
                                    FRAME_WIDTH, FRAME_HEIGHT);
                    Animation walking = new Animation(0.2f,
                            frames[0][0],
                            frames[0][1],
                            frames[0][2],
                            frames[0][1]);
                    walking.setPlayMode(Animation.PlayMode.LOOP);
                    put("walking", walking);
                }
            }, "walking");
        }

        @Override
        public void update(float dt) {
            super.update(dt);
            super.setPosition(super.getX() + this.speed.x * dt,
                    super.getY() + this.speed.y * dt);
        }

        public Vector2 getSpeed() {
            return speed;
        }

        public void setSpeed(Vector2 speed) {
            this.speed = speed;
        }

        public void startFleeing(Vector2 from) {
            if (this.isFleeing) {
                return;
            }
            this.isFleeing = true;
            Vector2 position = new Vector2(super.getX(), super.getY());
            this.speed = position.sub(from).nor().scl(maximumEnemySpeed);
            this.setColor(Color.YELLOW);
        }
    }

    /**
     * Um dente que deve ser defendido.
     */
    class Tooth extends Sprite {

        private final TextureRegion hurt;
        private final TextureRegion broken;
        private int lives = 2;

        // largura/altura de cada quadro da spritesheet do dente
        static final int FRAME_WIDTH = 64;
        static final int FRAME_HEIGHT = 64;

        public Tooth(TextureRegion textureOk, TextureRegion textureHurt,
                TextureRegion textureBroken, int lives) {
            super(textureOk);
            this.hurt = textureHurt;
            this.broken = textureBroken;
            this.lives = lives;
        }

        public boolean wasHurt() {
            lives--;
            super.setRegion(lives > 0 ? hurt : broken);
            return lives == 0;
        }
    }
}
