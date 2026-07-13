package com.brunobeduschi.duelarena;

import com.brunobeduschi.duelarena.arena.ArenaManager;
import com.brunobeduschi.duelarena.commands.ArenaAdminCommand;
import com.brunobeduschi.duelarena.commands.DuelCommand;
import com.brunobeduschi.duelarena.duel.DuelManager;
import com.brunobeduschi.duelarena.kit.KitManager;
import com.brunobeduschi.duelarena.listeners.DuelListener;
import com.brunobeduschi.duelarena.stats.StatsManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class DuelArenaPlugin extends JavaPlugin {

    private ArenaManager arenaManager;
    private KitManager kitManager;
    private StatsManager statsManager;
    private DuelManager duelManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getDataFolder().mkdirs();

        arenaManager = new ArenaManager(this);
        kitManager = new KitManager(this);
        statsManager = new StatsManager(this);
        duelManager = new DuelManager(this);

        getServer().getPluginManager().registerEvents(new DuelListener(this), this);

        getCommand("duel").setExecutor(new DuelCommand(this));
        getCommand("arena").setExecutor(new ArenaAdminCommand(this));
    }

    @Override
    public void onDisable() {
        if (arenaManager != null) {
            arenaManager.save();
        }
    }

    public ArenaManager getArenaManager() {
        return arenaManager;
    }

    public KitManager getKitManager() {
        return kitManager;
    }

    public StatsManager getStatsManager() {
        return statsManager;
    }

    public DuelManager getDuelManager() {
        return duelManager;
    }
}
