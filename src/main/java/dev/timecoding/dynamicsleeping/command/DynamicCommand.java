package dev.timecoding.dynamicsleeping.command;

import dev.timecoding.dynamicsleeping.DynamicSleeping;
import dev.timecoding.dynamicsleeping.config.ConfigHandler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class DynamicCommand implements CommandExecutor{

    private DynamicSleeping plugin;
    private ConfigHandler configHandler;

    public DynamicCommand(DynamicSleeping plugin){
        this.plugin = plugin;
        this.configHandler = this.plugin.getConfigHandler();
    }
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
            if(sender.hasPermission(configHandler.getString("Command-Permission"))){
                if(args.length == 1) {
                    if (args[0].equalsIgnoreCase("rl") || args[0].equalsIgnoreCase("reload")) {
                        configHandler.reload();
                        sender.sendMessage("§aThe config.yml was successfully reloaded!");
                    }else{
                        sender.sendMessage("§c/dynamicsleeping reload");
                    }
                }else{
                    sender.sendMessage("§c/dynamicsleeping reload");
                }
            }else{
                sender.sendMessage("§cYou do not have the permission to use this command!");
            }
        return false;
    }
}
