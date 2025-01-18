package me.clearedspore.easyCrystalEvent.command;

import me.clearedspore.easyCrystalEvent.util.EventManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EventRespawnCommand implements CommandExecutor {
    private final EventManager eventManager;

    public EventRespawnCommand(EventManager eventManager) {
        this.eventManager = eventManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can respawn other players.");
            return true;
        }

        Player player = (Player) sender;
        if (!player.equals(eventManager.getHost())) {
            player.sendMessage("§cOnly the host can respawn other players.");
            return true;
        }


        if(eventManager.isEventActive() == false){
            player.sendMessage("§cThere is no active event!");
            return true;
        }

        String playername = args[0];
        Player target = Bukkit.getServer().getPlayerExact(playername);
      if(player.equals(eventManager.getHost())) {
          if (args.length == 1) {
              if (eventManager.isDeathPlayer(target) == false) {
                  player.sendMessage("§cThis player is not a event player and cannot be respawned!");
                  return true;
              }
              if (eventManager.isDeathPlayer(target) == true) {
                  eventManager.respawnPlayer(target);
                  player.sendMessage("§aYou have respawned " + target.getName());
                  target.sendMessage("§aYou have been respawned by " + player.getName());
              }
          }
      }
        return true;
    }
}
