package br.microgamr.screens;

import br.microgamr.Config;
import br.microgamr.graphics.hud.Hud;
import br.microgamr.logic.BaseGameSequencer;
import br.microgamr.microgames.MicroGame;
import br.microgamr.microgames.util.MicroGameState;
import br.microgamr.microgames.util.MicroGameStateObserver;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.assets.loaders.TextureLoader;
import com.badlogic.gdx.assets.loaders.TextureLoader.TextureParameter;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;

/**
 * Tela de jogo. Executa uma sequência de microgames, dada pelo
 * {@link br.microgamr.logic.BaseGameSequencer}.
 *
 * @author Flávio Coutinho <fegemo@cefetmg.br>
 */
public class GameScreen extends BaseScreen
        implements MicroGameStateObserver {

    private MicroGame currentGame;
    private final BaseGameSequencer sequencer;
    private final Hud hud;
    private GameScreenState state;
    private int lives;
    private final InputMultiplexer inputMultiplexer;

    public GameScreen(Game game, BaseScreen previous, BaseGameSequencer gameSequencer) {
        super(game, previous);
        state = GameScreenState.PLAYING;
        lives = Config.MAX_LIVES;
        sequencer = gameSequencer;
        hud = new Hud(this, this);
        inputMultiplexer = new InputMultiplexer();
    }

    @Override
    public void appear() {
        Gdx.gl.glClearColor(1, 1, 1, 1);

        TextureParameter linearFilter = new TextureLoader.TextureParameter();
        linearFilter.minFilter = Texture.TextureFilter.Linear;
        linearFilter.magFilter = Texture.TextureFilter.Linear;

        assets.load("hud/countdown.png", Texture.class, linearFilter);
        assets.load("hud/gray-mask.png", Texture.class, linearFilter);
        assets.load("hud/unpause-button.png", Texture.class, linearFilter);
        assets.load("hud/pause-button.png", Texture.class, linearFilter);
        assets.load("hud/lives.png", Texture.class, linearFilter);
        assets.load("hud/clock.png", Texture.class, linearFilter);
        assets.load("hud/tick-tock.mp3", Sound.class);
        Gdx.input.setInputProcessor(inputMultiplexer);

        // avisa o game sequencer para pré-carregar os assets dos microgames
        sequencer.preloadAssets(assets);
    }

    @Override
    protected void assetsLoaded() {
        hud.create();
        inputMultiplexer.addProcessor(hud.getInputProcessor());
        advance();
    }

    @Override
    public void cleanUp() {
        Gdx.input.setCursorCatched(false);
        Gdx.input.setInputProcessor(null);
        assets.dispose();
    }

    @Override
    public void handleInput() {
        if (currentGame != null) {
            currentGame.handleInput();
        }

        if (state == GameScreenState.FINISHED_WON
                || state == GameScreenState.FINISHED_GAME_OVER) {
            if (Gdx.input.justTouched()) {
                // volta para o menu principal
                super.game.setScreen(new MenuScreen(super.game, this));
            }
        }
    }

    @Override
    public void update(float dt) {
        currentGame.update(dt);
        hud.update(dt);
    }

    @Override
    public void draw() {
        super.batch.begin();
        if (currentGame != null) {
            currentGame.draw();
        }
        if (state != GameScreenState.PLAYING) {
            drawEndGame();
        }
        super.batch.end();
        hud.draw();
    }

    private void advance() {
        if (this.state == GameScreenState.FINISHED_WON
                || this.state == GameScreenState.FINISHED_GAME_OVER) {
            // se deu gameover ou terminou a sequencia com sucesso,
            // não deixa avançar para próximo microgame
            return;
        }

        // se há um próximo jogo na sequência, carrega-o
        if (this.sequencer.hasNextGame()) {
            loadNextGame();
        } // se não há mais jogos, o jogador concluiu a sequência e ainda possui
        // vidas
        else {
            // mostra mensagem de vitória
            this.transitionTo(GameScreenState.FINISHED_WON);
        }
    }

    private void loadNextGame() {
        // carrega o novo jogo (pede ao sequenciador o próximo)
        currentGame = sequencer.nextGame(this, this);
        currentGame.start();

        // atualiza o número de sequência do jogo atual na HUD
        hud.setGameIndex(sequencer.getGameNumber());
    }

    private void drawEndGame() {
        super.drawCenterAlignedText("Toque para voltar ao Menu",
                super.viewport.getWorldHeight() * 0.35f);
    }

    private void loseLife() {
        lives--;
        hud.setLives(lives);
        if (lives == 0) {
            transitionTo(GameScreenState.FINISHED_GAME_OVER);
        }
    }

    private void transitionTo(GameScreenState newState) {
        switch (newState) {
            case FINISHED_GAME_OVER:
                Gdx.input.setCursorCatched(false);
                break;

        }
        this.state = newState;
    }

    // <editor-fold defaultstate="expanded" desc="Implementação da interface MicroGameStateObserver">
    @Override
    public void onStateChanged(MicroGameState state) {
        switch (state) {
            case SHOWING_INSTRUCTIONS:
                hud.showGameInstructions(currentGame.getInstructions());
                hud.startInitialCountdown();
                hud.showPauseButton();
                break;

            case PLAYING:
                hud.hideGameInstructions();
                Gdx.input.setCursorCatched(currentGame.shouldHideMousePointer());
                if (currentGame.getInputProcessor() != null) {
                    inputMultiplexer.addProcessor(
                            currentGame.getInputProcessor());
                }
                break;

            case PLAYER_SUCCEEDED:
                if (sequencer.hasNextGame()) {
                    Gdx.input.setCursorCatched(false);
                }
            // deixa passar para próximo caso (esta foi
            // uma decisão consciente =)

            case PLAYER_FAILED:
                hud.hidePauseButton();
                hud.showMessage(state == MicroGameState.PLAYER_FAILED ? "Falhou!" : "Conseguiu!");
                if (state == MicroGameState.PLAYER_FAILED) {
                    loseLife();
                }

                inputMultiplexer.removeProcessor(currentGame.getInputProcessor());
                Timer.instance().scheduleTask(new Task() {
                    @Override
                    public void run() {
                        advance();
                    }

                }, 1.5f);

                Gdx.input.setCursorCatched(false);
                hud.cancelEndingTimer();
                break;
        }
    }

    @Override
    public void onTimeEnding() {
        hud.startEndingTimer();
    }

    @Override
    public void onGamePaused() {
        currentGame.pause();

        // desabilita até que o jogo seja despausado
        inputMultiplexer.removeProcessor(currentGame.getInputProcessor());
    }

    @Override
    public void onGameResumed() {
        currentGame.resume();

        // recupera o possível processador de input do microgame
        if (currentGame.getInputProcessor() != null) {
            inputMultiplexer.addProcessor(currentGame.getInputProcessor());
        }
    }

    @Override
    public void showMessage(String strMessage) {
        hud.showMessage(strMessage);
    }
    // </editor-fold>

    enum GameScreenState {
        PLAYING,
        FINISHED_GAME_OVER,
        FINISHED_WON
    }
}
