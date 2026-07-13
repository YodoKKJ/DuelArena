package com.brunobeduschi.duelarena.commands;

import com.brunobeduschi.duelarena.DuelArenaPlugin;
import com.brunobeduschi.duelarena.arena.Arena;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class ArenaAdminCommand implements CommandExecutor, TabCompleter {

    private static final List<String> ACTIONS = List.of(
            "create", "setspawn1", "setspawn2", "setbounds1", "setbounds2", "remove", "list");

    private final DuelArenaPlugin plugin;

    public ArenaAdminCommand(DuelArenaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Apenas jogadores podem usar este comando.", NamedTextColor.RED));
            return true;
        }
        if (args.length < 1) {
            player.sendMessage(Component.text("Uso: /arena <create|setspawn1|setspawn2|setbounds1|setbounds2|remove|list> <nome>", NamedTextColor.RED));
            return true;
        }

        String action = args[0].toLowerCase();

        if (action.equals("list")) {
            String names = plugin.getArenaManager().getArenas().stream()
                    .map(Arena::getName)
                    .collect(Collectors.joining(", "));
            player.sendMessage(Component.text("Arenas: " + (names.isEmpty() ? "nenhuma" : names), NamedTextColor.AQUA));
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(Component.text("Uso: /arena " + action + " <nome>", NamedTextColor.RED));
            return true;
        }
        String name = args[1];

        switch (action) {
            case "create" -> {
                if (plugin.getArenaManager().get(name) != null) {
                    player.sendMessage(Component.text("Já existe uma arena com esse nome.", NamedTextColor.RED));
                    return true;
                }
                plugin.getArenaManager().create(name);
                player.sendMessage(Component.text("Arena '" + name + "' criada. Configure com /arena setspawn1/setspawn2.", NamedTextColor.GREEN));
            }
            case "setspawn1", "setspawn2", "setbounds1", "setbounds2" -> {
                Arena arena = plugin.getArenaManager().get(name);
                if (arena == null) {
                    player.sendMessage(Component.text("Arena não encontrada.", NamedTextColor.RED));
                    return true;
                }
                switch (action) {
                    case "setspawn1" -> arena.setSpawn1(player.getLocation());
                    case "setspawn2" -> arena.setSpawn2(player.getLocation());
                    case "setbounds1" -> arena.setCorner1(player.getLocation());
                    case "setbounds2" -> arena.setCorner2(player.getLocation());
                }
                plugin.getArenaManager().save();
                player.sendMessage(Component.text("Ponto definido para a arena '" + name + "'.", NamedTextColor.GREEN));
            }
            case "remove" -> {
                boolean removed = plugin.getArenaManager().remove(name);
                player.sendMessage(removed
                        ? Component.text("Arena removida.", NamedTextColor.GREEN)
                        : Component.text("Arena não encontrada.", NamedTextColor.RED));
            }
            default -> player.sendMessage(Component.text("Ação inválida.", NamedTextColor.RED));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return ACTIONS.stream()
                    .filter(option -> option.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length == 2) {
            return plugin.getArenaManager().getArenas().stream()
                    .map(Arena::getName)
                    .filter(n -> n.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}
