package br.microgamr.microgames.util;

/**
 * Um observador do estado de um microgame. É implementado pela
 * {@link br.microgamr.screens.GameScreen}, que precisa ser notificada quando o jogo é pausado,
 * resumido, quando o tempo está acabando e quando seu estado
 * {@link br.microgamr.microgames.util.MicroGameState} muda.
 *
 * @author Flávio Coutinho <fegemo@cefetmg.br>
 */
public interface MicroGameStateObserver {
    /**
     * Chamado quando o estado 
     * {@link br.microgamr.microgames.util.MicroGameState} do microgame
     * atual é alterado. Por exemplo, quando está 
     * <code>SHOWING_INSTRUCTIONS</code> e passa para <code>PLAYING</code>.
     * 
     * @param state o novo estado do microgame atual.
     */
    void onStateChanged(MicroGameState state);

    /**
     * Chamada quando faltam 3s para o microgame acabar.
     */
    void onTimeEnding();

    /**
     * Chamada quando o microgame é pausado.
     */
    void onGamePaused();

    /**
     * Chamada quando o microgame é resumido, depois de uma pausa.
     */
    void onGameResumed();

    /**
     * Usada para mostrar uma mensagem na tela de jogo.
     * @param strMessage 
     */
    void showMessage(String strMessage);
}
