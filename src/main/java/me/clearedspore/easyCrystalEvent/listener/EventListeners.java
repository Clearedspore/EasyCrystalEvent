package me.clearedspore.easyCrystalEvent.listener;

import me.clearedspore.easyCrystalEvent.util.EventManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;

public class EventListeners implements Listener {

    private final EventManager eventManager;

    public EventListeners(EventManager eventManager) {
        this.eventManager = eventManager;
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        String fromWorld = event.getFrom().getWorld().getName();
        String toWorld = event.getTo().getWorld().getName();

        String returnGamemodeString = eventManager.getPlugin().getConfig().getString("returnGamemode", "SURVIVAL");
        GameMode returnGamemode;
        try {
            returnGamemode = GameMode.valueOf(returnGamemodeString.toUpperCase());
        } catch (IllegalArgumentException e) {
            player.sendMessage("§cInvalid game mode in configuration. Defaulting to SURVIVAL.");
            returnGamemode = GameMode.SURVIVAL;
        }

        if (!fromWorld.equals(toWorld)) {
            if (toWorld.equals("cpvp_event")) {
                eventManager.savePlayerInventory(player);
                if (!eventManager.isEventStarted()) {
                    eventManager.giveKit(player);
                    eventManager.addEventPlayer(player);
                    eventManager.setupScoreboard(player);
                    for (Player online : eventManager.getEventPlayers()) {
                        online.sendMessage("§a" + player.getName() + " has joined the event");
                    }
                } else {
                    player.setGameMode(GameMode.SPECTATOR);
                    for (Player online : eventManager.getEventPlayers()) {
                        online.sendMessage("§a" + player.getName() + " has joined and will be watching");
                    }
                }
            } else if (fromWorld.equals("cpvp_event")) {
                eventManager.restorePlayerInventory(player);
                eventManager.removeEventPlayer(player);
                player.getActivePotionEffects().clear();
                player.setGameMode(returnGamemode);
                if (player.equals(eventManager.getHost())) {
                    eventManager.removeHost();
                }
                for (Player online : eventManager.getEventPlayers()) {
                    online.sendMessage("§a" + player.getName() + " has left the event");
                }
            }
            return;
        }
    }

    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent event){
        Player player = event.getPlayer();

        if(eventManager.isEventPlayer(player) || eventManager.isDeathPlayer(player) || eventManager.isAlivePlayer(player)) {
            World eventWorld = Bukkit.getWorld("cpvp_event");
            if (eventWorld == null) {
                player.sendMessage("§cThe event world is not available.");
                return;
            }

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

            for (Player eventPlayer : eventManager.getEventPlayers()) {
                eventManager.restorePlayerInventory(eventPlayer);
                player.getActivePotionEffects().clear();
                eventPlayer.teleport(tpBackWorld.getSpawnLocation());
                eventPlayer.setGameMode(returnGamemode);
            }
        }
    }

    @EventHandler
    private void onPlayerDeathEvent(PlayerDeathEvent event) {
        Player player = event.getEntity();

        if (eventManager.isEventPlayer(player) || player.equals(eventManager.getHost()) || eventManager.isAlivePlayer(player)) {
            event.setKeepInventory(false);
            event.setKeepLevel(false);

            eventManager.removeAlivePlayer(player);
            eventManager.addDeathPlayer(player);

            event.getDrops().clear();
            player.getInventory().forEach(item -> {
                if (item != null) {
                    player.getWorld().dropItemNaturally(player.getLocation(), item);
                }
            });


            player.getInventory().clear();
            World eventWorld = Bukkit.getWorld("cpvp_event");

            Bukkit.getScheduler().runTaskLater(eventManager.getPlugin(), () -> {
                player.setGameMode(GameMode.SPECTATOR);
                player.teleport(eventWorld.getSpawnLocation());
                eventManager.checkForWinner();
            }, 1L);


        }
    }

    @EventHandler
    private void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        if (eventManager.isDeathPlayer(player)) {

            World eventWorld = Bukkit.getWorld("cpvp_event");
            if (eventWorld != null) {

                event.setRespawnLocation(eventWorld.getSpawnLocation());
            }

            Bukkit.getScheduler().runTaskLater(eventManager.getPlugin(), () -> {
                player.setGameMode(GameMode.SPECTATOR);
            }, 1L);
        }
    }
    @EventHandler
    private void onBlockBreak(BlockBreakEvent event){
        Player player = event.getPlayer();
        if (eventManager.isEventPlayer(player)) {
            if (eventManager.isEventStarted() == false) {
                event.setCancelled(true);
            }
        }
    }
    @EventHandler
    private void onBlockPlace(BlockPlaceEvent event){
        Player player = event.getPlayer();
        if (eventManager.isEventPlayer(player)) {
            if (eventManager.isEventStarted() == false) {
                event.setCancelled(true);
            }
        }
    }
    @EventHandler
    private void onPvp(EntityDamageByEntityEvent event){
        Player player = (Player) event.getDamager();
        if (eventManager.isEventPlayer(player)) {
            if (eventManager.isEventStarted() == false) {
                event.setCancelled(true);
            }
        }
    }
    @EventHandler
    private void onInteract(PlayerInteractEvent event){
        Player player = event.getPlayer();
        if (eventManager.isEventPlayer(player)) {
            if (eventManager.isEventStarted() == false) {
                event.setCancelled(true);
            }
        }
    }
}