package me.clearedspore.easyCrystalEvent.command;

import me.clearedspore.easyCrystalEvent.util.EventManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EventManageCommand implements CommandExecutor {
    private final EventManager eventManager;

    public EventManageCommand(EventManager eventManager) {
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

        eventManager.openManageGUI(player);


        return true;
    }
}
