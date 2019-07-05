package br.microgamr.microgames.util;

/**
 * Estado de um @{link MiniGame}.
 *
 * @author Flávio Coutinho <fegemo@cefetmg.br>
 */
public enum MicroGameState {
    /**
     * Exibindo instruções.
     */
    SHOWING_INSTRUCTIONS,
    /**
     * Durante o jogo.
     */
    PLAYING,
    /**
     * Fim do microgame com vitória do jogador.
     */
    PLAYER_SUCCEEDED,
    /**
     * Fim do microgame com derrota do jogador.
     */
    PLAYER_FAILED
}
