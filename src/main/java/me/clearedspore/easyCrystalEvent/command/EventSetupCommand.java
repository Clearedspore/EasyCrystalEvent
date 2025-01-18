package me.clearedspore.easyCrystalEvent.command;

import me.clearedspore.easyCrystalEvent.util.EventManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EventSetupCommand implements CommandExecutor {

    private final EventManager eventManager;

    public EventSetupCommand(EventManager eventManager) {
        this.eventManager = eventManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (eventManager.isEventActive()) {
                player.sendMessage("§cAn event is already active. Please wait for it to finish before starting a new one.");
                return true;
            }



            eventManager.openSetupGUI(player);
            return true;
        }
        return false;
    }
}