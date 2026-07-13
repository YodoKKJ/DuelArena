package com.brunobeduschi.duelarena.duel;

import com.brunobeduschi.duelarena.DuelArenaPlugin;
import com.brunobeduschi.duelarena.arena.Arena;
import com.brunobeduschi.duelarena.kit.Kit;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DuelManager {

    private final DuelArenaPlugin plugin;
    private final Map<UUID, DuelRequest> pendingRequests = new HashMap<>();
    private final Map<UUID, Duel> activeDuels = new HashMap<>();

    public DuelManager(DuelArenaPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean isDueling(Player player) {
        return activeDuels.containsKey(player.getUniqueId());
    }

    public Duel getDuel(Player player) {
        return activeDuels.get(player.getUniqueId());
    }

    public void sendRequest(Player challenger, Player target) {
        if (challenger.equals(target)) {
            challenger.sendMessage(Component.text("Você não pode duelar consigo mesmo.", NamedTextColor.RED));
            return;
        }
        if (isDueling(challenger) || isDueling(target)) {
            challenger.sendMessage(Component.text("Um dos jogadores já está em um duelo.", NamedTextColor.RED));
            return;
        }
        if (pendingRequests.containsKey(target.getUniqueId())) {
            challenger.sendMessage(Component.text("Este jogador já tem um convite pendente.", NamedTextColor.RED));
            return;
        }
        if (plugin.getArenaManager().findFreeArena() == null) {
            challenger.sendMessage(Component.text("Nenhuma arena disponível no momento.", NamedTextColor.RED));
            return;
        }

        DuelRequest request = new DuelRequest(challenger.getUniqueId(), target.getUniqueId());
        pendingRequests.put(target.getUniqueId(), request);

        challenger.sendMessage(Component.text("Convite de duelo enviado a " + target.getName() + ".", NamedTextColor.GREEN));
        target.sendMessage(Component.text(challenger.getName() + " te desafiou para um duelo! Use /duel accept para aceitar.", NamedTextColor.GOLD));

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            DuelRequest current = pendingRequests.get(target.getUniqueId());
            if (current == request) {
                pendingRequests.remove(target.getUniqueId());
                if (challenger.isOnline()) {
                    challenger.sendMessage(Component.text("Seu convite de duelo para " + target.getName() + " expirou.", NamedTextColor.RED));
                }
                if (target.isOnline()) {
                    target.sendMessage(Component.text("O convite de duelo de " + challenger.getName() + " expirou.", NamedTextColor.RED));
                }
            }
        }, 30 * 20L);
    }

    public void acceptRequest(Player target) {
        DuelRequest request = pendingRequests.remove(target.getUniqueId());
        if (request == null) {
            target.sendMessage(Component.text("Você não tem nenhum convite de duelo pendente.", NamedTextColor.RED));
            return;
        }
        Player challenger = Bukkit.getPlayer(request.challenger());
        if (challenger == null || !challenger.isOnline()) {
            target.sendMessage(Component.text("O jogador que te desafiou não está mais online.", NamedTextColor.RED));
            return;
        }
        startDuel(challenger, target);
    }

    public void denyRequest(Player target) {
        DuelRequest request = pendingRequests.remove(target.getUniqueId());
        if (request == null) {
            target.sendMessage(Component.text("Você não tem nenhum convite de duelo pendente.", NamedTextColor.RED));
            return;
        }
        target.sendMessage(Component.text("Convite de duelo recusado.", NamedTextColor.YELLOW));
        Player challenger = Bukkit.getPlayer(request.challenger());
        if (challenger != null) {
            challenger.sendMessage(Component.text(target.getName() + " recusou seu convite de duelo.", NamedTextColor.RED));
        }
    }

    private void startDuel(Player p1, Player p2) {
        Arena arena = plugin.getArenaManager().findFreeArena();
        if (arena == null) {
            p1.sendMessage(Component.text("Nenhuma arena disponível no momento.", NamedTextColor.RED));
            p2.sendMessage(Component.text("Nenhuma arena disponível no momento.", NamedTextColor.RED));
            return;
        }
        Kit kit = plugin.getKitManager().getDefault();
        arena.setInUse(true);

        Duel duel = new Duel(p1, p2, arena, kit);
        activeDuels.put(p1.getUniqueId(), duel);
        activeDuels.put(p2.getUniqueId(), duel);

        prepare(p1, arena.getSpawn1(), kit);
        prepare(p2, arena.getSpawn2(), kit);

        runCountdown(duel);
    }

    private void prepare(Player player, Location spawn, Kit kit) {
        player.getInventory().clear();
        player.getInventory().setArmorContents(new ItemStack[4]);
        player.setGameMode(GameMode.SURVIVAL);
        player.setHealth(20.0);
        player.setFoodLevel(20);
        player.setSaturation(20f);
        player.setFireTicks(0);

        for (Map.Entry<Integer, ItemStack> entry : kit.items().entrySet()) {
            player.getInventory().setItem(entry.getKey(), entry.getValue().clone());
        }
        if (kit.helmet() != null) {
            player.getInventory().setHelmet(kit.helmet().clone());
        }
        if (kit.chestplate() != null) {
            player.getInventory().setChestplate(kit.chestplate().clone());
        }
        if (kit.leggings() != null) {
            player.getInventory().setLeggings(kit.leggings().clone());
        }
        if (kit.boots() != null) {
            player.getInventory().setBoots(kit.boots().clone());
        }

        player.teleport(spawn);
    }

    private void runCountdown(Duel duel) {
        new BukkitRunnable() {
            int secondsLeft = 5;

            @Override
            public void run() {
                Player p1 = duel.getPlayer1();
                Player p2 = duel.getPlayer2();
                if (!p1.isOnline() || !p2.isOnline()) {
                    cancel();
                    return;
                }
                if (secondsLeft > 0) {
                    sendTitle(p1, String.valueOf(secondsLeft));
                    sendTitle(p2, String.valueOf(secondsLeft));
                    secondsLeft--;
                } else {
                    sendTitle(p1, "LUTE!");
                    sendTitle(p2, "LUTE!");
                    duel.setPhase(Duel.Phase.FIGHTING);
                    duel.startScoreboard(plugin);
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    public void handleLoss(Player loser) {
        Duel duel = getDuel(loser);
        if (duel == null) {
            return;
        }
        Player winner = duel.getOpponent(loser);
        endDuel(duel, winner, loser);
    }

    public void handleQuit(Player player) {
        Duel duel = getDuel(player);
        if (duel == null) {
            return;
        }
        Player opponent = duel.getOpponent(player);
        endDuel(duel, opponent, player);
    }

    private void endDuel(Duel duel, Player winner, Player loser) {
        activeDuels.remove(duel.getPlayer1().getUniqueId());
        activeDuels.remove(duel.getPlayer2().getUniqueId());
        duel.getArena().setInUse(false);
        duel.stopScoreboard();

        duel.getSnapshot(winner).restore(winner);
        duel.getSnapshot(loser).restore(loser);

        if (winner.isOnline()) {
            sendTitle(winner, "VITÓRIA!");
        }
        if (loser.isOnline()) {
            sendTitle(loser, "DERROTA!");
        }

        plugin.getStatsManager().addWin(winner.getUniqueId());
        plugin.getStatsManager().addLoss(loser.getUniqueId());

        Bukkit.broadcast(Component.text(winner.getName() + " venceu o duelo contra " + loser.getName() + "!", NamedTextColor.GOLD));
    }

    private void sendTitle(Player player, String text) {
        player.showTitle(Title.title(Component.text(text, NamedTextColor.YELLOW), Component.empty()));
    }
}
