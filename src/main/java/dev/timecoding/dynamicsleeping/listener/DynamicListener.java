package dev.timecoding.dynamicsleeping.listener;

import dev.timecoding.dynamicsleeping.DynamicSleeping;
import dev.timecoding.dynamicsleeping.api.DynamicScheduler;
import dev.timecoding.dynamicsleeping.api.variables.DoubleOption;
import dev.timecoding.dynamicsleeping.api.variables.DynamicCustom;
import dev.timecoding.dynamicsleeping.config.ConfigHandler;
import dev.timecoding.dynamicsleeping.enums.IfType;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DynamicListener implements Listener {

    private DynamicSleeping plugin;
    private ConfigHandler configHandler;
    private DynamicScheduler scheduler;

    private List<World> asDefaultIsRunning = new ArrayList<>();
    private HashMap<String, World> isEveryTime = new HashMap<>();
    private List<Player> isSleeping = new ArrayList<>();

    public DynamicListener(DynamicSleeping plugin){
        this.plugin = plugin;
        this.configHandler = this.plugin.getConfigHandler();
        this.scheduler = this.plugin.getDynamicScheduler();
    }

    @EventHandler
    public void onPlayerSleep(PlayerBedEnterEvent e){
        Player p = e.getPlayer();
        Integer time = Math.toIntExact(p.getWorld().getTime());
        String messagebase = "Messages.";

        boolean blocksleepingresults = configHandler.getBoolean("SleepOptions.BlockSleepingResults");

        World w = p.getWorld();
        List<String> results = configHandler.getConfig().getStringList("SleepingResults");
        if(!onBlacklist(w)) {
            if (blocksleepingresults || !blocksleepingresults && results.contains(e.getBedEnterResult().toString())) {
                if (blocksleepingresults && !configHandler.getBoolean("SleepOptions.IgnoreMonstersOrOther") && e.getBedEnterResult() != PlayerBedEnterEvent.BedEnterResult.OK) {
                    p.sendMessage(replaceDefaultPlaceholders(configHandler.getString(messagebase + "NotAble"), p).replace("%case_lowercase%", e.getBedEnterResult().toString().toLowerCase()).replace("%case_uppercase%", e.getBedEnterResult().toString().toUpperCase()));
                } else {
                    if (plugin.canStartAnimation(w)) {
                        if(blocksleepingresults){
                            e.setCancelled(true);
                            e.setUseBed(Event.Result.ALLOW);
                        }
                        Integer enda = 1000;
                        if (worldTimeEnabled("TimeToWakeUp")) {
                            enda = worldTimeTicks("TimeToWakeUp");
                        }
                        Integer online = p.getWorld().getPlayers().size();
                        List<String> customs = plugin.filterCustomNames();
                        if (!isSleeping.contains(p)) {
                            isSleeping.add(p);
                        }
                        for (String custom : customs) {
                            DynamicCustom customOption = plugin.getCustom(custom);
                            if (customOption.isEverytime() || !customOption.isEverytime() && !isEveryTime.containsKey(custom)) {
                                if (!customOption.isEverytime() && !isEveryTime.containsKey(custom)) {
                                    isEveryTime.put(custom, w);
                                }
                                IfType ifType = customOption.getIfOption().getType();
                                Integer amount = getAmountWithOption(online, customOption);
                                Integer finalamount = 0;
                                if (ifType == IfType.PERCENTAGE) {
                                    finalamount = calculatePlayerPercentage(online, customOption.getIfOption().getPercentage());
                                }
                                Integer needonline = 0;
                                Integer aresleeping = getPlayerSleepingInWorld(w);
                                if (customOption.getIfOption().getSplittedAmount().size() > 1) {
                                    needonline = customOption.getIfOption().getSplittedAmount().get(1);
                                }
                                if (ifType == IfType.AMOUNT && amount == aresleeping && customOption.isExactly() || ifType == IfType.AMOUNT && aresleeping >= amount && !customOption.isExactly() || ifType == IfType.PERCENTAGE && aresleeping >= finalamount && !customOption.isExactly() || ifType == IfType.PERCENTAGE && aresleeping == finalamount && customOption.isExactly() || ifType == IfType.AMOUNTSPLITTED && aresleeping == amount && needonline == online) {
                                    String base = "Animation.Custom." + custom + ".Increase.";
                                    Integer speed = getAmountOnIncreaseOption(getIncreaseSpeedBase(w), customOption.getIncreaseSpeedOption());
                                    Integer perticks = getAmountOnIncreaseOption(getIncreaseTickBase(w), customOption.getIncreaseTicksOption());

                                    if(messageEnabled("IncreaseEvent")){
                                        for(Player all : w.getPlayers()){
                                            if(customOption.isIncreaseEnabled()){
                                                all.sendMessage(replaceIncrease(getMessage(all, "IncreaseEvent", "Message1"), speed, perticks, p));
                                            }else{
                                                all.sendMessage(replaceIncrease(getMessage(all, "IncreaseEvent", "Message2"), speed, perticks, p));
                                            }
                                        }
                                    }
                                    if (configHandler.getBoolean(base + "Enabled")) {
                                        scheduler.runDynamicAnimation(w.getUID().toString(), custom, speed, perticks, time, enda, w, true);
                                    } else {
                                        base = "Animation.Custom." + custom + ".";
                                        scheduler.runDynamicAnimation(w.getUID().toString(), custom, configHandler.getInteger(base + ".Speed"), configHandler.getInteger(base + ".PerTicks"), time, enda, w, true);
                                    }
                                }
                            }
                        }
                        if (configHandler.getBoolean("Animation.DefaultSpeed.StartWhenSleep")) {
                            String base = "Animation.DefaultSpeed.";
                            if (!asDefaultIsRunning.contains(w) && !scheduler.taskExists(w.getUID().toString())) {
                                asDefaultIsRunning.add(w);
                                scheduler.runDynamicAnimation(w.getUID().toString(), "", configHandler.getInteger(base + "Speed"), configHandler.getInteger(base + "PerTicks"), time, enda, w, true);
                            }
                        }
                    } else if (blocksleepingresults) {
                        e.setCancelled(true);
                        p.sendMessage(replaceDefaultPlaceholders(configHandler.getString(messagebase + ".TooLateForSleep"), p));
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerBedLeave(PlayerBedLeaveEvent e){
        Player p = e.getPlayer();
        isSleeping.remove(p);
        if(!onBlacklist(p.getWorld()) && plugin.canStartAnimation(p.getWorld()) && configHandler.getBoolean("SleepOptions.KeepPlayerInBed")){
            e.setCancelled(true);
        }
    }

    public String replaceIncrease(String string, Integer speed, Integer ticks, Player casep){
        return string.replace("%ticks%", ticks.toString()).replace("%speed%", speed.toString()).replace("%case_player%", casep.getName());
    }

    public boolean messageEnabled(String index){
        String base = "Messages."+index+".";
        if(configHandler.getBoolean(base+"Enabled")){
            return true;
        }
        return false;
    }

    public String getMessage(Player p, String index, String modified){
        String base = "Messages."+index+".";
        if(messageEnabled(index)){
            return replaceDefaultPlaceholders(configHandler.getString(base+modified), p);
        }
        return "";
    }

    public boolean onBlacklist(World w){
        if(configHandler.getBoolean("WorldBlacklist.Enabled")){
            List<String> worlds = configHandler.getConfig().getStringList("WorldBlacklist.Worlds");
            if(configHandler.getBoolean("WorldBlacklist.ToWhitelist")){
                if(!worlds.contains(w.getName())){
                    return true;
                }
            }else if(worlds.contains(w.getName())){
                return true;
            }
        }
        return false;
    }

    public String replaceDefaultPlaceholders(String string, Player p){
        return string.replace("%world%", p.getWorld().getName()).replace("%player%", p.getName());
    }

    public Integer getPlayerSleepingInWorld(World w){
        Integer i = 0;
        for(Player p : isSleeping){
            if(p.getWorld().getUID().toString().equalsIgnoreCase(w.getUID().toString())){
                i++;
            }
        }
        return i;
    }

    public Integer getIncreaseSpeedBase(World world){
         if(scheduler.getSavedSpeed().containsKey(world.getUID().toString())){
             return scheduler.getSavedSpeed().get(world.getUID().toString());
         }else{
             return configHandler.getInteger("Animation.DefaultSpeed.Speed");
         }
    }

    public Integer getIncreaseTickBase(World world){
        if(scheduler.getSavedTick().containsKey(world.getUID().toString())){
            return scheduler.getSavedTick().get(world.getUID().toString());
        }else{
            return configHandler.getInteger("Animation.DefaultSpeed.PerTicks");
        }
    }

    public List<World> getAsDefaultIsRunning() {
        return asDefaultIsRunning;
    }

    public HashMap<String, World> getIsEveryTime() {
        return isEveryTime;
    }

    public Integer calculatePlayerPercentage(Integer value, Integer percentage){
        int percent = (int)(value*(percentage/100.0f));
        return percent;
    }

    public Integer getAmountOnIncreaseOption(Integer base, DoubleOption option){
        Integer integer = 0;
        switch (option.getType()){
            case PERCENTAGE:
                integer = (base+plugin.calculatePercentage((base), option.getPercentage()));
                break;
            case AMOUNT:
                integer = (base+option.getAmount());
                break;
        }
        return integer;
    }

    public Integer getAmountWithOption(Integer online, DynamicCustom option){
        Integer integer = 0;
        IfType ifType = option.getIfOption().getType();
        switch (ifType){
            case AMOUNT:
                integer = option.getIfOption().getAmount();
                break;
            case PERCENTAGE:
                integer = calculatePlayerPercentage(online, option.getIfOption().getPercentage());
                break;
            case AMOUNTSPLITTED:
                integer = option.getIfOption().getSplittedAmount().get(0);
                break;
        }
        return integer;
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


}
