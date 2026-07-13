package com.brunobeduschi.duelarena.kit;

import org.bukkit.inventory.ItemStack;

import java.util.Map;

public record Kit(String name, Map<Integer, ItemStack> items, ItemStack helmet, ItemStack chestplate,
                   ItemStack leggings, ItemStack boots) {
}
