package br.microgamr.microgames;

import br.microgamr.Config;
import br.microgamr.microgames.util.MicroGameState;
import br.microgamr.microgames.util.MicroGameStateObserver;
import br.microgamr.microgames.util.TimeoutBehavior;
import br.microgamr.screens.BaseScreen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;
import com.badlogic.gdx.utils.viewport.Viewport;
import java.util.Random;

/**
 * Um microgame. Todos os microgames devem herdar desta classe abstrata e
 * implementar alguns métodos. Os principais métodos e suas funções são:
 *
 * <ul>
 * <li><code>configureDifficultyParameters(float difficulty)</code>: configurar
 * os parâmetros específicos deste microgame de acordo com a dificuldade
 * solicitada.</li>
 * <li><code>onStart()</code>: criar os objetos que compõem a cena do jogo.</li>
 * <li><code>onEnd()</code>: interromper coisas em andamento, como música de
 * fundo.</li>
 * <li><code>onDrawGame()</code>: desenhar o jogo.</li>
 * <li><code>onUpdate(float dt)</code>: atualizar a lógica do jogo.</li>
 * <li><code>onHandlePlayingInput()</code>: verificar se comandos foram emitidos
 * pelo jogador.</li>
 * </ul>
 *
 * @author fegemo <fegemo@cefetmg.br>
 */
public abstract class MicroGame {

    protected final AssetManager assets;
    protected final Viewport viewport;
    protected final SpriteBatch batch;
    protected long remainingTime;
    protected float maxDuration;
    private float timeSpentOnInstructions;
    private float timeSpentPlaying;
    private MicroGameState state;
    protected Random rand;
    protected final Timer timer;
    private boolean isPaused;

    private boolean challengeSolved;
    private MicroGameStateObserver stateObserver;
    private long timeWhenPausedLastTime;
    private InputProcessor microGameInputProcessor;
    private float difficulty;

    /**
     * Instancia um microgame.
     *
     * @param screen a tela dona deste microgame.
     * @param observer alguém interessado no estado deste microgame (eg, HUD).
     * @param difficulty dificuldade [0,1] que esta instância deve ter.
     * @param maxDuration quanto tempo este microgame vai durar.
     * @param endOfGameSituation um
     * {@link br.microgamr.microgames.util.TimeoutBehavior} indicando o que
     * acontece quando o tempo acaba.
     */
    public MicroGame(BaseScreen screen, MicroGameStateObserver observer,
            float difficulty, float maxDuration,
            TimeoutBehavior endOfGameSituation) {
        if (difficulty < 0 || difficulty > 1) {
            throw new IllegalArgumentException(
                    "A dificuldade (difficulty) de um microgame deve ser um "
                    + "número entre 0 e 1. Você passou o número " + difficulty
                    + ".");
        }

        this.assets = screen.assets;
        this.viewport = screen.viewport;
        this.batch = screen.batch;
        this.challengeSolved = endOfGameSituation
                == TimeoutBehavior.WINS_WHEN_MICROGAME_ENDS;
        this.maxDuration = maxDuration;
        this.timeSpentPlaying = 0;
        this.timeSpentOnInstructions = 0;
        this.stateObserver = observer;
        this.rand = new Random();
        this.timer = new Timer();
        this.timer.stop();
        this.difficulty = difficulty;
        state = MicroGameState.SHOWING_INSTRUCTIONS;
    }

    /**
     * Dá início ao microgame.
     */
    public final void start() {
        this.configureDifficultyParameters(this.difficulty);
        transitionTo(MicroGameState.SHOWING_INSTRUCTIONS);
    }

    /**
     * Verifica o pressionamento de teclas/movimento do mouse/toque.
     */
    public final void handleInput() {
        // deixa o MicroGame lidar com o input apenas se estivermos no estado
        // de jogo propriamente dito e sem pausa
        if (this.state == MicroGameState.PLAYING && !isPaused) {
            onHandlePlayingInput();
        }
    }

    /**
     * Pausa o jogo e notifica o observador.
     */
    public final void pause() {
        isPaused = true;

        // interrompe o timer do microgame, salvando o momento em
        // que o jogo foi pausado
        this.timer.stop();
        this.timeWhenPausedLastTime = TimeUtils.nanosToMillis(
                TimeUtils.nanoTime());

        // libera o cursor do mouse
        Gdx.input.setCursorCatched(false);

        if (state == MicroGameState.PLAYING) {
            onGamePaused(isPaused);
        }
    }

    /**
     * Retoma o jogo após uma pausa e notifica o observador.
     */
    public final void resume() {
        isPaused = false;

        // retoma o timer, atrasando-o pelo tempo que o jogo ficou pausado
        this.timer.start();
        this.timer.delay(TimeUtils.nanosToMillis(
                TimeUtils.nanoTime()) - this.timeWhenPausedLastTime);

        // se a pausa foi feita durante o jogo (fora das instruções
        // ou do final do jogo), oculta novamente o cursor
        if (state == MicroGameState.PLAYING) {
            Gdx.input.setCursorCatched(shouldHideMousePointer());
            onGamePaused(isPaused);
        }

    }

    /**
     * Atualiza a lógica do jogo, se ele não estiver pausado.
     *
     * @param dt quanto tempo se passou desde a última atualização.
     */
    public final void update(float dt) {
        if (isPaused) {
            return;
        }

        switch (this.state) {
            case SHOWING_INSTRUCTIONS:
                this.timeSpentOnInstructions += dt;
                if (timeSpentOnInstructions
                        > Config.TIME_SHOWING_MICROGAME_INSTRUCTIONS) {
                    transitionTo(MicroGameState.PLAYING);
                }

                break;

            case PLAYING:
                timeSpentPlaying += dt;
                if (timeSpentPlaying > maxDuration) {
                    transitionTo(challengeSolved
                            ? MicroGameState.PLAYER_SUCCEEDED
                            : MicroGameState.PLAYER_FAILED);
                }
                onUpdate(dt);
                break;
        }
    }

    /**
     * Desenha o jogo ou então apenas as mensagens de <em>countdown</em>,
     * dependendo do estado do microgame.
     */
    public final void draw() {
        switch (this.state) {
            case PLAYING:
                onDrawGame();
                break;

            case PLAYER_FAILED:
            case PLAYER_SUCCEEDED:
                onDrawGame();
                break;
        }
    }

    /**
     * Retorna se o microgame está pausado.
     *
     * @return <code>true</code> se estiver pausado, <code>false</code> do
     * contrário.
     */
    public final boolean isPaused() {
        return isPaused;
    }

    /**
     * Retorna o {@link com.badlogic.gdx.InputProcessor} que está sendo usado
     * por este microgame.
     *
     * @return o <code>InputProcessor</code> sendo usado.
     */
    public final InputProcessor getInputProcessor() {
        return microGameInputProcessor;
    }

    /**
     * Começa a usar o {@link com.badlogic.gdx.InputProcessor} enviado como
     * parâmetro.
     *
     * @param processor o <code>InputProcessor</code> que deve ser usado.
     */
    protected final void useInputProcessor(InputProcessor processor) {
        microGameInputProcessor = processor;
    }

    /**
     * Retorna o estado atual deste microgame.
     *
     * @return estado atual deste microgame.
     */
    protected final MicroGameState getState() {
        return state;
    }

    /**
     * Muda o estado do microgame para um novo
     * {@link br.microgamr.microgames.util.MicroGameState}. s
     *
     * @param newState novo estado.
     */
    private void transitionTo(MicroGameState newState) {
        switch (newState) {
            case PLAYING:
                this.onStart();

                this.timer.scheduleTask(new Task() {
                    @Override
                    public void run() {
                        stateObserver.onTimeEnding();
                    }
                }, (maxDuration - Config.MICROGAME_COUNTDOWN_ON_HUD_BEGIN_AT));

                timer.start();
                break;

            case PLAYER_SUCCEEDED:
            case PLAYER_FAILED:
                this.onEnd();
                timer.stop();
                break;
        }
        this.state = newState;
        this.stateObserver.onStateChanged(state);
    }

    /**
     * Define que o desafio deste microgame foi perdido. Deve ser chamado quando
     * o microgame determina que o jogador perdeu.
     */
    protected void challengeFailed() {
        this.challengeSolved = false;
        transitionTo(MicroGameState.PLAYER_FAILED);
    }

    /**
     * Define que o desafio deste microgame foi vencido. Deve ser chamado quando
     * o microgame determina que o jogador ganhou.
     */
    protected void challengeSolved() {
        this.challengeSolved = true;
        transitionTo(MicroGameState.PLAYER_SUCCEEDED);
    }

    /**
     * Exibe uma mensagem centralizada na HUD
     *
     * @param strMessage a mensagem.
     */
    protected void showMessage(String strMessage) {
        this.stateObserver.showMessage(strMessage);
    }

    /**
     * Configura os parâmetros de dificuldade do jogo como, por exemplo, a
     * quantidade de inimigos, a velocidade deles etc. Este método é chamado
     * pela própria MicroGame e, dentro dele, você deve apenas configurar suas
     * próprias variáveis relativas à dificuldade.
     *
     * @param difficulty a dificuldade, entre [0, 1].
     */
    protected abstract void configureDifficultyParameters(float difficulty);

    /**
     * Inicializa o jogo - jogador, inimigos, timers etc.
     */
    protected abstract void onStart();

    /**
     * Executa rotinas necessárias quando o jogo é terminado. Normalmente, usado
     * para interromper música de fundo.
     */
    protected abstract void onEnd();

    /**
     * É chamado pelo próprio MicroGame quando ele é pausado/resumido pelo
     * jogador. Pode ser usado para interromper/retomar a música de fundo, por
     * exemplo.
     *
     * @param justPaused acabou de pausar (true) ou resumir (false).
     */
    protected void onGamePaused(boolean justPaused) {

    }

    /**
     * Chamada o tempo todo, deve ser usada para detectar se o jogador interagiu
     * com a tela/teclado/mouse de alguma forma e, então, agir de acordo.
     */
    public abstract void onHandlePlayingInput();

    /**
     * Chamada o tempo todo, deve ser usada para atualizar a lógica do jogo.
     *
     * @param dt tempo (em segundos) desde a última vez que onUpdate(dt) foi
     * chamada.
     */
    public abstract void onUpdate(float dt);

    /**
     * Chamada o tempo todo, deve ser usada para desenhar o jogo.
     */
    public abstract void onDrawGame();

    /**
     * Chamada pelo próprio MicroGame para mostrar as instruções do jogo ao
     * jogador na tela. Basta retornar uma string com as instruções (que devem
     * ser curtas!).
     *
     * @return as instruções (no máximo, 3-5 palavras
     */
    public abstract String getInstructions();

    /**
     * Chamada pelo próprio jogo para saber se, durante o seu MicroGame, ele
     * deve esconder o ponteiro do mouse (no caso de um dispositivo com mouse).
     *
     * @return true/false para falar se deve esconder o ponteiro do mouse ou
     * não.
     */
    public abstract boolean shouldHideMousePointer();
}
