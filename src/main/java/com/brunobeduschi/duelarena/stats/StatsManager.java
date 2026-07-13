package com.brunobeduschi.duelarena.stats;

import com.brunobeduschi.duelarena.DuelArenaPlugin;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class StatsManager {

    private final DuelArenaPlugin plugin;
    private final File file;
    private final YamlConfiguration config;

    public StatsManager(DuelArenaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "stats.yml");
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    public void addWin(UUID uuid) {
        config.set(uuid + ".wins", getWins(uuid) + 1);
        save();
    }

    public void addLoss(UUID uuid) {
        config.set(uuid + ".losses", getLosses(uuid) + 1);
        save();
    }

    public int getWins(UUID uuid) {
        return config.getInt(uuid + ".wins", 0);
    }

    public int getLosses(UUID uuid) {
        return config.getInt(uuid + ".losses", 0);
    }

    public List<Map.Entry<UUID, Integer>> getTopWins(int limit) {
        return config.getKeys(false).stream()
                .map(key -> Map.entry(UUID.fromString(key), config.getInt(key + ".wins", 0)))
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    private void save() {
        try {
            plugin.getDataFolder().mkdirs();
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Falha ao salvar estatísticas: " + e.getMessage());
        }
    }
}
