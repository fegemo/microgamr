package br.microgamr.microgames.factories;

import br.microgamr.microgames.MicroGame;
import br.microgamr.screens.BaseScreen;
import java.util.Map;
import br.microgamr.microgames.util.MicroGameStateObserver;

/**
 * Uma fábrica (padrão de projeto GoF Factory) que sabe instanciar um tipo de
 * MicroGame. Além de saber instanciar, uma fábrica também deve retornar um
 * conjunto de recursos (<em>assets</em>) necessários ao microgame pois eles
 * serão pré-carregados.
 *
 * @author Flávio Coutinho <fegemo@cefetmg.br>
 */
public interface MicroGameFactory {

    /**
     * Retorna uma instância de um MiniGame.
     *
     * @param screen a tela "dona" do minigame.
     * @param observer alguém que se interesse por saber da alteração de estado
     * do jogo.
     * @param difficulty dificuldade ([0,1]).
     * @return a instância do MicroGame.
     */
    public MicroGame createMicroGame(BaseScreen screen,
            MicroGameStateObserver observer, float difficulty);

    /**
     * Retorna os recursos que devem ser pré-carregados para este MiniGame.
     *
     * @return os recursos que devem ser pré-carregados para este MiniGame.
     */
    public Map<String, Class> getAssetsToPreload();
}
