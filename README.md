# DuelArena

Plugin de duelos 1v1 para servidores Paper/Spigot, com sistema de convites, arenas configuráveis in-game, kits, contagem regressiva e placar de vitórias.

## Funcionalidades

- **Convite de duelo** entre jogadores, com expiração automática após 30 segundos.
- **Arenas configuráveis em jogo** (sem editar arquivos manualmente): admin fica na posição desejada e usa comandos pra marcar os spawns e (opcionalmente) os limites da área.
- **Contagem regressiva** (5 → LUTE!) antes do combate começar; jogadores ficam congelados e invulneráveis durante a contagem.
- **Kits configuráveis** (itens + armadura) aplicados automaticamente ao início do duelo — inventário original do jogador é salvo e restaurado ao fim.
- **Interceptação do golpe fatal**: em vez de deixar o jogador realmente morrer (e lidar com tela de respawn), o dano que zeraria a vida é cancelado e o duelo termina ali — mais limpo pra minigames.
- **Placar (scoreboard) na lateral da tela** durante o combate, mostrando a vida de você e do oponente em tempo real.
- **Limite de área**: se a arena tiver limites definidos, o jogador que sair da área é teleportado de volta ao seu spawn.
- **Estatísticas persistentes** (vitórias/derrotas) salvas em `stats.yml`, com ranking (`/duel top`).
- Duelo é encerrado automaticamente se um dos jogadores desconectar (derrota por W.O.).

## Comandos

| Comando | Descrição | Permissão |
|---|---|---|
| `/duel <jogador>` | Envia um convite de duelo | — |
| `/duel accept` | Aceita o convite pendente | — |
| `/duel deny` | Recusa o convite pendente | — |
| `/duel top` | Mostra os 5 jogadores com mais vitórias | — |
| `/duel stats [jogador]` | Mostra vitórias/derrotas suas ou de outro jogador | — |
| `/arena create <nome>` | Cria uma nova arena | `duelarena.admin` (padrão: OP) |
| `/arena setspawn1 \| setspawn2 <nome>` | Define o ponto de spawn 1/2 na posição atual | `duelarena.admin` |
| `/arena setbounds1 \| setbounds2 <nome>` | Define os cantos da área da arena (opcional) | `duelarena.admin` |
| `/arena remove <nome>` | Remove uma arena | `duelarena.admin` |
| `/arena list` | Lista as arenas existentes | `duelarena.admin` |

## Como configurar uma arena

1. Vá até um local seguro no mapa e rode `/arena create arena1`
2. Fique no ponto onde o jogador 1 deve nascer e rode `/arena setspawn1 arena1`
3. Vá até o ponto do jogador 2 e rode `/arena setspawn2 arena1`
4. (Opcional) Fique nos dois cantos opostos da área e rode `/arena setbounds1 arena1` / `/arena setbounds2 arena1` para limitar o combate a essa região
5. Pronto — `/duel <jogador>` já vai poder usar essa arena

Um servidor pode ter várias arenas; o plugin sempre usa a primeira que estiver livre (pronta e sem duelo em andamento).

## Configuração de kits (`config.yml`)

```yaml
default-kit: guerreiro

kits:
  guerreiro:
    items:
      0:
        material: IRON_SWORD
        amount: 1
    armor:
      helmet: IRON_HELMET
      chestplate: IRON_CHESTPLATE
      leggings: IRON_LEGGINGS
      boots: IRON_BOOTS
```

Cada número em `items` é o slot do inventário (0-35) onde o item será colocado.

## Arquitetura

```
DuelArenaPlugin              → classe principal
arena/Arena, ArenaManager    → modelo e persistência de arenas (arenas.yml, usando serialização nativa de Location)
kit/Kit, KitManager          → carregamento de kits a partir do config.yml
duel/Duel                    → estado de um duelo em andamento (fase, snapshots, placar)
duel/DuelManager             → orquestra convites, início, contagem regressiva e fim de duelo
duel/PlayerStateSnapshot     → salva/restaura inventário, vida, XP, gamemode e localização do jogador
stats/StatsManager           → persistência de vitórias/derrotas em stats.yml
listeners/DuelListener       → intercepta dano fatal, desconexão e movimento (congelamento/limite de área)
commands/                    → DuelCommand, ArenaAdminCommand
```

## Stack técnica

- **Paper API 26.1.2** (Minecraft 26.1.2)
- Adventure API (Components/Titles) para mensagens e títulos na tela
- Scoreboard API para o placar lateral
- Java 21, Maven

## Build

```
mvn clean package
```

O jar final fica em `target/DuelArena.jar`. Basta colocar na pasta `plugins/` do servidor.
