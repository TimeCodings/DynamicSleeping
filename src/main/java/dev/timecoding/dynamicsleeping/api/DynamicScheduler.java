package dev.timecoding.dynamicsleeping.api;

import dev.timecoding.dynamicsleeping.DynamicSleeping;
import dev.timecoding.dynamicsleeping.config.ConfigHandler;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class DynamicScheduler {

    private DynamicSleeping plugin;
    private ConfigHandler configHandler;
    private HashMap<String, BukkitTask> dynamicmap = new HashMap<>();

    public DynamicScheduler(DynamicSleeping plugin){
        this.plugin = plugin;
        this.configHandler = this.plugin.getConfigHandler();
    }

    public void addTask(String name, BukkitTask task){
        if(!dynamicmap.containsKey(name)){
            dynamicmap.put(name, task);
        }
    }

    public BukkitTask getTask(String name){
        if(taskExists(name)){
            return dynamicmap.get(name);
        }
        return null;
    }

    public void removeTask(String name){
        if(taskExists(name)){
            dynamicmap.remove(name);
        }
    }

    public boolean taskExists(String name){
        if(dynamicmap.containsKey(name)){
            return true;
        }
        return false;
    }

    private HashMap<String, Integer> savedSpeed = new HashMap<>();
    private HashMap<String, Integer> savedTick = new HashMap<>();

    public HashMap<String, Integer> getSavedSpeed() {
        return savedSpeed;
    }

    public HashMap<String, Integer> getSavedTick() {
        return savedTick;
    }

    public void runDynamicAnimation(String name, String custom, Integer speed, Integer ticks, Integer start, Integer end, World world, boolean autodisable){
        BukkitTask task = null;
        if(taskExists(name)){
            getTask(name).cancel();
            removeTask(name);
        }
        if(savedSpeed.containsKey(name)){
            speed = (savedSpeed.get(name)+speed);
            savedSpeed.remove(name);
        }
        if(savedTick.containsKey(name)){
            ticks = (savedTick.get(name)+ticks);
            savedTick.remove(name);
        }
        savedSpeed.put(name, speed);
        savedTick.put(name, ticks);
        Integer finalSpeed = speed;
        Integer finalTicks = ticks;
        task = Bukkit.getScheduler().runTaskTimer(this.plugin, new Runnable() {
            @Override
            public void run() {
                Long updated = plugin.getUpdatedTime(world.getTime(), finalSpeed.longValue());
                world.setTime(updated);
                if(autodisable){
                    if(!plugin.canStartAnimation(world)){
                        if(messageEnabled("TimeToWakeUp")) {
                            for (Player all : world.getPlayers()) {
                                all.sendMessage(getMessage(all, "TimeToWakeUp"));
                            }
                        }
                        savedSpeed.remove(name);
                        savedTick.remove(name);
                        plugin.getListener().getAsDefaultIsRunning().remove(world);
                        removeAllCustomsFromWorld(world);
                        getTask(name).cancel();
                        removeTask(name);
                    }
                }
            }
        } ,0, ticks);
        addTask(name, task);
    }

    public void removeAllCustomsFromWorld(World w){
        HashMap<String, World> map = plugin.getListener().getIsEveryTime();
        Set<Map.Entry<String, World>> entrySet = map.entrySet();
        Iterator<Map.Entry<String, World>> itr = entrySet.iterator();
        while(itr.hasNext()){
            Map.Entry<String, World> entry = itr.next();
            String key = entry.getKey();
            World value = entry.getValue();
            if(value.equals(w)){
                itr.remove();
            }
        }
    }

    private List<Player> sended = new ArrayList<>();

    public void runAutoAnimationChecker(){
        Integer speed = configHandler.getInteger("Animation.DefaultSpeed.Speed");
        Integer ticks = configHandler.getInteger("Animation.DefaultSpeed.PerTicks");
        BukkitTask task;
        task = Bukkit.getScheduler().runTaskTimer(this.plugin, new Runnable() {
            @Override
            public void run() {
                for(Player all : Bukkit.getOnlinePlayers()){
                    World world = all.getWorld();
                    Long newtime = plugin.getUpdatedTime(world.getTime(), speed.longValue());
                    if(plugin.canStartAnimation(world)){
                        if(configHandler.getBoolean("Animation.DefaultSpeed.AutoAnimation")) {
                            world.setTime(newtime);
                        }
                        if(!sended.contains(all) && messageEnabled("TimeToSleep")){
                            sended.add(all);
                            all.sendMessage(getMessage(all, "TimeToSleep"));
                        }
                    }else{
                        if(sended.contains(all) && messageEnabled("TooLateForAll") && plugin.tooLateForSleep(Integer.valueOf((int) world.getTime()))){
                            all.sendMessage(getMessage(all, "TooLateForAll"));
                        }
                        sended.remove(all);
                    }
                }
            }
        } ,0, ticks);
        addTask("AA", task);
    }

    public boolean messageEnabled(String index){
        String base = "Messages."+index+".";
        if(configHandler.getBoolean(base+"Enabled")){
            return true;
        }
        return false;
    }

    public String getMessage(Player p, String index){
        String base = "Messages."+index+".";
        if(messageEnabled(index)){
            return replaceDefaultPlaceholders(configHandler.getString(base+"Message"), p);
        }
        return "";
    }

    public String replaceDefaultPlaceholders(String string, Player p){
        return string.replace("%world%", p.getWorld().getName()).replace("%player%", p.getName());
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
