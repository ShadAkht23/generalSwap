package com.shard.generalswap.commands;

import com.shard.generalswap.SwapPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.logging.Level;

public class StartCommand implements CommandExecutor, TabCompleter {


    private final SwapPlugin plugin;
    public StartCommand(SwapPlugin p) {
        plugin = p;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        try {
            if (args.length == 0) {
                commandSender.sendMessage("its litearlly just /gswap start");
                return true;
            }
            String subCommand = args[0].toLowerCase();
            if (subCommand.equals("start")) {
                plugin.start();
            } else if (subCommand.equals("stop")) {
                plugin.stop();
            }
            else {
                commandSender.sendMessage("its litearlly just /gswap start");
                return false;
            }

        } catch (Exception e) {
            // Catch any unexpected errors so Bukkit doesn't show the generic message without a stacktrace
            commandSender.sendMessage("§cAn internal error occurred while executing that command. Check server logs for details.");
            plugin.getLogger().log(Level.SEVERE, "Unhandled exception while executing /swap by " + (commandSender == null ? "UNKNOWN" : commandSender.getName()), e);
            return false;
        }
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        return List.of();
    }
}
