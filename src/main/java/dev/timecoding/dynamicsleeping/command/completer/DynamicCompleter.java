package dev.timecoding.dynamicsleeping.command.completer;

import dev.timecoding.dynamicsleeping.DynamicSleeping;
import dev.timecoding.dynamicsleeping.config.ConfigHandler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DynamicCompleter implements TabCompleter {

    private DynamicSleeping plugin;
    private ConfigHandler configHandler;

    public DynamicCompleter(DynamicSleeping plugin){
        this.plugin = plugin;
        this.configHandler = this.plugin.getConfigHandler();
    }
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(command.getName().equalsIgnoreCase("dynamicsleeping") || command.getName().equalsIgnoreCase("dyns")){
            if(args.length == 1 && sender.hasPermission(configHandler.getString("Command-Permission"))){
                List<String> list = new ArrayList<>();
                list.add("reload");
                return list;
            }
        }
        return new ArrayList<>();
    }
}
