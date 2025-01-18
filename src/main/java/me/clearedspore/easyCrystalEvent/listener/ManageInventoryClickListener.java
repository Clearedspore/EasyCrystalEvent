package me.clearedspore.easyCrystalEvent.listener;

import me.clearedspore.easyCrystalEvent.util.EventManager;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class ManageInventoryClickListener implements Listener {
    private final EventManager eventManager;

    public ManageInventoryClickListener(EventManager eventManager) {
        this.eventManager = eventManager;
    }

    @EventHandler
    public void onClickListener(InventoryClickEvent event) {
        if (event.getView().getTitle().equals("Manage Event")) {
            event.setCancelled(true);
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

            Player player = (Player) event.getWhoClicked();

            switch (clickedItem.getType()) {
                case DROPPER:
                    eventManager.clearLayers(player);
                    player.closeInventory();
                    break;
                case HEART_OF_THE_SEA:
                    eventManager.openBorderGUI(player);
                    break;
                case BARRIER:
                    player.performCommand("cevent-cancel");
                    player.closeInventory();
                    break;
                case RED_BED:
                    eventManager.openRespawn(player);
                    break;
                case ARROW:
                    eventManager.openKickGUI(player);
                    break;
            }
        } else if (event.getView().getTitle().equals("Set the border")) {
            event.setCancelled(true);
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

            Player player = (Player) event.getWhoClicked();


            World eventWorld = Bukkit.getWorld("cpvp_event");
            if (eventWorld == null) {
                player.sendMessage("The event world is not available.");
                return;
            }

            WorldBorder worldBorder = eventWorld.getWorldBorder();
            double currentSize = worldBorder.getSize();

            switch (clickedItem.getType()) {
                case RED_CARPET:
                    eventManager.closeBorder(worldBorder, currentSize, 16, player);
                    break;
                case LIME_CARPET:
                    eventManager.growBorder(worldBorder, currentSize, 16, player);
                    break;
                case ARROW:
                    eventManager.openManageGUI(player);
                    break;
            }
        } else if (event.getView().getTitle().equals("Kick a player")){
            event.setCancelled(true);
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

            Player player = (Player) event.getWhoClicked();


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

            switch (clickedItem.getType()) {
                case PLAYER_HEAD:
                    String playerName = clickedItem.getItemMeta().getDisplayName();
                    Player target = Bukkit.getPlayerExact(playerName);
                    if (target != null) {
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
                    } else {
                        player.sendMessage("§cPlayer " + playerName + " is not online.");
                    }
                    player.closeInventory();
                    break;
            }
        } else if (event.getView().getTitle().equals("Respawn a player")){
            event.setCancelled(true);
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

            Player player = (Player) event.getWhoClicked();
            switch (clickedItem.getType()) {
                case PLAYER_HEAD:
                    String playerName = clickedItem.getItemMeta().getDisplayName();
                    Player target = Bukkit.getPlayerExact(playerName);
                    if (target != null) {
                        if (eventManager.isDeathPlayer(target) == false) {
                            player.sendMessage("§cThis player is not a event player and cannot be respawned!");
                            player.closeInventory();
                            return;
                        }
                        if (eventManager.isDeathPlayer(target) == true) {
                            eventManager.respawnPlayer(target);
                            player.sendMessage("§aYou have respawned " + target.getName());
                            target.sendMessage("§aYou have been respawned by " + player.getName());
                            player.closeInventory();
                        }
                    }
                    break;
            }
        }
    }
}
