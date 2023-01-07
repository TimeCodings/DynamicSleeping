package dev.timecoding.dynamicsleeping;

import dev.timecoding.dynamicsleeping.api.DynamicScheduler;
import dev.timecoding.dynamicsleeping.api.Metrics;
import dev.timecoding.dynamicsleeping.api.variables.DynamicCustom;
import dev.timecoding.dynamicsleeping.command.DynamicCommand;
import dev.timecoding.dynamicsleeping.command.completer.DynamicCompleter;
import dev.timecoding.dynamicsleeping.config.ConfigHandler;
import dev.timecoding.dynamicsleeping.listener.DynamicListener;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class DynamicSleeping extends JavaPlugin {

    private ConfigHandler configHandler;
    private DynamicScheduler dynamicScheduler;
    private DynamicListener listener;
    private Metrics metrics;

    @Override
    public void onEnable() {
        ConsoleCommandSender sender = this.getServer().getConsoleSender();
        this.configHandler = new ConfigHandler(this);
        this.configHandler.init();
        if(this.configHandler.getBoolean("Enabled")) {
            sender.sendMessage("§eDynamicSleeping §cv" + this.getDescription().getVersion() + " §agot enabled!");
            this.dynamicScheduler = new DynamicScheduler(this);
            this.dynamicScheduler.runAutoAnimationChecker();

            if (configHandler.getBoolean("bStats")) {
                this.metrics = new Metrics(this, 17322);
            }

            this.listener = new DynamicListener(this);

            PluginManager pluginManager = this.getServer().getPluginManager();
            pluginManager.registerEvents(this.listener, this);

            PluginCommand command = this.getCommand("dynamicsleeping");
            command.setExecutor(new DynamicCommand(this));
            command.setTabCompleter(new DynamicCompleter(this));
        }else{
            sender.sendMessage("§cThe plugin got disabled, because someone disabled the plugin in the config.yml!");
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    public DynamicListener getListener() {
        return listener;
    }

    public DynamicScheduler getDynamicScheduler() {
        return dynamicScheduler;
    }

    public ConfigHandler getConfigHandler() {
        return configHandler;
    }

    public boolean canStartAnimation(World world){
        Integer first = 13000;
        Integer second = 0;
        if(worldTimeEnabled("TimeToSleep")){
            first = worldTimeTicks("TimeToSleep");
        }
        if(worldTimeEnabled("TimeToWakeUp")){
            second = worldTimeTicks("TimeToWakeUp");
        }
        if(betweenBoth(first, second, Integer.valueOf((int) world.getTime()))){
            return true;
        }
        return false;
    }

    public boolean worldTimeEnabled(String s){
        if(configHandler.getBoolean("WorldTime."+s+".Enabled")){
            return true;
        }
        return false;
    }

    public Integer worldTimeTicks(String s){
        return configHandler.getInteger("WorldTime."+s+".Ticks");
    }

    public Long getUpdatedTime(Long time, Long added){
        Long finallong = (time+added);
        return finallong;
    }

    public List<String> filterCustomNames(){
        Map<String, Object> map = configHandler.getConfig().getValues(true);
        List<String> list = new ArrayList<>();
        for(String key : map.keySet()){
            if(key.startsWith("Animation.Custom.")){
                String after = key.split("\\.")[2];
                if(!list.contains(after)){
                    list.add(after);
                }
            }
        }
        return list;
    }

    public Integer calculatePercentage(Integer base, Integer percentage){
        return Integer.valueOf((int) (base+(percentage/100d)*base));
    }

    public DynamicCustom getCustom(String name){
        String base = "Animation.Custom."+name+".";
        DynamicCustom dynamicCustom = new DynamicCustom(configHandler.getString(base+"If"), configHandler.getBoolean(base+"Exactly"), configHandler.getBoolean(base+"EveryTime"), configHandler.getBoolean(base+"Increase.Enabled"), configHandler.getString(base+"Increase.AddSpeed"), configHandler.getString(base+"Increase.AddTicks"), configHandler.getInteger(base+"ChangeSpeed"), configHandler.getInteger(base+"PerTicks"));
        return dynamicCustom;
    }

    public boolean tooLateForSleep(Integer value){
        if(worldTimeEnabled("TooLateForSleep")){
            Integer first = 13000;
            Integer second = 0;
            if(worldTimeEnabled("TimeToSleep")){
                first = worldTimeTicks("TimeToSleep");
            }
            if(worldTimeEnabled("TooLateForSleep")){
                second = worldTimeTicks("TooLateForSleep");
            }
            if(!betweenBoth(first, second, value)){
                return true;
            }
        }
        return false;
    }

    public boolean betweenBoth(Integer first, Integer second, Integer value){
        if(second == first){
            first = first-1;
        }
        if(second > first){
            for(int i = first; first <= second; i++){
                if(i == value){
                    return true;
                }
            }
        }else if(second < first) {
            if (second <= 24000 && value >= first) {
                return true;
            }
            for (int i2 = 0; i2 <= second; i2++) {
                if (i2 == value) {
                    return true;
                }
            }
        }
        return false;
    }
}
