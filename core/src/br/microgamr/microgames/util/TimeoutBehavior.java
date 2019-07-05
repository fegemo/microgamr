package br.microgamr.microgames.util;

/**
 * Define o que acontece quando do término do microgame por causa de tempo
 * esgotado.
 *
 * @author Flávio Coutinho <fegemo@cefetmg.br>
 */
public enum TimeoutBehavior {
    /**
     * Jogador vence o desafio quando o tempo acaba e ele não foi derrotado.
     *
     * Basicamente, ele deve sobreviver/impedir que algo aconteça até o final.
     */
    WINS_WHEN_MICROGAME_ENDS,
    /**
     * Jogador perde o desafio quando o tempo acaba e ele não conseguiu executar
     * o que o microgame propôs.
     *
     * Basicamente, o jogador precisa fazer uma atividade dentro do limite de
     * tempo do microgame.
     */
    FAILS_WHEN_MICROGAME_ENDS
}
