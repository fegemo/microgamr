package br.microgamr.logic;

import br.microgamr.microgames.MicroGame;
import br.microgamr.microgames.factories.MicroGameFactory;
import br.microgamr.microgames.util.MicroGameStateObserver;
import br.microgamr.screens.BaseScreen;
import com.badlogic.gdx.assets.AssetManager;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Uma classe base que define um algoritmo para determinar a sequência de jogos
 * a ser escolhida. Esta classe deve ser herdada para implementar uma estratégia
 * de definição de sequência de microgames.
 *
 * @author Flávio Coutinho <fegemo@cefetmg.br>
 */
public abstract class BaseGameSequencer {

    protected final Set<MicroGameFactory> availableGames;

    /**
     * Cria um algoritmo sequenciador que pega um conjunto de microgames
     * (factories deles, na verdade), a tela do jogo que vai executá-los e
     * potenciais observadores do estado do microgame.
     *
     * @param availableGames conjunto de fábricas dos microgames.
     */
    public BaseGameSequencer(Set<MicroGameFactory> availableGames) {
        this.availableGames = availableGames;
    }

    /**
     * Pré-carrega os <em>assets</em> dos microgames que foram selecionados.
     *
     * @param assets o <code>AssetManager</code> da tela responsável pelos
     * microgames.
     */
    public void preloadAssets(AssetManager assets) {
        HashMap<String, Class> allAssets = new HashMap<String, Class>();

        for (MicroGameFactory factory : availableGames) {
            allAssets.putAll(factory.getAssetsToPreload());
        }

        for (Map.Entry<String, Class> asset : allAssets.entrySet()) {
            assets.load(asset.getKey(), asset.getValue());
        }
    }

    /**
     * Retorna se ainda há um próximo jogo, ou se todos da sequência já foram
     * jogados.
     *
     * @return true se ainda há um jogo, false do contrário.
     */
    public abstract boolean hasNextGame();

    /**
     * Retorna uma instância do próximo jogo.
     *
     * @param screen a tela que está executando os microgames.
     * @param observer classe que está interessada em saber o estado do
     * microgame atual - tipicamente, é a própria tela de jogo (mesmo que
     * screen).
     *
     * @return a instância do próximo jogo, devidamente construída por sua
     * factory.
     */
    public abstract MicroGame nextGame(BaseScreen screen, MicroGameStateObserver observer);

    /**
     * Retorna o índice do jogo na sequência, começando de 0.
     *
     * @return o índice do jogo na sequência.
     */
    public abstract int getGameNumber();
}
