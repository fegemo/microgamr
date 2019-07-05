package br.microgamr.microgames.factories;

import br.microgamr.microgames.MicroGame;
import br.microgamr.microgames.ShootTheMonsters;
import br.microgamr.microgames.util.MicroGameStateObserver;
import br.microgamr.screens.BaseScreen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import java.util.HashMap;
import java.util.Map;

/**
 * Fábrica do jogo ShootTheMonsters.
 * 
 * @author fegemo <coutinho@decom.cefetmg.br>
 */
public class ShootTheMonstersFactory implements MicroGameFactory {

    /**
     * Veja {@link br.microgamr.microgames.factories.MicroGameFactory}.
     *
     * @param screen a tela onde o microgame será executado.
     * @param observer um observador interessado no estado deste microgame.
     * @param difficulty a dificuldade [0,1] desta instância do microgame.
     * @return Retorna um microgame ShootTheMonsters.
     */
    @Override
    public MicroGame createMicroGame(BaseScreen screen,
            MicroGameStateObserver observer, float difficulty) {
        return new ShootTheMonsters(screen, observer, difficulty);
    }

    /**
     * Veja {@link br.microgamr.microgames.factories.MicroGameFactory}.
     *
     * @return todos os <em>assets</em> usados neste microgame.
     */
    @Override
    public Map<String, Class> getAssetsToPreload() {
        return new HashMap<String, Class>() {
            {
                // texturas
                put("shoot-the-monsters/monster.png", Texture.class);
                put("shoot-the-monsters/target.png", Texture.class);
                
                // efeitos sonoros
                put("shoot-the-monsters/monster1.mp3", Sound.class);
                put("shoot-the-monsters/monster2.mp3", Sound.class);
                
                // música
                put("shoot-the-monsters/music.mp3", Music.class);
                
            }
        };
    }

}
