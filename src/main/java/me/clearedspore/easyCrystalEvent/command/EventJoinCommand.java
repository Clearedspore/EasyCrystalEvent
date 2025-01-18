package me.clearedspore.easyCrystalEvent.command;

import me.clearedspore.easyCrystalEvent.util.EventManager;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EventJoinCommand implements CommandExecutor {
    private final EventManager eventManager;

    public EventJoinCommand(EventManager eventManager) {
        this.eventManager = eventManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can join the event.");
            return true;
        }

        Player player = (Player) sender;
        World eventWorld = Bukkit.getWorld("cpvp_event");

        if (eventWorld == null) {
            player.sendMessage("§cThe event world is not available.");
            return true;
        }

        if(eventManager.isEventActive() == false){
            player.sendMessage("§cThere is no active event!");
            return true;
        }

        eventManager.savePlayerInventory(player);
        player.teleport(eventWorld.getSpawnLocation());
        eventManager.addEventPlayer(player);
        player.setGameMode(GameMode.SURVIVAL);
        if (eventManager.isKitAvailable()) {
            eventManager.giveKit(player);
        } else if (!eventManager.isKitAvailable()){

            Player host = eventManager.getHost();
            if (host != null) {
                host.sendMessage("§cNo kit has been set for the event.");
            }
        }

        return true;
    }
}
