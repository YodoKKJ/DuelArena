package com.brunobeduschi.duelarena.duel;

import com.brunobeduschi.duelarena.arena.Arena;
import com.brunobeduschi.duelarena.kit.Kit;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.plugin.Plugin;

public class Duel {

    public enum Phase {
        COUNTDOWN,
        FIGHTING
    }

    private final Player player1;
    private final Player player2;
    private final Arena arena;
    private final Kit kit;
    private final PlayerStateSnapshot snapshot1;
    private final PlayerStateSnapshot snapshot2;
    private Phase phase = Phase.COUNTDOWN;
    private BukkitTask scoreboardTask;

    public Duel(Player player1, Player player2, Arena arena, Kit kit) {
        this.player1 = player1;
        this.player2 = player2;
        this.arena = arena;
        this.kit = kit;
        this.snapshot1 = new PlayerStateSnapshot(player1);
        this.snapshot2 = new PlayerStateSnapshot(player2);
    }

    public Player getPlayer1() {
        return player1;
    }

    public Player getPlayer2() {
        return player2;
    }

    public Arena getArena() {
        return arena;
    }

    public Kit getKit() {
        return kit;
    }

    public Phase getPhase() {
        return phase;
    }

    public void setPhase(Phase phase) {
        this.phase = phase;
    }

    public Player getOpponent(Player player) {
        return player.equals(player1) ? player2 : player1;
    }

    public PlayerStateSnapshot getSnapshot(Player player) {
        return player.equals(player1) ? snapshot1 : snapshot2;
    }

    public void startScoreboard(Plugin plugin) {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        if (manager == null) {
            return;
        }
        Scoreboard board1 = manager.getNewScoreboard();
        Scoreboard board2 = manager.getNewScoreboard();
        Objective objective1 = board1.registerNewObjective("duel", Criteria.DUMMY, Component.text("Duelo", NamedTextColor.GOLD));
        objective1.setDisplaySlot(DisplaySlot.SIDEBAR);
        Objective objective2 = board2.registerNewObjective("duel", Criteria.DUMMY, Component.text("Duelo", NamedTextColor.GOLD));
        objective2.setDisplaySlot(DisplaySlot.SIDEBAR);
        player1.setScoreboard(board1);
        player2.setScoreboard(board2);

        scoreboardTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (phase != Phase.FIGHTING || !player1.isOnline() || !player2.isOnline()) {
                    return;
                }
                update(objective1, player1, player2);
                update(objective2, player2, player1);
            }
        }.runTaskTimer(plugin, 0L, 10L);
    }

    private void update(Objective objective, Player self, Player opponent) {
        for (String entry : objective.getScoreboard().getEntries()) {
            objective.getScoreboard().resetScores(entry);
        }
        objective.getScore("Você: %.1f HP".formatted(self.getHealth())).setScore(2);
        objective.getScore("Oponente: %.1f HP".formatted(opponent.getHealth())).setScore(1);
    }

    public void stopScoreboard() {
        if (scoreboardTask != null) {
            scoreboardTask.cancel();
        }
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        if (manager == null) {
            return;
        }
        if (player1.isOnline()) {
            player1.setScoreboard(manager.getMainScoreboard());
        }
        if (player2.isOnline()) {
            player2.setScoreboard(manager.getMainScoreboard());
        }
    }
}
