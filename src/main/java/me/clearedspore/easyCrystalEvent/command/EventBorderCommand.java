package me.clearedspore.easyCrystalEvent.command;

import me.clearedspore.easyCrystalEvent.util.EventManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EventBorderCommand implements CommandExecutor, TabCompleter {
    private final EventManager eventManager;

    public EventBorderCommand(EventManager eventManager) {
        this.eventManager = eventManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (!sender.equals(eventManager.getHost())) {
            sender.sendMessage("§cOnly the host can change the border");
            return true;
        }

        if (args.length != 2) {
            sender.sendMessage("Usage: /event-border <close|grow> <blocks>");
            return true;
        }

        Player player = (Player) sender;
        String action = args[0];
        int blocks;

        if(eventManager.isEventActive() == false){
            player.sendMessage("§cThere is no active event!");
            return true;
        }

        try {
            blocks = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage("The number of blocks must be an integer.");
            return true;
        }

        World eventWorld = Bukkit.getWorld("cpvp_event");
        if (eventWorld == null) {
            player.sendMessage("The event world is not available.");
            return true;
        }

        WorldBorder worldBorder = eventWorld.getWorldBorder();
        double currentSize = worldBorder.getSize();

        switch (action.toLowerCase()) {
            case "close":
                eventManager.closeBorder(worldBorder, currentSize, blocks, player);
                break;
            case "grow":
                eventManager.growBorder(worldBorder, currentSize, blocks, player);
                break;
            default:
                player.sendMessage("Invalid action. Use 'close' or 'grow'.");
                break;
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("close", "grow");
        }
        return new ArrayList<>();
    }
}
