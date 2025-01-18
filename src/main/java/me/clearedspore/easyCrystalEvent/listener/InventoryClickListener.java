package me.clearedspore.easyCrystalEvent.listener;

import me.clearedspore.easyCrystalEvent.util.EventManager;
import me.clearedspore.easyCrystalEvent.util.PlayerInventoryData;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import java.io.File;

public class InventoryClickListener implements Listener {

    private final EventManager eventManager;

    public InventoryClickListener(EventManager eventManager) {
        this.eventManager = eventManager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals("Event Setup")) {
            event.setCancelled(true);
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

            Player player = (Player) event.getWhoClicked();

            switch (clickedItem.getType()) {
                case GRASS_BLOCK:
                    eventManager.openSetWorldGUI(player);
                    break;
                case CHEST:
                    if (event.isLeftClick()) {
                        eventManager.saveKit(player);
                        player.sendMessage("§aKit selected.");
                    } else if (event.isRightClick()) {
                        player.sendMessage("§aOpening kit preview...");
                        eventManager.OpenKitPreview(player);
                    }
                    break;
                case OAK_SIGN:
                    eventManager.openOptionsGUI(player);
                    break;
                case EMERALD_BLOCK:
                    eventManager.confirmWorldCreation(player);
                    break;
            }
        } else if (event.getView().getTitle().equals("Event Options")) {
            event.setCancelled(true);
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

            Player player = (Player) event.getWhoClicked();

            switch (clickedItem.getType()) {
                case STONE:
                    toggleNaturalBlockDrop(player);
                    break;
                case BARRIER:
                    player.closeInventory();
                    player.sendMessage("§aPlease type the default border size in chat.");

                    break;
                case ANVIL:
                    toggleItemDamage(player);
                    break;
                case ELYTRA:
                    toggleElytra(player);
                    break;
                case POTION:
                    eventManager.openPermanentEffectGUI(player);
                    break;
                case ARROW:
                    eventManager.openSetupGUI(player);
                    break;
            }
        }
    }

    @EventHandler
    public void onInventoryClickgeneration(InventoryClickEvent event) {
        if (event.getView().getTitle().equals("World Generation Setup")) {
            event.setCancelled(true);
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

            Player player = (Player) event.getWhoClicked();

            switch (clickedItem.getType()) {
                case GRASS_BLOCK:
                    eventManager.setSelectedWorldType(player, "NORMAL");
                    player.sendMessage("§aNormal world generation selected.");
                    break;
                case DIRT:
                    eventManager.setSelectedWorldType(player, "FLAT");
                    player.sendMessage("§eFlat world generation selected.");
                    break;
                case SAND:
                    eventManager.setSelectedWorldType(player, "SAND");
                    player.sendMessage("§7Sand world generation selected.");
                    break;
            }
            eventManager.openSetupGUI(player);
        }
    }

    private void toggleNaturalBlockDrop(Player player) {
        boolean currentSetting = eventManager.getSetting("naturalBlockDrop", false);
        boolean newSetting = !currentSetting;
        eventManager.saveSetting("naturalBlockDrop", newSetting);
        player.sendMessage("§aNatural Block Drop is now " + (newSetting ? "enabled" : "disabled") + ".");
        eventManager.openOptionsGUI(player);
    }

    private void toggleItemDamage(Player player) {
        boolean currentSetting = eventManager.getSetting("itemDamage", true);
        boolean newSetting = !currentSetting;
        eventManager.saveSetting("itemDamage", newSetting);
        player.sendMessage("§aItem Damage is now " + (newSetting ? "enabled" : "disabled") + ".");
        eventManager.openOptionsGUI(player);
    }

    private void toggleElytra(Player player) {
        boolean currentSetting = eventManager.getSetting("elytra", true);
        boolean newSetting = !currentSetting;
        eventManager.saveSetting("elytra", newSetting);
        player.sendMessage("§aElytra usage is now " + (newSetting ? "allowed" : "disallowed") + ".");
        eventManager.openOptionsGUI(player);
    }

    @EventHandler
    public void onEntityToggleGlide(EntityToggleGlideEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (eventManager.isEventPlayer(player) && !eventManager.getSetting("elytra", true)) {
                event.setCancelled(true);
                player.sendMessage("§cElytra usage is disabled.");
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (eventManager.isEventPlayer(player) && !eventManager.getSetting("naturalBlockDrop", false)) {
            event.setDropItems(false);
        }
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        if (!eventManager.getSetting("naturalBlockDrop", false)) {
            event.blockList().clear();
        }
    }

    @EventHandler
    public void onInventoryClickListener(InventoryClickEvent event) {
        if (event.getView().getTitle().equals("Permanent Effects")) {
            event.setCancelled(true);
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

            Player player = (Player) event.getWhoClicked();

            if (clickedItem.getType() == Material.ARROW) {
                eventManager.openOptionsGUI(player);
                return;
            }

            PotionEffectType effectType = PotionEffectType.getByName(clickedItem.getItemMeta().getDisplayName().substring(2));

            if (effectType != null) {
                int currentLevel = eventManager.getIntSetting("effectLevel." + effectType.getName(), 0);
                if (event.isLeftClick()) {
                    currentLevel++;
                } else if (event.isRightClick() && currentLevel > 0) {
                    currentLevel--;
                }
                eventManager.saveSetting("effectLevel." + effectType.getName(), currentLevel);
                player.sendMessage("§a" + effectType.getName() + " level set to " + currentLevel + ".");
                eventManager.openPermanentEffectGUI(player);
            }
        }
    }

    private boolean getSetting(String key, boolean defaultValue) {
        File settingsFile = new File(eventManager.getPlugin().getDataFolder(), "settings.yml");
        FileConfiguration settingsConfig = YamlConfiguration.loadConfiguration(settingsFile);
        return settingsConfig.getBoolean(key, defaultValue);
    }
}