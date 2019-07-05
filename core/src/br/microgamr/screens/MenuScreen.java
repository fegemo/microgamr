package br.microgamr.screens;

import br.microgamr.Config;
import br.microgamr.logic.GameSequencer;
import br.microgamr.microgames.factories.*;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.TextureLoader;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import java.util.Arrays;
import java.util.HashSet;

/**
 * A tela de menu principal do jogo.
 *
 * @author Flávio Coutinho <fegemo@cefetmg.br>
 */
public class MenuScreen extends BaseScreen {

    private static final int NUMBER_OF_TILED_BACKGROUND_TEXTURE = 7;
    private TextureRegion background;

    /**
     * Cria uma nova tela de menu.
     *
     * @param game o jogo dono desta tela.
     * @param previous a tela de onde o usuário veio.
     */
    public MenuScreen(Game game, BaseScreen previous) {
        super(game, previous);
    }

    /**
     * Configura parâmetros da tela e instancia objetos.
     */
    @Override
    public void appear() {
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.input.setCursorCatched(false);

        // instancia a textura e a região de textura (usada para repetir)
        TextureLoader.TextureParameter linearFilter = new TextureLoader.TextureParameter();
        linearFilter.minFilter = Texture.TextureFilter.Linear;
        linearFilter.magFilter = Texture.TextureFilter.Linear;

        assets.load("menu/menu-background.png", Texture.class, linearFilter);
    }

    @Override
    protected void assetsLoaded() {
        // instancia a textura e a região de textura (usada para repetir)
        background = new TextureRegion(assets.get("menu/menu-background.png", Texture.class));
        // configura a textura para repetir caso ela ocupe menos espaço que o
        // espaço disponível
        background.getTexture().setWrap(
                Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);

        // define a largura da região de desenho de forma que ela seja repetida
        // um número de vezes igual a NUMBER_OF_TILED_BACKGROUND_TEXTURE
        background.setRegionWidth(
                background.getTexture().getWidth()
                * NUMBER_OF_TILED_BACKGROUND_TEXTURE);
        // idem para altura, porém será repetida um número de vezes igual a
        // NUMBER_OF_TILED_BACKGROUND_TEXTURE * razãoDeAspecto
        background.setRegionHeight(
                (int) (background.getTexture().getHeight()
                * NUMBER_OF_TILED_BACKGROUND_TEXTURE
                / Config.DESIRED_ASPECT_RATIO));
    }

    /**
     * Recebe <em>input</em> do jogador.
     */
    @Override
    public void handleInput() {
        // se qualquer interação é feita (teclado, mouse pressionado, tecla
        // tocada), navega para a próxima tela (de jogo)
        if (Gdx.input.justTouched()) {
            navigateToMicroGameScreen();
        }
    }

    /**
     * Atualiza a lógica da tela.
     *
     * @param dt Tempo desde a última atualização.
     */
    @Override
    public void update(float dt) {
        float speed = dt * 0.25f;
        background.scroll(speed, -speed);
    }

    /**
     * Desenha o conteúdo da tela de Menu.
     */
    @Override
    public void draw() {
        batch.begin();
        batch.draw(background, 0, 0,
                viewport.getWorldWidth(),
                viewport.getWorldHeight());
        drawCenterAlignedText("Pressione qualquer tecla para jogar",
                viewport.getWorldHeight() * 0.35f);
        batch.end();
    }

    /**
     * Navega para a tela de jogo.
     */
    private void navigateToMicroGameScreen() {
        final float difficultyOfFirstMicrogame = 0;
        final float difficultyOfLastMicrogame = 1;
        final int numberOfGamesInSequence = 5;
        // cria um sequenciador com um número de jogos em sequência e a lista
        // de microgames disponíveis para serem sorteados, além de definir a
        // dificuldade do microgame inicial e a do final
        GameSequencer sequencer = new GameSequencer(numberOfGamesInSequence,
                new HashSet<MicroGameFactory>(
                Arrays.asList(
                        // microgames iniciais, de exemplo
                        // (*não devem ser removidos* ao commitar)
                        new ShootTheMonstersFactory(),
                        new ExpelTheMonstersFactory()
                        // microgames do grupo 1:
                        
                        // microgames do grupo 2:
                        
                        // microgames do grupo 3:
                        
                        // microgames do grupo 4:
                        
                        // microgames do grupo 5:
                        
                        // microgames do grupo 6:
                        
                        // microgames do grupo 7:
                        
                        // microgames do grupo 8:
                        
                        // microgames do grupo 9:
                        
                        // microgames do grupo 10:
                        
                        // microgames do grupo 11:
                        
                        // microgames do grupo 12:
                        
                        // microgames do grupo 13:
                        
                        // microgames do grupo 14:
                        
                        // microgames do grupo 15:
                )
        ), difficultyOfFirstMicrogame, difficultyOfLastMicrogame);
        game.setScreen(new GameScreen(game, this, sequencer));
    }

    /**
     * Libera os recursos necessários para esta tela.
     */
    @Override
    public void cleanUp() {
        background.getTexture().dispose();
    }

}
