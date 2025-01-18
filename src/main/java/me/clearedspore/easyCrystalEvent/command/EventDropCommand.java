package me.clearedspore.easyCrystalEvent.command;

import me.clearedspore.easyCrystalEvent.util.EventManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EventDropCommand implements CommandExecutor {
    private final EventManager eventManager;

    public EventDropCommand(EventManager eventManager) {
        this.eventManager = eventManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;


        if(eventManager.isEventActive() == false){
            player.sendMessage("§cThere is no active event!");
            return true;
        }

        if (!player.equals(eventManager.getHost())) {
            player.sendMessage("§cOnly the host can execute this command.");
            return true;
        }

        eventManager.clearLayers(player);
        return true;
    }
}
