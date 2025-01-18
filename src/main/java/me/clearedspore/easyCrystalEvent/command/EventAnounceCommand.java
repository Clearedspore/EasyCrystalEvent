package me.clearedspore.easyCrystalEvent.command;

import me.clearedspore.easyCrystalEvent.util.EventManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class EventAnounceCommand implements CommandExecutor {
    private final EventManager eventManager;
    private final HashMap<UUID, Long> lastAnnounceTime = new HashMap<>();

    public EventAnounceCommand(EventManager eventManager) {
        this.eventManager = eventManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;
        UUID playerId = player.getUniqueId();
           int cooldown = eventManager.getPlugin().getConfig().getInt("announceCooldown", 60);
           long currentTime = System.currentTimeMillis();
           long lastTime = lastAnnounceTime.getOrDefault(playerId, 0L);
           if ((currentTime - lastTime) < cooldown * 1000) {
               long timeLeft = (cooldown * 1000 - (currentTime - lastTime)) / 1000;
               player.sendMessage("§cYou must wait " + timeLeft + " seconds before announcing the event");
               return true;

           }
               if (!(sender instanceof Player)) {
                   sender.sendMessage("§cOnly players can kick other players");
                   return true;
               }

               if(eventManager.isEventActive() == false){
                   player.sendMessage("§cNo active event to announce!");
                   return true;
               }

               if(eventManager.isEventStarted() == true){
                   player.sendMessage("§cEvent has already started!");
                   return true;
               }

               if (!player.equals(eventManager.getHost())) {
                   player.sendMessage("§cOnly the host can kick other players");
                   return true;
               }
               if (player.equals(eventManager.getHost())) {
                   eventManager.announceEvent(player);
                   lastAnnounceTime.put(playerId, currentTime);
               }

        return true;
    }
}