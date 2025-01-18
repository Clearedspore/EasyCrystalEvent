package me.clearedspore.easyCrystalEvent.command;

import me.clearedspore.easyCrystalEvent.util.EventManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EventRekitCommand implements CommandExecutor {
    private final EventManager eventManager;

    public EventRekitCommand(EventManager eventManager) {
        this.eventManager = eventManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can rekit other players.");
            return true;
        }

        Player player = (Player) sender;
        if (!player.equals(eventManager.getHost())) {
            player.sendMessage("§cOnly the host can rekit other players.");
            return true;
        }


        if(eventManager.isEventActive() == false){
            player.sendMessage("§cThere is no active event!");
            return true;
        }

        String playername = args[0];
        Player target = Bukkit.getServer().getPlayerExact(playername);

        if(player.equals(eventManager.getHost())) {
            if (eventManager.isAlivePlayer(target) == true) {
                target.getInventory().clear();
                eventManager.giveKit(target);
                player.sendMessage("§ayou have rekitted " + target.getName());
                target.sendMessage("§aYou have recieved a new kit by " + player.getName());
            } else if (!eventManager.isAlivePlayer(target) == true){
                player.sendMessage("§cThis player is not alive and you can't rekit them!");
            }
        }
        return true;
    }
}
