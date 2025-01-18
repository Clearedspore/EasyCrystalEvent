package me.clearedspore.easyCrystalEvent.util;

import me.clearedspore.easyCrystalEvent.generation.FlatChunkGenerator;
import me.clearedspore.easyCrystalEvent.generation.SandstoneChunkGenerator;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.*;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class EventManager {
    private final JavaPlugin plugin;
    private PlayerInventoryData eventKit;
    private final Map<Player, String> selectedWorldType = new HashMap<>();
    private final Map<Player, PlayerInventoryData> savedInventories = new HashMap<>();
    private final Map<Player, PlayerInventoryData> savedHostInventories = new HashMap<>();
    private final Set<Player> eventPlayers = new HashSet<>();
    private final Set<Player> deathplayers = new HashSet<>();
    private final Set<Player> aliveplayers = new HashSet<>();
    private final Set<Player> mutedEventPlayers = new HashSet<>();
    private Player host;
    private int countdownTime;
    private BukkitTask countdownTask;
    private File kitFile;
    private FileConfiguration kitConfig;
    private boolean eventStarted = false;
    private boolean eventActive = false;
    private static final double MIN_BORDER_SIZE = 10.0;
    private static final int RADIUS = 80;
    private static final int LAYERS_TO_CLEAR = 10;
    private final Set<Player> hostmode = new HashSet<>();

    public EventManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.countdownTime = plugin.getConfig().getInt("countdown", 300);
        this.kitFile = new File(plugin.getDataFolder(), "kit.yml");
        this.kitConfig = YamlConfiguration.loadConfiguration(kitFile);
        loadKit();
    }

    public JavaPlugin getPlugin() {
        return plugin;
    }

    public void saveSetting(String key, Object value) {
        File settingsFile = new File(plugin.getDataFolder(), "settings.yml");
        FileConfiguration settingsConfig = YamlConfiguration.loadConfiguration(settingsFile);
        settingsConfig.set(key, value);
        try {
            settingsConfig.save(settingsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void openSetupGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 9, "Event Setup");

        ItemStack selectGeneration = new ItemStack(Material.GRASS_BLOCK);
        ItemMeta selectGenerationMeta = selectGeneration.getItemMeta();
        selectGenerationMeta.setDisplayName("§a§lSelect Generation");
        selectGeneration.setItemMeta(selectGenerationMeta);

        ItemStack selectKit = new ItemStack(Material.CHEST);
        ItemMeta kitMeta = selectKit.getItemMeta();
        kitMeta.setDisplayName("§d§lSelect Kit");
        selectKit.setItemMeta(kitMeta);

        ItemStack options = new ItemStack(Material.OAK_SIGN);
        ItemMeta optionsMeta = options.getItemMeta();
        optionsMeta.setDisplayName("§b§lOptions");
        options.setItemMeta(optionsMeta);

        ItemStack confirm = new ItemStack(Material.EMERALD_BLOCK);
        ItemMeta confirmMeta = confirm.getItemMeta();
        confirmMeta.setDisplayName("§a§lConfirm");
        confirm.setItemMeta(confirmMeta);

        gui.setItem(0, selectGeneration);
        gui.setItem(1, selectKit);
        gui.setItem(2, options);
        gui.setItem(8, confirm);

        player.openInventory(gui);
    }

    public boolean getSetting(String key, boolean defaultValue) {
        File settingsFile = new File(plugin.getDataFolder(), "settings.yml");
        FileConfiguration settingsConfig = YamlConfiguration.loadConfiguration(settingsFile);
        return settingsConfig.getBoolean(key, defaultValue);
    }



    public int getIntSetting(String key, int defaultValue) {
        File settingsFile = new File(plugin.getDataFolder(), "settings.yml");
        FileConfiguration settingsConfig = YamlConfiguration.loadConfiguration(settingsFile);
        return settingsConfig.getInt(key, defaultValue);
    }

    public void openOptionsGUI(Player player) {
        Inventory optionsGui = Bukkit.createInventory(null, 9, "Event Options");

        boolean naturalBlockDropEnabled = getSetting("naturalBlockDrop", false);
        boolean itemDamageEnabled = getSetting("itemDamage", true);
        boolean elytraEnabled = getSetting("elytra", true);

        ItemStack naturalBlockDrop = new ItemStack(Material.STONE);
        ItemMeta naturalBlockDropMeta = naturalBlockDrop.getItemMeta();
        naturalBlockDropMeta.setDisplayName("§e§lNatural Block Drop");
        naturalBlockDropMeta.setLore(Arrays.asList(
                naturalBlockDropEnabled ? "§a> Enabled" : "§aEnabled",
                naturalBlockDropEnabled ? "§cDisabled" : "§c> Disabled"
        ));
        naturalBlockDrop.setItemMeta(naturalBlockDropMeta);

        ItemStack itemDamage = new ItemStack(Material.ANVIL);
        ItemMeta itemDamageMeta = itemDamage.getItemMeta();
        itemDamageMeta.setDisplayName("§6§lItem Damage");
        itemDamageMeta.setLore(Arrays.asList(
                itemDamageEnabled ? "§a> Enabled" : "§aEnabled",
                itemDamageEnabled ? "§cDisabled" : "§c> Disabled"
        ));
        itemDamage.setItemMeta(itemDamageMeta);

        ItemStack elytra = new ItemStack(Material.ELYTRA);
        ItemMeta elytraMeta = elytra.getItemMeta();
        elytraMeta.setDisplayName("§9§lElytra");
        elytraMeta.setLore(Arrays.asList(
                elytraEnabled ? "§a> Enabled" : "§aEnabled",
                elytraEnabled ? "§cDisabled" : "§c> Disabled"
        ));
        elytra.setItemMeta(elytraMeta);

        ItemStack permanentEffect = new ItemStack(Material.POTION);
        ItemMeta permanentEffectMeta = permanentEffect.getItemMeta();
        permanentEffectMeta.setDisplayName("§d§lPermanent Effect");
        permanentEffect.setItemMeta(permanentEffectMeta);

        ItemStack backArrow = new ItemStack(Material.ARROW);
        ItemMeta backArrowMeta = backArrow.getItemMeta();
        backArrowMeta.setDisplayName("§a§lBack");
        backArrow.setItemMeta(backArrowMeta);

        optionsGui.setItem(0, naturalBlockDrop);
        optionsGui.setItem(2, itemDamage);
        optionsGui.setItem(3, elytra);
        optionsGui.setItem(4, permanentEffect);
        optionsGui.setItem(8, backArrow);

        player.openInventory(optionsGui);
    }

    public void openPermanentEffectGUI(Player player) {
        Inventory effectGui = Bukkit.createInventory(null, 54, "Permanent Effects");

        for (PotionEffectType effectType : PotionEffectType.values()) {
            if (effectType != null) {
                int currentLevel = getIntSetting("effectLevel." + effectType.getName(), 0);
                ItemStack effectItem = new ItemStack(Material.POTION);
                ItemMeta effectMeta = effectItem.getItemMeta();
                effectMeta.setDisplayName("§b" + effectType.getName());
                effectMeta.setLore(Arrays.asList(
                        "§7Left-click to increase level",
                        "§7Right-click to decrease level",
                        "§eLevel = " + currentLevel
                ));
                effectItem.setItemMeta(effectMeta);
                effectGui.addItem(effectItem);
            }
        }


        ItemStack backButton = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backButton.getItemMeta();
        backMeta.setDisplayName("§a§lBack");
        backButton.setItemMeta(backMeta);
        effectGui.setItem(53, backButton);

        player.openInventory(effectGui);
    }

    public void openSetWorldGUI(Player player){
        Inventory gui = Bukkit.createInventory(null, 9, "World Generation Setup");

        ItemStack normalWorld = new ItemStack(Material.GRASS_BLOCK);
        ItemMeta normalMeta = normalWorld.getItemMeta();
        normalMeta.setDisplayName("§a§lNormal Generation");
        normalWorld.setItemMeta(normalMeta);

        ItemStack flatWorld = new ItemStack(Material.DIRT);
        ItemMeta flatMeta = flatWorld.getItemMeta();
        flatMeta.setDisplayName("§e§lFlat World");
        flatWorld.setItemMeta(flatMeta);

        ItemStack sandWorld = new ItemStack(Material.SAND);
        ItemMeta sandMeta = sandWorld.getItemMeta();
        sandMeta.setDisplayName("§7§lSand World");
        sandWorld.setItemMeta(sandMeta);

        gui.setItem(0, normalWorld);
        gui.setItem(1, flatWorld);
        gui.setItem(2, sandWorld);

        player.openInventory(gui);
    }

    public void setSelectedWorldType(Player player, String worldType) {
        selectedWorldType.put(player, worldType);
    }

    public boolean isEventActive() {
        return eventActive;
    }

    public void setEventActive(boolean active) {
        this.eventActive = active;
    }

    public void confirmWorldCreation(Player player) {
        if (eventActive) {
            player.sendMessage("§cAn event is already active. You cannot start another one.");
            return;
        }

        String worldType = selectedWorldType.get(player);
        if (worldType != null) {
            createWorld(player, worldType);
            selectedWorldType.remove(player);
            setHost(player);
            eventPlayers.add(player);
            setEventActive(true);
            startCountdown();
        } else {
            player.sendMessage("§c§lNo world type selected!");
        }
    }

    public void setHost(Player player) {
        this.host = player;
    }

    public Player getHost() {
        return host;
    }

    public void removeHost() {
        this.host = null;
    }

    public void createWorld(Player player, String worldType) {
        WorldCreator creator = new WorldCreator("cpvp_event");
        creator.generateStructures(false);
        switch (worldType) {
            case "NORMAL":
                creator.environment(World.Environment.NORMAL);
                break;
            case "FLAT":
                creator.generator(new FlatChunkGenerator());
                break;
            case "SAND":
                creator.generator(new SandstoneChunkGenerator());
                break;
        }
        World world = creator.createWorld();
        if (world != null) {
            world.setGameRule(GameRule.FALL_DAMAGE, false);
            world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
            world.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true);
            world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);


            int defaultBorderSize = plugin.getConfig().getInt("defualtborder", 200);
            WorldBorder worldBorder = world.getWorldBorder();
            worldBorder.setCenter(world.getSpawnLocation());
            worldBorder.setSize(defaultBorderSize);
            worldBorder.setDamageAmount(1);

            Location spawnLocation = world.getSpawnLocation();
            int highestY = world.getHighestBlockYAt(spawnLocation);
            spawnLocation.setY(highestY + 10);

            world.setSpawnLocation(spawnLocation);
            player.teleport(spawnLocation);
            player.setGameMode(GameMode.SURVIVAL);
            addEventPlayer(player);
        }
    }

    public void openManageGUI(Player player){
        Inventory inventory = Bukkit.createInventory(player, 54, "Manage Event");

        ItemStack ClearLayers = new ItemStack(Material.DROPPER);
        ItemMeta clearLayerMeta = ClearLayers.getItemMeta();
        clearLayerMeta.setDisplayName("§b§lClear layers");
        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add("§eLeft click to clear 10 layers in the event");
        clearLayerMeta.setLore(lore);
        ClearLayers.setItemMeta(clearLayerMeta);

        ItemStack Border = new ItemStack(Material.HEART_OF_THE_SEA);
        ItemMeta borderMeta = Border.getItemMeta();
        borderMeta.setDisplayName("§9§lBorder");
        List<String> lore3 = new ArrayList<>();
        lore3.add("");
        lore3.add("§eLeft click to open the border gui");
        borderMeta.setLore(lore3);
        Border.setItemMeta(borderMeta);

        ItemStack Cancel = new ItemStack(Material.BARRIER);
        ItemMeta cancelMeta = Cancel.getItemMeta();
        cancelMeta.setDisplayName("§c§lCancel Event");
        List<String> lore4 = new ArrayList<>();
        lore4.add("");
        lore4.add("§c§lCancel the event");
        lore4.add("§c§lTHIS CANNOT BE UNDONE!!!");
        cancelMeta.setLore(lore4);
        Cancel.setItemMeta(cancelMeta);

        ItemStack Respawn = new ItemStack(Material.RED_BED);
        ItemMeta respawnMeta = Respawn.getItemMeta();
        respawnMeta.setDisplayName("§a§lRespawn");
        List<String> lore5 = new ArrayList<>();
        lore5.add("");
        lore5.add("§eRespawn a player");
        respawnMeta.setLore(lore5);
        Respawn.setItemMeta(respawnMeta);

        ItemStack Kick = new ItemStack(Material.ARROW);
        ItemMeta kickMeta = Kick.getItemMeta();
        kickMeta.setDisplayName("§d§lKick");
        List<String> lore6 = new ArrayList<>();
        lore6.add("");
        lore6.add("§eKick a player from the event");
        kickMeta.setLore(lore6);
        Kick.setItemMeta(kickMeta);

        inventory.setItem(10, ClearLayers);
        inventory.setItem(13, Cancel);
        inventory.setItem(16, Border);
        inventory.setItem(28, Respawn);
        inventory.setItem(34, Kick);

        player.openInventory(inventory);
    }


    public void openBorderGUI(Player player){
        Inventory inventory = Bukkit.createInventory(player, 27, "Set the border");

        ItemStack Shrink = new ItemStack(Material.RED_CARPET);
        ItemMeta shrinkMeta = Shrink.getItemMeta();
        shrinkMeta.setDisplayName("§eShrink the border by 16 blocks");
        Shrink.setItemMeta(shrinkMeta);

        ItemStack Grow = new ItemStack(Material.LIME_CARPET);
        ItemMeta growMeta = Grow.getItemMeta();
        growMeta.setDisplayName("§eGrow the border by 16 blocks!");
        Grow.setItemMeta(growMeta);

        ItemStack Back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = Back.getItemMeta();
        backMeta.setDisplayName("§eBack");
        Back.setItemMeta(backMeta);

        inventory.setItem(14, Shrink);
        inventory.setItem(15, Grow);
        inventory.setItem(11, Back);

        player.openInventory(inventory);
    }

    public void openKickGUI(Player player) {
        int size = ((eventPlayers.size() / 9) + 1) * 9;
        Inventory eventPlayersGui = Bukkit.createInventory(null, size, "Kick a player");

        for (Player eventPlayer : eventPlayers) {
            ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);
            ItemMeta meta = playerHead.getItemMeta();
            meta.setDisplayName(eventPlayer.getName());
            playerHead.setItemMeta(meta);

            eventPlayersGui.addItem(playerHead);
        }

        player.openInventory(eventPlayersGui);
    }
    public void openRespawn(Player player) {
        int size = ((eventPlayers.size() / 9) + 1) * 9;
        Inventory eventPlayersGui = Bukkit.createInventory(null, size, "Respawn a player");

        for (Player eventPlayer : eventPlayers) {
            ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);
            ItemMeta meta = playerHead.getItemMeta();
            meta.setDisplayName(eventPlayer.getName());
            playerHead.setItemMeta(meta);

            eventPlayersGui.addItem(playerHead);
        }

        player.openInventory(eventPlayersGui);
    }

    public void closeBorder(WorldBorder worldBorder, double currentSize, int blocks, Player player) {
        double newSize = currentSize - blocks;
        if (newSize < MIN_BORDER_SIZE) {
            newSize = MIN_BORDER_SIZE;
        }
        worldBorder.setSize(newSize, 2);
        for(Player online : Bukkit.getOnlinePlayers()){
            online.sendMessage("§c[ALERT] §aThe border is closing to " + newSize + " blocks!!!");
        }
    }

    public void growBorder(WorldBorder worldBorder, double currentSize, int blocks, Player player) {
        double newSize = currentSize + blocks;
        worldBorder.setSize(newSize, 2);
        for(Player online : Bukkit.getOnlinePlayers()){
            online.sendMessage("§c[ALERT] §aThe border is growing by " + newSize + " blocks!!");
        }
    }

    public void saveKit(Player player) {
        ItemStack[] mainInventory = player.getInventory().getContents();
        ItemStack[] armorContents = player.getInventory().getArmorContents();
        ItemStack offHandItem = player.getInventory().getItemInOffHand();
        eventKit = new PlayerInventoryData(mainInventory, armorContents, offHandItem);
        saveKitToFile();
        player.sendMessage("§a§lKit saved successfully!");
    }

    private void saveKitToFile() {
        if (eventKit != null) {
            kitConfig.set("mainInventory", eventKit.getMainInventory());
            kitConfig.set("armorContents", eventKit.getArmorContents());
            kitConfig.set("offHandItem", eventKit.getOffHandItem());
            try {
                kitConfig.save(kitFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadKit() {
        if (kitFile.exists()) {
            ItemStack[] mainInventory = ((List<ItemStack>) kitConfig.get("mainInventory")).toArray(new ItemStack[0]);
            ItemStack[] armorContents = ((List<ItemStack>) kitConfig.get("armorContents")).toArray(new ItemStack[0]);
            ItemStack offHandItem = kitConfig.getItemStack("offHandItem");
            eventKit = new PlayerInventoryData(mainInventory, armorContents, offHandItem);
        }
    }

    public void giveKit(Player player) {
        if (eventKit != null) {
            try {

                ItemStack[] mainInventory = eventKit.getMainInventory();
                for (int i = 0; i < mainInventory.length; i++) {
                    ItemStack item = mainInventory[i];
                    if (item != null) {
                        ItemMeta meta = item.getItemMeta();
                        if (meta != null) {
                            meta.setUnbreakable(true);
                            item.setItemMeta(meta);
                        }
                    }
                }
                player.getInventory().setContents(mainInventory);


                ItemStack[] armorContents = eventKit.getArmorContents();
                for (int i = 0; i < armorContents.length; i++) {
                    ItemStack item = armorContents[i];
                    if (item != null) {
                        ItemMeta meta = item.getItemMeta();
                        if (meta != null) {
                            meta.setUnbreakable(true);
                            item.setItemMeta(meta);
                        }
                    }
                }
                player.getInventory().setArmorContents(armorContents);


                ItemStack offHandItem = eventKit.getOffHandItem();
                if (offHandItem != null) {
                    ItemMeta meta = offHandItem.getItemMeta();
                    if (meta != null) {
                        meta.setUnbreakable(true);
                        offHandItem.setItemMeta(meta);
                    }
                }
                player.getInventory().setItemInOffHand(offHandItem);

                player.sendMessage("§a§lKit given successfully!");
            } catch (Exception e) {
                player.sendMessage("§cAn error occurred while giving the kit.");
                e.printStackTrace();
            }
        } else {
            player.sendMessage("§c§lEvent kit has not been setup");
        }
    }
    public boolean isKitAvailable() {
        return eventKit != null;
    }
    public PlayerInventoryData getEventKit() {
        return eventKit;
    }

    public void clearLayers(Player player) {
        World world = Bukkit.getWorld("cpvp_event");
        if (world == null) {
            player.sendMessage("The event world is not available.");
            return;
        }

        WorldBorder worldBorder = world.getWorldBorder();
        double currentBorderSize = worldBorder.getSize();

        if (currentBorderSize > 100) {
            player.sendMessage("§cThe border is too large to clear layers. It must be 100 blocks or smaller.");
            return;
        }

        Location spawnLocation = world.getSpawnLocation();

        int startX = spawnLocation.getBlockX() - RADIUS;
        int endX = spawnLocation.getBlockX() + RADIUS;
        int startZ = spawnLocation.getBlockZ() - RADIUS;
        int endZ = spawnLocation.getBlockZ() + RADIUS;

        for (int x = startX; x <= endX; x++) {
            for (int z = startZ; z <= endZ; z++) {
                int highestY = world.getHighestBlockYAt(x, z);
                for (int y = highestY; y > highestY - LAYERS_TO_CLEAR && y > 0; y--) {
                    if (world.getBlockAt(x, y, z).getType() != Material.BEDROCK) {
                        world.getBlockAt(x, y, z).setType(Material.AIR);
                    }
                }
            }
        }

      for(Player online : Bukkit.getOnlinePlayers()){
          online.sendMessage("§c[ALERT] §aClearing §f" + LAYERS_TO_CLEAR + "§a layers off blocks! ");
      }
    }

    public void savePlayerInventory(Player player) {
        ItemStack[] mainInventory = player.getInventory().getContents();
        ItemStack[] armorContents = player.getInventory().getArmorContents();
        ItemStack offHandItem = player.getInventory().getItemInOffHand();
        savedInventories.put(player, new PlayerInventoryData(mainInventory, armorContents, offHandItem));
    }

    public void restorePlayerInventory(Player player) {
        PlayerInventoryData inventoryData = savedInventories.get(player);
        if (inventoryData != null) {
            player.getInventory().setContents(inventoryData.getMainInventory());
            player.getInventory().setArmorContents(inventoryData.getArmorContents());
            player.getInventory().setItemInOffHand(inventoryData.getOffHandItem());
            savedInventories.remove(player);
        }
    }

    public void setupScoreboard(Player player) {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard board = manager.getNewScoreboard();

        Objective objective = board.registerNewObjective("event", "dummy", ChatColor.GREEN + "Event Stats");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        Score playersInEvent = objective.getScore(ChatColor.YELLOW + "Players: ");
        playersInEvent.setScore(getEventPlayers().size());

        Score empty = objective.getScore(ChatColor.GREEN + "");

        Score playersAlive = objective.getScore(ChatColor.YELLOW + "Alive: ");
        playersAlive.setScore(getAliveplayers().size());

        player.setScoreboard(board);
    }

    public void OpenKitPreview(Player player) {
        if (!isKitAvailable()) {
            player.sendMessage("§cNo kit has been set.");
            return;
        }

        PlayerInventoryData kit = getEventKit();
        Inventory previewInventory = Bukkit.createInventory(null, 54, "Kit Preview");


        ItemStack[] mainInventory = kit.getMainInventory();
        for (int i = 0; i < mainInventory.length && i < 36; i++) {
            previewInventory.setItem(i, mainInventory[i]);
        }


        ItemStack[] armorContents = kit.getArmorContents();
        for (int i = 0; i < armorContents.length; i++) {
            previewInventory.setItem(45 + i, armorContents[i]);
        }


        previewInventory.setItem(50, kit.getOffHandItem());

        ItemStack OffhandInfo = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
        ItemMeta offhandInfoMeta = OffhandInfo.getItemMeta();
        offhandInfoMeta.setDisplayName("§a< Offhand");
        OffhandInfo.setItemMeta(offhandInfoMeta);

        ItemStack ArmorInfo = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
        ItemMeta armorInfoMeta = ArmorInfo.getItemMeta();
        armorInfoMeta.setDisplayName("§a< Armor");
        ArmorInfo.setItemMeta(armorInfoMeta);

        previewInventory.setItem(49, ArmorInfo);
        previewInventory.setItem(51, OffhandInfo);

        player.openInventory(previewInventory);
    }

    public void respawnPlayer(Player player){
        deathplayers.remove(player);
        eventPlayers.add(player);
        World eventWorld = Bukkit.getWorld("cpvp_event");
        if (eventWorld != null) {
            player.teleport(eventWorld.getSpawnLocation());
        }
        player.setGameMode(GameMode.SURVIVAL);
        giveKit(player);
    }

    public void announceEvent(Player host) {
        int timeLeft = countdownTime;

        int minutes = timeLeft / 60;
        int seconds = timeLeft % 60;
        String timeString = String.format("§a Starts in %d minutes and %d seconds", minutes, seconds);

        for (Player online : Bukkit.getOnlinePlayers()) {
            TextComponent joinButton = new TextComponent("[Click to join!]");
            joinButton.setColor(net.md_5.bungee.api.ChatColor.GREEN);
            joinButton.setBold(true);
            joinButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cevent-join"));

            TextComponent message = new TextComponent();
            message.addExtra(host.getName() + " §ais hosting an event! \n");
            message.addExtra(joinButton);
            message.addExtra(" " + timeString);
            online.spigot().sendMessage(message);
        }
    }

    public void addEventPlayer(Player player) {
        eventPlayers.add(player);
    }

    public void removeEventPlayer(Player player) {
        eventPlayers.remove(player);
    }

    public Set<Player> getEventPlayers() {
        return new HashSet<>(eventPlayers);
    }

    public boolean isEventPlayer(Player player){
        return eventPlayers.contains(player);
    }

    public void addDeathPlayer(Player player){
        deathplayers.add(player);
    }
    public void removeDeathPlayer(Player player){
        deathplayers.remove(player);
    }
    public Set<Player> getDeathplayers(){
        return new HashSet<>(deathplayers);
    }
    public boolean isDeathPlayer(Player player){
        return eventPlayers.contains(player);
    }

    public void addAlivePlayer(Player player){
        aliveplayers.add(player);
    }

    public void removeAlivePlayer(Player player){
        aliveplayers.remove(player);
    }
    public Set<Player> getAliveplayers(){
        return new HashSet<>(aliveplayers);
    }
    public boolean isAlivePlayer(Player player){
        return aliveplayers.contains(player);
    }

    public void addHostMode(Player player){
        hostmode.add(player);
        saveHostInv(player);
        player.getInventory().clear();
        player.setInvisible(true);
        player.setInvulnerable(true);
        player.setGameMode(GameMode.SURVIVAL);
        player.setAllowFlight(true);

        ItemStack KickItem = new ItemStack(Material.RED_BED);
        ItemMeta kickItemMeta = KickItem.getItemMeta();
        kickItemMeta.setDisplayName("§e§lKick a player");
        KickItem.setItemMeta(kickItemMeta);

        ItemStack AdvertiseItem;
    }
    public void removeHostMode(Player player){
        hostmode.remove(player);
        player.getInventory().clear();
        restoreHostInv(player);
        player.setGameMode(GameMode.SPECTATOR);


    }

    public void saveHostInv(Player host) {
        if (host != null) {
            ItemStack[] mainInventory = host.getInventory().getContents();
            ItemStack[] armorContents = host.getInventory().getArmorContents();
            ItemStack offHandItem = host.getInventory().getItemInOffHand();
            savedHostInventories.put(host, new PlayerInventoryData(mainInventory, armorContents, offHandItem));
            host.sendMessage("§aHost inventory saved successfully!");
        } else {
            System.out.println("No host is set to save inventory.");
        }
    }

    public void restoreHostInv(Player host) {
        PlayerInventoryData inventoryData = savedHostInventories.get(host);
        if (inventoryData != null) {
            host.getInventory().setContents(inventoryData.getMainInventory());
            host.getInventory().setArmorContents(inventoryData.getArmorContents());
            host.getInventory().setItemInOffHand(inventoryData.getOffHandItem());
            savedHostInventories.remove(host);
            host.sendMessage("§aHost inventory restored successfully!");
        } else {
            host.sendMessage("§cNo saved inventory found for host.");
        }
    }
    public boolean isInHostMode(Player player){
        return hostmode.contains(player);
    }


    private void startCountdown() {
        countdownTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (countdownTime <= 0) {
                    startCountdownWithTitle();
                    cancel();
                    return;
                }

                int minutes = countdownTime / 60;
                int seconds = countdownTime % 60;
                String timeString = String.format("§aEvent starts in %d minutes and %d seconds", minutes, seconds);

                for (Player player : eventPlayers) {
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(timeString));
                }

                countdownTime--;
            }
        }.runTaskTimer(plugin, 0, 20);
    }

    public int getCountdownTime() {
        return countdownTime;
    }

    public void cancelActionBar() {
        if (countdownTask != null) {
            countdownTask.cancel();
            countdownTask = null;
        }
    }
    public boolean isEventStarted() {
        return eventStarted;
    }

    public void startEvent() {
        if (!eventActive) {
            return;
        }
        eventStarted = true;
        for (Player player : eventPlayers) {
            player.sendMessage("§aThe event has started!");
            player.sendMessage("§aGood luck!!!");
            applyPermanentEffects(player);
        }
    }
    private void applyPermanentEffects(Player player) {
        for (PotionEffectType effectType : PotionEffectType.values()) {
            if (effectType != null) {
                int level = getIntSetting("effectLevel." + effectType.getName(), 0);
                if (level > 0) {
                    player.addPotionEffect(new PotionEffect(effectType, Integer.MAX_VALUE, level - 1, true, false));
                }
            }
        }
    }

    public void cancelEvent() {
        eventActive = false;
        eventStarted = false;
    }


    public void startCountdownWithTitle() {
        cancelActionBar();
        new BukkitRunnable() {
            int countdown = 5;

            @Override
            public void run() {
                if (countdown <= 0) {
                    for (Player player : eventPlayers) {
                        player.sendTitle("§aEvent has begun!", "", 10, 70, 20);
                    }
                    startEvent();
                    cancel();
                    return;
                }

                for (Player player : eventPlayers) {
                    player.sendTitle("§e" + countdown, "", 10, 20, 10);
                }
                countdown--;
            }
        }.runTaskTimer(plugin, 0, 20);
    }

    public void checkForWinner() {
        if (eventPlayers.size() == 1) {
            Player winner = eventPlayers.iterator().next();
            announceWinner(winner);
        }
    }

    private void announceWinner(Player winner) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendTitle("§6" + winner.getName() + " has won the event!!!", "§aGive them a GG in the chat", 10, 70, 20);
        }

        Location winnerLocation = winner.getLocation();
        for (Player player : eventPlayers) {
            player.teleport(winnerLocation);
        }

        new BukkitRunnable() {
            int times = 0;
            int returnCountdown = plugin.getConfig().getInt("returncountdown", 30);

            @Override
            public void run() {
                if (times >= 10) {
                    startReturnCountdown(returnCountdown);
                    cancel();
                    return;
                }

                Firework firework = winner.getWorld().spawn(winnerLocation, Firework.class);
                FireworkMeta meta = firework.getFireworkMeta();
                meta.addEffect(FireworkEffect.builder()
                        .withColor(Color.YELLOW)
                        .with(FireworkEffect.Type.BALL)
                        .withFlicker()
                        .build());
                meta.setPower(1);
                firework.setFireworkMeta(meta);

                times++;
            }
        }.runTaskTimer(plugin, 0, 20);
    }

    private void startReturnCountdown(int countdown) {
        new BukkitRunnable() {
            int timeLeft = countdown;

            @Override
            public void run() {
                if (timeLeft <= 0) {
                    sendPlayersBackToMainWorld();
                    cancel();
                    return;
                }

                String timeString = String.format("§aEvent will close in %d seconds", timeLeft);
                for (Player player : eventPlayers) {
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(timeString));
                }

                timeLeft--;
            }
        }.runTaskTimer(plugin, 0, 20);
    }

    private void sendPlayersBackToMainWorld() {
        String tpBackWorldName = plugin.getConfig().getString("tpbackworld", "world");
        World tpBackWorld = Bukkit.getWorld(tpBackWorldName);

        if (tpBackWorld == null) {
            Bukkit.getLogger().warning("The configured world to teleport back to is not available.");
            return;
        }

        String returnGamemodeString = plugin.getConfig().getString("returnGamemode", "SURVIVAL");
        GameMode returnGamemode;
        try {
            returnGamemode = GameMode.valueOf(returnGamemodeString.toUpperCase());
        } catch (IllegalArgumentException e) {
            Bukkit.getLogger().warning("Invalid game mode in configuration. Defaulting to SURVIVAL.");
            returnGamemode = GameMode.SURVIVAL;
        }

        for (Player player : eventPlayers) {
            restorePlayerInventory(player);
            player.teleport(tpBackWorld.getSpawnLocation());
            player.setGameMode(returnGamemode);
        }

        eventPlayers.clear();
        mutedEventPlayers.clear();
        cancelActionBar();
        removeHost();
        cancelEvent();

        World eventWorld = Bukkit.getWorld("cpvp_event");
        if (eventWorld != null) {
            Bukkit.unloadWorld(eventWorld, false);
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (eventWorld.getWorldFolder().exists()) {
                    deleteWorldFolder(eventWorld.getWorldFolder());
                }
            }, 20L);
        }
    }

    private void deleteWorldFolder(File path) {
        if (path.exists()) {
            for (File file : path.listFiles()) {
                if (file.isDirectory()) {
                    deleteWorldFolder(file);
                } else {
                    file.delete();
                }
            }
        }
        path.delete();
    }
}