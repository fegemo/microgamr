# Microgamr

Um jogo simples, descomprometido e maroto com vários microgames.

## Instruções para Começar

O projeto deve ser entregue como um _Pull Request_ (veja
[[1]][using-pull-requests] e [[2]][creating-pull-requests]) neste repositório.
Ou seja, você deve fazer um _fork_ e, no **_branch_ `master`**, você deve
criar os dois microjogos.

Siga os passos:

1. (Um (01) integrante da dupla/grupo) faça um _fork_ deste repositório pela
   interface do Github.com.
1. Dê permissão de alteração no _fork_ para o(s) colega(s).
1. Clonem o (seu) repositório _forked_ para seu computador.
1. Criem um _branch_ cujo nome é o primeiro nome do(s) integrante(s) do grupo,
   sem letras maiúsculas e com hífen separando o(s) nome(s).
   - Por exemplo, `git branch -b sandy-junior`.
1. Trabalhem fazendo _commits_ nesse _branch_.
1. Quando estiver pronto, faça um _Pull Request_ do seu branch (_e.g._,
   `leandro-leonardo`) para o _branch_ `master` do professor.


### Descrição da Implementação

Veja alguns detalhes sobre a implementação do jogo a seguir.

#### Telas

O jogo possui algumas telas, como de _splash_ (inicial), menu principal e
"de jogo", e o código referente a cada uma reside em uma classe que herda de
`BaseScreen`.

![Pacote "screens" com as classes referentes às telas do jogo](docs/package-screens.png)


#### Pacotes

As classes do projeto estão modularizadas nos seguintes pacotes:

![Todos os pacotes do projeto](docs/packages.png)

1. `br.microgamr`: classes de inicialização e configuração geral do jogo.
1. `br.microgamr.graphics`: classes com utilitários gráficos.
1. `br.microgamr.logic`: classes de utilidade para a lógica de jogo.
1. `br.microgamr.minigames`: classes referentes aos _microgames_.
1. `br.microgamr.minigames.factories`: classes referentes às fábricas
   abstratas que são responsáveis por instanciar os _microgames_.
1. `br.microgamr.minigames.util`: classes utilitárias aos _microgames_.
1. `br.microgamr.screens`: classes referentes às telas do jogo.

#### _Assets_

Os _assets_ (recursos gráficos e de áudio) do jogo ficam na pasta `core/assets`:

![Pasta assets dentro da pasta core](docs/folder-assets.png)

Os _assets_ de cada _microgame_ devem estar dentro de uma pasta cujo nome é
o nome dele, **sem maiúsculas e acentuação, com hífen separando as palavras,**
caso haja mais de uma (_e.g._, `assets/shoot-the-monsters`).

Para os _microgames_, estamos usando um **gerenciador de _assets_** para
pré-carregá-los de forma que, quando da execução da sequência de _microgames_,
o jogo não pára para carregar os recursos e isso proporciona uma experiência
de jogo melhor.

Para usar o gerenciador, supondo que você está criando um `SuperMicroJogo`,
cada _microgame_ deve ser implementado em 2 passos:

1. Declarar de quais _assets_ ele precisa, na classe `SuperMicroJogoFactory`.
   Por exemplo:
   ```java
   package br.microgamr.microgames.factories;

   public class SuperMicroJogoFactory implements MicroGameFactory {
       // ...

       @Override
       public Map<String, Class> getAssetsToPreload() {
           return new HashMap<String, Class>() {
               {
                   // texturas
                   put("super-micro-jogo/personagem.png", Texture.class);
                   // efeitos sonoros
                   put("super-micro-jogo/tiro.wav", Sound.class);
                   // música de fundo
                   put("super-micro-jogo/musica.mp3", Music.class);
               }
           };
       }
   }  
   ```
1. Solicitar os _assets_ já carregados ao gerenciador, na classe do
   _microgame_ propriamente dito. Por exemplo:
   ```java
   public class SuperMicroJogo extends MicroGame {
       private Texture texturaPersonagem;
       private Sound somTiro;
       private Music musicaFundo;

       public SuperMicroJogo(BaseScreen screen,
             GameStateObserver observer, float difficulty) {
           super(
                   screen,        // tela que está executando este microgame.
                   observer,      // alguém interessado no estado (eg, HUD).
                   difficulty,    // dificuldade [0,1] que este jogo deve ter.
                   10,            // duração em segundos deste microgame.
                   TimeoutBehavior.FAILS_WHEN_MINIGAME_ENDS   // o que acontece
                                                              // qdo o tempo
                                                              // acaba.
           );
       }

       @Override
       protected void onStart() {
           this.texturaPersonagem = assets.get(
                  "super-micro-jogo/personagem.png", Texture.class);
           this.somTiro = assets.get(
                  "super-micro-jogo/tiro.wav", Sound.class);
           this.musicaFundo = assets.get(
                  "super-micro-jogo/musica.mp3", Music.class);
           // ...
       }
       // ...
   }
   ```

Após criar classes que herdam de `MicroGame` e `MicroGameFactory`, você
deve ir até `MenuScreen` e incluir a fábrica do seu microgame na lista de
microgames disponíveis e passar para o construtor de `GameSequencer`:

```java
/**
 * Navega para a tela de jogo.
 */
private void navigateToMicroGameScreen() {
    final float difficultyOfFirstMicrogame = 0;
    final float difficultyOfLastMicrogame = 1;
    final int numberOfGamesInSequence = 5;
    // cria um sequenciador com um número de jogos em sequência e a lista
    // de microgames disponíveis para serem sorteados, além de definir a
    // dificuldade do microgame inicial e a do final
    GameSequencer sequencer = new GameSequencer(numberOfGamesInSequence,
        new HashSet<MicroGameFactory>(
            Arrays.asList(
                    // microgames iniciais, de exemplo
                    // (*não devem ser removidos* ao commitar)
                    new ShootTheMonstersFactory(),
                    new ExpelTheMonstersFactory()
                    // microgames do grupo 1:

                    // microgames do grupo 2:

                    // microgames do grupo 3:
                    // ...
            )
        ),
        difficultyOfFirstMicrogame,
        difficultyOfLastMicrogame);
    game.setScreen(new GameScreen(game, this, sequencer));
}
```

_Nota: para ver como o pré-carregamento está sendo feito, procure na classe
`GameSequencer`._


#### Sistema de Coordenadas

Como muito bem lembramos da queridíssima aula de Computação Gráfica, uma
aplicação em OpenGL possui pelo menos 2 sistemas de coordenadas extremamente
importantes:

- O **sistema de coordenadas do mundo** (arbitrário, definido por nós)
- O **sistema de coordenadas da janela** (definido em pixels)

Em jogos digitais, qual deve ser o comportamento quando um usuário redimensiona
a janela? Há pelo menos 3 possibilidades:

1. A imagem que estamos renderizando fica espichada ou achatada (não
   queremos isso)
1. Mantemos a razão de aspecto (a imagem não distorce) e fazemos com que toda
   a imagem renderizada caiba dentro do espaço disponível
   - Isso faz com que surjam barras laterais ou superioes-inferiores "em branco"
     quando a nova dimensão da janela tem uma razão de aspecto diferente
     daquela para a qual o jogo foi programado (veja a imagem a seguir)
1. Mantemos a razão de aspecto e aumentamos o espaço do mundo que é visível ao
   jogador

![](docs/fit-viewport-1.png)
![](docs/arrow.png)
![](docs/fit-viewport-2.png)

É possível e bem simples fazer qualquer uma dessas formas na LibGDX. Optamos
pela forma (2) porque ela permite que usemos valores virtuais para largura e
altura da tela... como assim?

Podemos ver 3 constantes importantes em `Config.java`:
```java
public class Config {

    /**
     * A largura do mundo de jogo.
     *
     * Todos os objetos (sprites, etc.) devem estar contidos em coordenadas x
     * que vão de 0 a WORLD_WIDTH para que apareçam na tela.
     */
    public static final int WORLD_WIDTH = 1280;

    /**
     * A altura do mundo de jogo.
     *
     * Todos os objetos (sprites, etc.) devem estar contidos em coordenadas y
     * que vão de 0 a WORLD_HEIGHT para que apareçam na tela.
     */
    public static final int WORLD_HEIGHT = 720;

    public static final float DESIRED_ASPECT_RATIO
            = (float) WORLD_WIDTH / (float) WORLD_HEIGHT;

    // ...
}
```

Repare na figura a seguir. Podendo considerar que o sistema de coordenadas
do mundo é sempre x E [0,1280] e y E [0,720], fica fácil posicionar os
elementos de forma que eles estarão aonde queremos independente da
resolução atual da janela do jogo.

![](docs/coordinate-systems.png)

## FAQ

1. **Pergunta**: Por que o código está em inglês?
   - **Resposta**: as linguagens de programação e
     seus compiladores costumam ter problemas para identificar caracteres com
     acentuação nos códigos-fonte. Poderíamos escrever em Português sem usar a
     acentuação, porém se escrevermos em inglês, além de descartar essa
     possibilidade de problemas, tornamos o código-fonte acessível a leitores
     estrangeiros que saibam ler em Inglês.
1. **Pergunta**: Fiz meu _fork_, mas o professor foi lá e fez mais _commits_...
   agora meu  _fork_ está desatualizado. Como faço para ressincronizá-lo do
   o do professor?
   - **Resposta**: basta [atualizar o _branch_ master a partir do _fork_][syncing-fork]

[enunciado]: https://github.com/fegemo/cefet-games/blob/master/assignments/tp1-brushmyteethplz/README.md
[using-pull-requests]: https://help.github.com/articles/using-pull-requests/
[creating-pull-requests]: https://help.github.com/articles/creating-a-pull-request/
[syncing-fork]: https://help.github.com/articles/syncing-a-fork/
