package com.brunobeduschi.duelarena.commands;

import com.brunobeduschi.duelarena.DuelArenaPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class DuelCommand implements CommandExecutor, TabCompleter {

    private final DuelArenaPlugin plugin;

    public DuelCommand(DuelArenaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Apenas jogadores podem usar este comando.", NamedTextColor.RED));
            return true;
        }
        if (args.length == 0) {
            player.sendMessage(Component.text("Uso: /duel <jogador|accept|deny|top|stats>", NamedTextColor.RED));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "accept" -> plugin.getDuelManager().acceptRequest(player);
            case "deny" -> plugin.getDuelManager().denyRequest(player);
            case "top" -> showTop(player);
            case "stats" -> showStats(player, args);
            default -> {
                Player target = Bukkit.getPlayerExact(args[0]);
                if (target == null) {
                    player.sendMessage(Component.text("Jogador não encontrado ou offline.", NamedTextColor.RED));
                    return true;
                }
                plugin.getDuelManager().sendRequest(player, target);
            }
        }
        return true;
    }

    private void showTop(Player player) {
        List<Map.Entry<UUID, Integer>> top = plugin.getStatsManager().getTopWins(5);
        player.sendMessage(Component.text("=== Top Duelistas ===", NamedTextColor.GOLD));
        if (top.isEmpty()) {
            player.sendMessage(Component.text("Nenhum duelo registrado ainda.", NamedTextColor.YELLOW));
            return;
        }
        int position = 1;
        for (Map.Entry<UUID, Integer> entry : top) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(entry.getKey());
            String name = offlinePlayer.getName() != null ? offlinePlayer.getName() : "Desconhecido";
            player.sendMessage(Component.text(position + ". " + name + " - " + entry.getValue() + " vitórias", NamedTextColor.YELLOW));
            position++;
        }
    }

    private void showStats(Player player, String[] args) {
        Player target = args.length > 1 ? Bukkit.getPlayerExact(args[1]) : player;
        if (target == null) {
            player.sendMessage(Component.text("Jogador não encontrado ou offline.", NamedTextColor.RED));
            return;
        }
        int wins = plugin.getStatsManager().getWins(target.getUniqueId());
        int losses = plugin.getStatsManager().getLosses(target.getUniqueId());
        player.sendMessage(Component.text(target.getName() + ": " + wins + " vitórias, " + losses + " derrotas", NamedTextColor.AQUA));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> options = new ArrayList<>(List.of("accept", "deny", "top", "stats"));
            Bukkit.getOnlinePlayers().forEach(p -> options.add(p.getName()));
            return options.stream()
                    .filter(option -> option.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}
