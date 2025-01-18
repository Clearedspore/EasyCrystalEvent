package me.clearedspore.easyCrystalEvent.command;

import me.clearedspore.easyCrystalEvent.util.EventManager;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EventKickCommand implements CommandExecutor {
    private final EventManager eventManager;

    public EventKickCommand(EventManager eventManager) {
        this.eventManager = eventManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can kick other players");
            return true;
        }

        Player player = (Player) sender;
        if (!player.equals(eventManager.getHost())) {
            player.sendMessage("§cOnly the host can kick other players");
            return true;
        }

        if(eventManager.isEventActive() == false){
            player.sendMessage("§cThere is no active event!");
            return true;
        }

        String playername = args[0];
        Player target = Bukkit.getServer().getPlayerExact(playername);
        StringBuilder builder = new StringBuilder();
        for(int i = 1; i < args.length; i++) {
            builder.append(args[i]);
            builder.append(" ");
        }
        String Reason = builder.toString();
        Reason = Reason.stripTrailing();

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

        if(eventManager.isEventPlayer(target)){
        if(args.length == 1){
                eventManager.restorePlayerInventory(target);
                target.teleport(tpBackWorld.getSpawnLocation());
                eventManager.removeEventPlayer(target);
                target.setGameMode(returnGamemode);
                player.sendMessage("§aYou have kicked " + target.getName());
                target.sendMessage("§cYou have been kicked");

                if(eventManager.isAlivePlayer(target)){
                    eventManager.removeAlivePlayer(target);
                }
                if(eventManager.isDeathPlayer(target)){
                    eventManager.removeDeathPlayer(target);
                }
                return true;
            }
        if(args.length > 1){
            target.teleport(tpBackWorld.getSpawnLocation());
            eventManager.restorePlayerInventory(target);
            eventManager.removeEventPlayer(target);
            target.setGameMode(returnGamemode);
            player.sendMessage("§aYou have kicked " + target.getName() + " Reason: §f" + Reason);
            target.sendMessage("§cYou have been kicked from the event");
            target.sendMessage("§aReason: §f" + Reason);
        }
        }

        return true;
    }
}
