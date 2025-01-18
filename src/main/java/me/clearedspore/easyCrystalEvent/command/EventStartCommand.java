package me.clearedspore.easyCrystalEvent.command;

import me.clearedspore.easyCrystalEvent.util.EventManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EventStartCommand implements CommandExecutor {
    private final EventManager eventManager;

    public EventStartCommand(EventManager eventManager) {
        this.eventManager = eventManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can start the event.");
            return true;
        }

        Player player = (Player) sender;
        if (!player.equals(eventManager.getHost())) {
            player.sendMessage("§cOnly the host can start the event.");
            return true;
        }


        if(eventManager.isEventActive() == false){
            player.sendMessage("§cThere is no active event!");
            return true;
        }

        if (eventManager.isEventStarted()) {
            player.sendMessage("§cThe event has already started.");
            return true;
        }

        eventManager.startCountdownWithTitle();
        player.sendMessage("§aThe event countdown has started!");
        return true;
    }
}

