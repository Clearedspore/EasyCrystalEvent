package me.clearedspore.easyCrystalEvent.listener;

import me.clearedspore.easyCrystalEvent.util.EventManager;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class HostModeListener implements Listener {
    private final EventManager eventManager;

    public HostModeListener(EventManager eventManager) {
        this.eventManager = eventManager;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event){
        Player player = event.getPlayer();

        if(eventManager.isInHostMode(player) == true){
            event.setCancelled(true);
        }
    }
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event){
        Player player = event.getPlayer();

        if(eventManager.isInHostMode(player) == true){
            event.setCancelled(true);
        }
    }
    @EventHandler
    public void onInteract(PlayerInteractEvent event){
        Player player = event.getPlayer();

        if(eventManager.isInHostMode(player) == true){
            event.setCancelled(true);
        }
    }
    @EventHandler
    public void onRightClickPlayer(PlayerInteractEntityEvent event){
        Player player = event.getPlayer();
        Player target = (Player) event.getRightClicked();



        String tpBackWorldName = eventManager.getPlugin().getConfig().getString("tpbackworld", "world");
        World tpBackWorld = Bukkit.getWorld(tpBackWorldName);

        if (tpBackWorld == null) {
            player.sendMessage("§cThe configured world to teleport back to is not available.");
            return;
        }

        String returnGamemodeString = eventManager.getPlugin().getConfig().getString("returnGamemode", "SURVIVAL");
        GameMode returnGamemode;
        try {
            returnGamemode = GameMode.valueOf(returnGamemodeString.toUpperCase());
        } catch (IllegalArgumentException e) {
            player.sendMessage("§cInvalid game mode in configuration. Defaulting to SURVIVAL.");
            returnGamemode = GameMode.SURVIVAL;
        }

        if(player.getInventory().getItemInHand().getItemMeta().getDisplayName().equalsIgnoreCase("§e§lKick a player")){
            eventManager.restorePlayerInventory(target);
            target.teleport(tpBackWorld.getSpawnLocation());
            eventManager.removeEventPlayer(target);
            target.setGameMode(returnGamemode);
            player.sendMessage("§aYou have kicked " + target.getName());
            target.sendMessage("§cYou have been kicked from the event");

            if(eventManager.isAlivePlayer(target)){
                eventManager.removeAlivePlayer(target);
            }
            if(eventManager.isDeathPlayer(target)){
                eventManager.removeDeathPlayer(target);
            }
        }
    }
}
