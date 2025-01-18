package me.clearedspore.easyCrystalEvent;

import me.clearedspore.easyCrystalEvent.command.*;
import me.clearedspore.easyCrystalEvent.listener.EventListeners;
import me.clearedspore.easyCrystalEvent.listener.HostModeListener;
import me.clearedspore.easyCrystalEvent.listener.InventoryClickListener;
import me.clearedspore.easyCrystalEvent.listener.ManageInventoryClickListener;
import me.clearedspore.easyCrystalEvent.util.EventManager;
import org.bukkit.event.server.BroadcastMessageEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class EasyCrystalEvent extends JavaPlugin {

    private EventManager eventManager;

    @Override
    public void onEnable() {

        getConfig().options().copyDefaults();
        saveDefaultConfig();

        eventManager = new EventManager(this);
        getCommand("cevent-setup").setExecutor(new EventSetupCommand(eventManager));
        getCommand("cevent-join").setExecutor(new EventJoinCommand(eventManager));
        getCommand("cevent-cancel").setExecutor(new EventCancelCommand(eventManager));
        getCommand("cevent-kick").setExecutor(new EventKickCommand(eventManager));
        getCommand("cevent-start").setExecutor(new EventStartCommand(eventManager));
        getCommand("cevent-kick").setTabCompleter(new EventTargetTabCompleter(eventManager));
        getCommand("cevent-respawn").setExecutor(new EventRespawnCommand(eventManager));
        getCommand("cevent-respawn").setTabCompleter(new EventTargetTabCompleter(eventManager));
        getCommand("cevent-rekit").setExecutor(new EventRekitCommand(eventManager));
        getCommand("cevent-rekit").setTabCompleter(new EventTargetTabCompleter(eventManager));
        getCommand("cevent-announce").setExecutor(new EventAnounceCommand(eventManager));
        getCommand("cevent-border").setExecutor(new EventBorderCommand(eventManager));
        getCommand("cevent-border").setTabCompleter(new EventBorderCommand(eventManager));
        getCommand("cevent-clearlayers").setExecutor(new EventDropCommand(eventManager));
        getCommand("cevent-manage").setExecutor(new EventManageCommand(eventManager));
        getServer().getPluginManager().registerEvents(new InventoryClickListener(eventManager), this);
        getServer().getPluginManager().registerEvents(new EventListeners(eventManager), this);
        getServer().getPluginManager().registerEvents(new ManageInventoryClickListener(eventManager), this);
        getServer().getPluginManager().registerEvents(new HostModeListener(eventManager), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
