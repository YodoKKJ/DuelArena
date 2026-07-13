package com.brunobeduschi.duelarena.listeners;

import com.brunobeduschi.duelarena.DuelArenaPlugin;
import com.brunobeduschi.duelarena.arena.Arena;
import com.brunobeduschi.duelarena.duel.Duel;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class DuelListener implements Listener {

    private final DuelArenaPlugin plugin;

    public DuelListener(DuelArenaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        Duel duel = plugin.getDuelManager().getDuel(player);
        if (duel == null) {
            return;
        }
        if (duel.getPhase() == Duel.Phase.COUNTDOWN) {
            event.setCancelled(true);
            return;
        }
        if (event.getFinalDamage() >= player.getHealth()) {
            event.setCancelled(true);
            plugin.getDuelManager().handleLoss(player);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.getDuelManager().handleQuit(event.getPlayer());
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Duel duel = plugin.getDuelManager().getDuel(player);
        if (duel == null) {
            return;
        }

        if (duel.getPhase() == Duel.Phase.COUNTDOWN) {
            if (hasMoved(event)) {
                event.setTo(event.getFrom());
            }
            return;
        }

        Arena arena = duel.getArena();
        if (arena.hasBounds() && !arena.isWithinBounds(player.getLocation())) {
            Location spawn = player.equals(duel.getPlayer1()) ? arena.getSpawn1() : arena.getSpawn2();
            player.teleport(spawn);
        }
    }

    private boolean hasMoved(PlayerMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();
        return to != null && (from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ());
    }
}
