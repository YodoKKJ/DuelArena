package com.brunobeduschi.duelarena.arena;

import com.brunobeduschi.duelarena.DuelArenaPlugin;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class ArenaManager {

    private final DuelArenaPlugin plugin;
    private final File file;
    private final Map<String, Arena> arenas = new LinkedHashMap<>();

    public ArenaManager(DuelArenaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "arenas.yml");
        load();
    }

    public void load() {
        arenas.clear();
        if (!file.exists()) {
            return;
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = config.getConfigurationSection("arenas");
        if (section == null) {
            return;
        }
        for (String name : section.getKeys(false)) {
            String path = "arenas." + name + ".";
            Arena arena = new Arena(name);
            arena.setSpawn1(config.getLocation(path + "spawn1"));
            arena.setSpawn2(config.getLocation(path + "spawn2"));
            arena.setCorner1(config.getLocation(path + "corner1"));
            arena.setCorner2(config.getLocation(path + "corner2"));
            arenas.put(name.toLowerCase(), arena);
        }
    }

    public void save() {
        YamlConfiguration config = new YamlConfiguration();
        for (Arena arena : arenas.values()) {
            String path = "arenas." + arena.getName() + ".";
            setIfPresent(config, path + "spawn1", arena.getSpawn1());
            setIfPresent(config, path + "spawn2", arena.getSpawn2());
            setIfPresent(config, path + "corner1", arena.getCorner1());
            setIfPresent(config, path + "corner2", arena.getCorner2());
        }
        try {
            plugin.getDataFolder().mkdirs();
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Falha ao salvar arenas: " + e.getMessage());
        }
    }

    private void setIfPresent(YamlConfiguration config, String path, Location location) {
        if (location != null) {
            config.set(path, location);
        }
    }

    public Arena create(String name) {
        Arena arena = new Arena(name);
        arenas.put(name.toLowerCase(), arena);
        save();
        return arena;
    }

    public Arena get(String name) {
        return arenas.get(name.toLowerCase());
    }

    public boolean remove(String name) {
        boolean removed = arenas.remove(name.toLowerCase()) != null;
        if (removed) {
            save();
        }
        return removed;
    }

    public Collection<Arena> getArenas() {
        return arenas.values();
    }

    public Arena findFreeArena() {
        return arenas.values().stream()
                .filter(Arena::isReady)
                .filter(arena -> !arena.isInUse())
                .findFirst()
                .orElse(null);
    }
}
