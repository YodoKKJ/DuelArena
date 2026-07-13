package com.brunobeduschi.duelarena.duel;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PlayerStateSnapshot {

    private final Location location;
    private final ItemStack[] contents;
    private final ItemStack[] armor;
    private final double health;
    private final int food;
    private final float saturation;
    private final int level;
    private final float exp;
    private final GameMode gameMode;

    public PlayerStateSnapshot(Player player) {
        this.location = player.getLocation().clone();
        this.contents = player.getInventory().getContents().clone();
        this.armor = player.getInventory().getArmorContents().clone();
        this.health = player.getHealth();
        this.food = player.getFoodLevel();
        this.saturation = player.getSaturation();
        this.level = player.getLevel();
        this.exp = player.getExp();
        this.gameMode = player.getGameMode();
    }

    public void restore(Player player) {
        player.getInventory().setContents(contents);
        player.getInventory().setArmorContents(armor);
        player.setGameMode(gameMode);
        player.setHealth(Math.min(health, 20.0));
        player.setFoodLevel(food);
        player.setSaturation(saturation);
        player.setLevel(level);
        player.setExp(exp);
        player.teleport(location);
    }
}
