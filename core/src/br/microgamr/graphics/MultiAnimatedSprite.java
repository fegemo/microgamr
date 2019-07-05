package br.microgamr.graphics;

import com.badlogic.gdx.graphics.g2d.Animation;
import java.util.Map;
import net.dermetfan.gdx.graphics.g2d.AnimatedSprite;

/**
 * Uma sprite que pode conter várias animações em vez de apenas uma. Herda de
 * {@link AnimatedSprite} e recebe um mapa de animações (nome -> animation),
 * além do nome da animação inicial.
 *
 * @author Flávio Coutinho <fegemo@cefetmg.br>
 */
public class MultiAnimatedSprite extends AnimatedSprite {

    private final Map<String, Animation> animations;

    public MultiAnimatedSprite(Map<String, Animation> animations,
            String initialAnimationName) {
        super(animations.get(initialAnimationName));
        this.animations = animations;
        super.setAutoUpdate(false);
    }

    public void startAnimation(String animationName) {
        if (!animations.containsKey(animationName)) {
            throw new IllegalArgumentException(
                    "Pediu-se para iniciar uma animação com o nome + '"
                    + animationName + "', mas esta MultiAnimatedSprite"
                    + "não possui uma animação com esse nome");
        }

        // redefine o tempo decorrente da animação anterior (se existir)
        // para 0 para poder iniciar a nova do início
        super.setTime(0);

        // define qual é a animação
        super.setAnimation(animations.get(animationName));

        // começa a animação
        super.play();
    }

    public void stopAnimation() {
        super.stop();
    }

}
