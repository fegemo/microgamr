package br.microgamr.microgames;

import br.microgamr.microgames.util.DifficultyCurve;
import br.microgamr.microgames.util.MicroGameStateObserver;
import br.microgamr.microgames.util.TimeoutBehavior;
import br.microgamr.screens.BaseScreen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Timer.Task;

/**
 * MicroGame ShootTheMonsters. Monstros aparecem grandes e vão diminuindo de
 * tamanho. Se o jogador não destruí-los no tempo disponível, ele perde.
 * 
 * @author Flávio Coutinho <fegemo@cefetmg.br>
 */
public class ShootTheMonsters extends MicroGame {

    // texturas
    private Texture monsterTexture;
    private Texture targetTexture;
    
    // efeitos sonoros e música de fundo
    private Sound monsterAppearingSound;
    private Sound monsterDyingSound;
    private Music backgroundMusic;
    
    // objetos do jogo
    private Array<Sprite> enemies;
    private Sprite target;

    // estado do jogo
    private int enemiesKilled;
    private int spawnedEnemies;

    // variáveis que determinam a dificuldade do jogo
    private float initialEnemyScale;
    private float minimumEnemyScale;
    private int totalEnemies;
    private float spawnInterval;

    public ShootTheMonsters(BaseScreen screen,
            MicroGameStateObserver observer, float difficulty) {
        super(screen, observer, difficulty, 10f,
                TimeoutBehavior.FAILS_WHEN_MICROGAME_ENDS);
    }

    @Override
    protected void onStart() {
        // texturas
        monsterTexture = assets.get(
                "shoot-the-monsters/monster.png", Texture.class);
        targetTexture = assets.get(
                "shoot-the-monsters/target.png", Texture.class);
        // efeitos sonoros
        monsterAppearingSound = assets.get(
                "shoot-the-monsters/monster1.mp3", Sound.class);
        monsterDyingSound = assets.get(
                "shoot-the-monsters/monster2.mp3", Sound.class);
        
        // música de fundo
        backgroundMusic = assets.get("shoot-the-monsters/music.mp3", Music.class);
        backgroundMusic.play();
        
        // objeto do jogo
        target = new Sprite(targetTexture);
        target.setOriginCenter();
        enemies = new Array<Sprite>();
        
        // estado inicial do microgame
        enemiesKilled = 0;
        spawnedEnemies = 0;
        
        // registra o surgimento do primeiro monstro
        scheduleEnemySpawn();
    }

    @Override
    protected void onEnd() {
        backgroundMusic.stop();
    }
    

    @Override
    protected void configureDifficultyParameters(float difficulty) {
        // a dificuldade deste microgame é dada pelo tamanho inicial e o final
        // dos monstros, do tempo que eles demoram para aparecer e da quantidade
        // total de monstros que devem ser abatidos
        this.initialEnemyScale = DifficultyCurve.LINEAR
                .getCurveValueBetween(difficulty, 1.15f, 0.8f);
        this.minimumEnemyScale = DifficultyCurve.LINEAR_NEGATIVE
                .getCurveValueBetween(difficulty, 0.15f, 0.4f);
        this.spawnInterval = DifficultyCurve.S_NEGATIVE
                .getCurveValueBetween(difficulty, 0.5f, 1.5f);
        this.totalEnemies = (int) Math.ceil(maxDuration / spawnInterval) - 3;
    }

    @Override
    public void onHandlePlayingInput() {
        // atualiza a posição do alvo de acordo com o mouse
        Vector3 click = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        viewport.unproject(click);
        this.target.setPosition(click.x - this.target.getWidth() / 2,
                click.y - this.target.getHeight() / 2);

        // verifica se matou um inimigo
        if (Gdx.input.justTouched()) {
            // itera no array de inimigos
            for (int i = 0; i < enemies.size; i++) {
                Sprite sprite = enemies.get(i);
                // se há interseção entre o retângulo da sprite e do alvo,
                // o tiro acertou
                if (sprite.getBoundingRectangle().overlaps(
                        target.getBoundingRectangle())) {
                    // contabiliza um inimigo morto
                    this.enemiesKilled++;
                    // remove o inimigo do array
                    this.enemies.removeValue(sprite, true);
                    monsterDyingSound.play();
                    // se tiver matado todos os inimigos, o desafio
                    // está resolvido
                    if (this.enemiesKilled >= this.totalEnemies) {
                        super.challengeSolved();
                    }

                    // pára de iterar, porque senão o tiro pode pegar em mais
                    // de um inimigo
                    break;
                }
            }
        }
    }

    @Override
    public void onUpdate(float dt) {

        // vai diminuindo o tamanho das cáries existentes
        for (int i = 0; i < enemies.size; i++) {
            Sprite sprite = enemies.get(i);
            // diminui só até x% do tamanho da imagem
            if (sprite.getScaleX() > minimumEnemyScale) {
                sprite.setScale(sprite.getScaleX() - 0.3f * dt);
            }
        }
    }

    @Override
    public String getInstructions() {
        return "Acerte os monstros";
    }

    @Override
    public void onDrawGame() {

        for (int i = 0; i < enemies.size; i++) {
            Sprite sprite = enemies.get(i);
            sprite.draw(batch);
        }
        target.draw(batch);
    }

    @Override
    public boolean shouldHideMousePointer() {
        return true;
    }
    
    
    /**
     * Registra o surgimento de um monstro para daqui um tempo.
     */
    private void scheduleEnemySpawn() {
        Task t = new Task() {
            @Override
            public void run() {
                spawnEnemy();
                if (++spawnedEnemies < totalEnemies) {
                    scheduleEnemySpawn();
                }
            }
        };
        // spawnInterval * 15% para mais ou para menos
        float nextSpawnMillis = this.spawnInterval
                * (rand.nextFloat() / 3 + 0.15f);
        timer.scheduleTask(t, nextSpawnMillis);
    }

    /**
     * Faz um monstro surgir em uma posição aleatória da tela
     */
    private void spawnEnemy() {
        // pega x e y aleatórios entre 0 e 1
        Vector2 position = new Vector2(rand.nextFloat(), rand.nextFloat());
        // multiplica x e y pela largura e altura da tela (- larg/alt do monstro)
        position.scl(
                viewport.getWorldWidth() - monsterTexture.getWidth()
                * initialEnemyScale,
                viewport.getWorldHeight()
                - monsterTexture.getHeight() * initialEnemyScale);

        Sprite enemy = new Sprite(monsterTexture);
        enemy.setPosition(position.x, position.y);
        enemy.setScale(initialEnemyScale);
        enemies.add(enemy);

        // toca um efeito sonoro
        monsterAppearingSound.play(0.5f);
    }

}
