package com.brunobeduschi.duelarena.kit;

import com.brunobeduschi.duelarena.DuelArenaPlugin;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class KitManager {

    private final DuelArenaPlugin plugin;
    private final Map<String, Kit> kits = new HashMap<>();

    public KitManager(DuelArenaPlugin plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        kits.clear();
        ConfigurationSection kitsSection = plugin.getConfig().getConfigurationSection("kits");
        if (kitsSection == null) {
            return;
        }
        for (String name : kitsSection.getKeys(false)) {
            ConfigurationSection kitSection = kitsSection.getConfigurationSection(name);
            if (kitSection == null) {
                continue;
            }

            Map<Integer, ItemStack> items = new HashMap<>();
            ConfigurationSection itemsSection = kitSection.getConfigurationSection("items");
            if (itemsSection != null) {
                for (String slotKey : itemsSection.getKeys(false)) {
                    ConfigurationSection itemSection = itemsSection.getConfigurationSection(slotKey);
                    if (itemSection == null) {
                        continue;
                    }
                    Material material = Material.matchMaterial(itemSection.getString("material", "STONE"));
                    if (material == null) {
                        continue;
                    }
                    int amount = itemSection.getInt("amount", 1);
                    try {
                        items.put(Integer.parseInt(slotKey), new ItemStack(material, amount));
                    } catch (NumberFormatException ignored) {
                    }
                }
            }

            ConfigurationSection armorSection = kitSection.getConfigurationSection("armor");
            ItemStack helmet = readArmorPiece(armorSection, "helmet");
            ItemStack chestplate = readArmorPiece(armorSection, "chestplate");
            ItemStack leggings = readArmorPiece(armorSection, "leggings");
            ItemStack boots = readArmorPiece(armorSection, "boots");

            kits.put(name.toLowerCase(), new Kit(name, items, helmet, chestplate, leggings, boots));
        }
    }

    private ItemStack readArmorPiece(ConfigurationSection section, String key) {
        if (section == null) {
            return null;
        }
        String materialName = section.getString(key);
        if (materialName == null || materialName.equalsIgnoreCase("NONE")) {
            return null;
        }
        Material material = Material.matchMaterial(materialName);
        return material == null ? null : new ItemStack(material);
    }

    public Kit get(String name) {
        return kits.get(name.toLowerCase());
    }

    public Kit getDefault() {
        return get(plugin.getConfig().getString("default-kit", "guerreiro"));
    }
}
