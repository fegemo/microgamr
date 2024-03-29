package br.microgamr.screens;

import br.microgamr.Config;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.TextureLoader;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.utils.TimeUtils;

/**
 * A tela de <em>splash</em> (inicial, com a logomarca) da turma.
 *
 * @author Flávio Coutinho <fegemo@cefetmg.br>
 */
public class SplashScreen extends BaseScreen {

    /**
     * Momento em que a tela foi mostrada (em milissegundos).
     */
    private long timeWhenScreenShowedUp;

    /**
     * Uma {@link com.badlogic.gdx.graphics.g2d.Sprite} que contém a 
     * logo da turma.
     */
    private Sprite logo;

    /**
     * Cria uma nova tela de <em>splash</em>.
     *
     * @param game O jogo dono desta tela.
     * @param previous A tela de onde o usuário veio.
     */
    public SplashScreen(Game game, BaseScreen previous) {
        super(game, previous);
    }

    /**
     * Configura parâmetros iniciais da tela.
     */
    @Override
    public void appear() {
        Gdx.gl.glClearColor(1, 1, 1, 1);
        timeWhenScreenShowedUp = TimeUtils.millis();

        TextureLoader.TextureParameter linearFilter = new TextureLoader.TextureParameter();
        linearFilter.minFilter = Texture.TextureFilter.Linear;
        linearFilter.magFilter = Texture.TextureFilter.Linear;

        assets.load("splash/logo.png", Texture.class, linearFilter);
    }

    @Override
    protected void assetsLoaded() {
        logo = new Sprite(assets.get("splash/logo.png", Texture.class));
        logo.setCenter(
                super.viewport.getWorldWidth() / 2,
                super.viewport.getWorldHeight() / 2);
        logo.setScale(0.4f);
    }

    @Override
    public void cleanUp() {
        logo.getTexture().dispose();
    }

    /**
     * Navega para a tela de Menu.
     */
    private void navigateToMenuScreen() {
        game.setScreen(new MenuScreen(game, this));
    }

    /**
     * Verifica se houve <em>input</em> do jogador.
     */
    @Override
    public void handleInput() {
        // se o jogador apertar alguma tecla, clicar com o mouse ou 
        // tocar a tela, pula direto para a próxima tela.
        if (Gdx.input.justTouched()) {
            navigateToMenuScreen();
        }
    }

    /**
     * Atualiza a lógica da tela de acordo com o tempo.
     *
     * @param dt Tempo desde a última chamada.
     */
    @Override
    public void update(float dt) {
        // verifica se o tempo em que se passou na tela é maior do que o máximo
        // para que possamos navegar para a próxima tela.
        if (TimeUtils.timeSinceMillis(timeWhenScreenShowedUp)
                >= Config.TIME_ON_SPLASH_SCREEN) {
            navigateToMenuScreen();
        }
    }

    /**
     * Desenha a {@link com.badlogic.gdx.graphics.g2d.Sprite} com a logomarca.
     */
    @Override
    public void draw() {
        batch.begin();
        logo.draw(batch);
        batch.end();
    }
}
