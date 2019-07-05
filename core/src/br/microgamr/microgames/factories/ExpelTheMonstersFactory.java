package br.microgamr.microgames.factories;

import br.microgamr.microgames.ExpelTheMonsters;
import br.microgamr.microgames.MicroGame;
import br.microgamr.microgames.util.MicroGameStateObserver;
import br.microgamr.screens.BaseScreen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import java.util.HashMap;
import java.util.Map;

/**
 * Fábrica do jogo ExeplTheMonsters.
 * 
 * @author Flávio Coutinho <fegemo@cefetmg.br>
 */
public class ExpelTheMonstersFactory implements MicroGameFactory {

    @Override
    public MicroGame createMicroGame(BaseScreen screen,
            MicroGameStateObserver observer, float difficulty) {
        return new ExpelTheMonsters(screen, observer, difficulty);
    }

    @Override
    public Map<String, Class> getAssetsToPreload() {
        return new HashMap<String, Class>() {
            {
                // texturas
                put("expel-the-monsters/toothbrush-spritesheet.png",
                        Texture.class);
                put("expel-the-monsters/monster-spritesheet.png",
                        Texture.class);
                put("expel-the-monsters/tooth.png", Texture.class);
                
                // efeitos sonors
                put("expel-the-monsters/appearing1.wav", Sound.class);
                put("expel-the-monsters/appearing2.wav", Sound.class);
                put("expel-the-monsters/appearing3.wav", Sound.class);
                put("expel-the-monsters/tooth-breaking.wav", Sound.class);
                
                // música
                put("expel-the-monsters/music.mp3", Music.class);
            }
        };
    }
}
