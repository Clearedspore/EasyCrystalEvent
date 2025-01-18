package me.clearedspore.easyCrystalEvent.command;

import me.clearedspore.easyCrystalEvent.util.EventManager;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

public class EventCancelCommand implements CommandExecutor {
    private final EventManager eventManager;

    public EventCancelCommand(EventManager eventManager) {
        this.eventManager = eventManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can cancel the event.");
            return true;
        }

        Player player = (Player) sender;
        if (!player.equals(eventManager.getHost())) {
            player.sendMessage("§cOnly the host can cancel the event.");
            return true;
        }

        if(eventManager.isEventActive() == false){
            player.sendMessage("§cThere is no active event!");
            return true;
        }

        World eventWorld = Bukkit.getWorld("cpvp_event");
        if (eventWorld == null) {
            player.sendMessage("§cThe event world is not available.");
            return true;
        }

        String tpBackWorldName = eventManager.getPlugin().getConfig().getString("tpbackworld", "world");
        World tpBackWorld = Bukkit.getWorld(tpBackWorldName);

        if (tpBackWorld == null) {
            player.sendMessage("§cThe configured world to teleport back to is not available.");
            return true;
        }


        String returnGamemodeString = eventManager.getPlugin().getConfig().getString("returnGamemode", "SURVIVAL");
        GameMode returnGamemode;
        try {
            returnGamemode = GameMode.valueOf(returnGamemodeString.toUpperCase());
        } catch (IllegalArgumentException e) {
            player.sendMessage("§cInvalid game mode in configuration. Defaulting to SURVIVAL.");
            returnGamemode = GameMode.SURVIVAL;
        }

        for (Player eventPlayer : eventManager.getEventPlayers()) {
            eventManager.restorePlayerInventory(eventPlayer);
            eventPlayer.getActivePotionEffects().clear();
            eventPlayer.teleport(tpBackWorld.getSpawnLocation());
            eventPlayer.setGameMode(returnGamemode);
        }
        eventManager.getEventPlayers().clear();
        eventManager.getDeathplayers().clear();
        eventManager.getAliveplayers().clear();
        eventManager.cancelActionBar();
        eventManager.removeHost();
        eventManager.cancelEvent();

        Bukkit.unloadWorld(eventWorld, false);
        Bukkit.getServer().getScheduler().runTaskLater(eventManager.getPlugin(), () -> {
            if (eventWorld.getWorldFolder().exists()) {
                deleteWorldFolder(eventWorld.getWorldFolder());
            }
        }, 20L);

        player.sendMessage("§aThe event has been cancelled and all players have been returned to the configured world.");
        return true;
    }



    private void deleteWorldFolder(java.io.File path) {
        if (path.exists()) {
            for (java.io.File file : path.listFiles()) {
                if (file.isDirectory()) {
                    deleteWorldFolder(file);
                } else {
                    file.delete();
                }
            }
        }
        path.delete();
    }
}