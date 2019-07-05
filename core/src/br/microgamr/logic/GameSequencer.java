package br.microgamr.logic;

import br.microgamr.microgames.MicroGame;
import br.microgamr.microgames.factories.MicroGameFactory;
import br.microgamr.microgames.util.DifficultyCurve;
import br.microgamr.microgames.util.MicroGameStateObserver;
import br.microgamr.screens.BaseScreen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.math.MathUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Monta uma sequência finita de microgames a serem jogados.
 *
 * @author Flávio Coutinho <fegemo@cefetmg.br>
 */
public class GameSequencer extends BaseGameSequencer {

    private final int numberOfGames;
    private final ArrayList<MicroGameFactory> previousGames;
    private Integer[] indexSequence;
    private float finalDifficulty;
    private float initialDifficulty;

    /**
     * Cria um novo sequenciador com um número de microgames igual a
     * {@code numberOfGames}, a partir de um <em>pool</em> de microgames
     * {@code availableGames}.
     *
     * @param numberOfGames total de jogos que será criado para o jogador.
     * @param availableGames os tipos de microgames disponíveis para o
     * sequenciador.
     * @param initialDifficulty dificuldade usada para o primeiro MicroGame.
     * Deve estar entre 0 e 1.
     * @param finalDifficulty dificuldade usada para o último MicroGame. Deve
     * estar entre 0 e 1.
     */
    public GameSequencer(int numberOfGames, Set<MicroGameFactory> availableGames,
            float initialDifficulty, float finalDifficulty) {
        super(availableGames);
        if (numberOfGames <= 0) {
            throw new IllegalArgumentException("Tentou-se criar um "
                    + "GameSequencer com 0 jogos. Deve haver ao menos 1.");
        }
        this.numberOfGames = numberOfGames;
        this.initialDifficulty = initialDifficulty;
        this.finalDifficulty = finalDifficulty;
        previousGames = new ArrayList<MicroGameFactory>();
        indexSequence = new Integer[numberOfGames];
        determineGameSequence();
    }

    @Override
    public boolean hasNextGame() {
        return previousGames.size() < numberOfGames;
    }

    private void determineGameSequence() {
        int lastIndex = -1; // garante que o primeiro sorteio não será igual a lastIndex

        for (int i = 0; i < numberOfGames; i++) {
            indexSequence[i] = MathUtils.random(availableGames.size() - 1);
            int index = MathUtils.random(availableGames.size() - 1);
            if (index != lastIndex) {
                // garante que o último ínidce sorteado não irá repetir
                indexSequence[i] = index;
                lastIndex = (availableGames.size() == 1) ? -1 : index;
            } else {
                i--;
            }
        }
    }

    private float getSequenceProgress() {
        return Math.min(1, ((float) previousGames.size()) / (numberOfGames - 1));
    }

    /**
     * Pré-carrega os <em>assets</em> dos microgames que foram selecionados.
     *
     * @param assets o <code>AssetManager</code> da tela responsável pelos
     * microgames.
     */
    @Override
    public void preloadAssets(AssetManager assets) {
        HashMap<String, Class> allAssets = new HashMap<String, Class>();
        HashSet<Integer> allFactoriesIndices = new HashSet<Integer>(
                Arrays.asList(indexSequence));

        for (Integer i : allFactoriesIndices) {
            allAssets.putAll(((MicroGameFactory) availableGames.toArray()[i])
                    .getAssetsToPreload());
        }

        for (Map.Entry<String, Class> asset : allAssets.entrySet()) {
            assets.load(asset.getKey(), asset.getValue());
        }
    }

    /**
     * Retorna uma instância do próximo jogo.
     *
     * @return uma instância do próximo jogo.
     */
    @Override
    public MicroGame nextGame(BaseScreen screen, MicroGameStateObserver observer) {
        MicroGameFactory factory = (MicroGameFactory) availableGames
                .toArray()[indexSequence[getGameNumber()]];
        float difficulty = DifficultyCurve.S.getCurveValueBetween(
                getSequenceProgress(), initialDifficulty, finalDifficulty);
        previousGames.add(factory);

        return factory.createMicroGame(screen, observer, difficulty);
    }

    /**
     * Retorna o índice deste jogo na série de jogos criados para o jogador.
     *
     * @return o índice deste jogo na série de jogos criados para o jogador.
     */
    @Override
    public int getGameNumber() {
        return previousGames.size();
    }

}
