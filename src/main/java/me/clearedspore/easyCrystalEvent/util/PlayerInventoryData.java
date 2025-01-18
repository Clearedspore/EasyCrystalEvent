package me.clearedspore.easyCrystalEvent.util;

import org.bukkit.inventory.ItemStack;

public class PlayerInventoryData {
    private final ItemStack[] mainInventory;
    private final ItemStack[] armorContents;
    private final ItemStack offHandItem;

    public PlayerInventoryData(ItemStack[] mainInventory, ItemStack[] armorContents, ItemStack offHandItem) {
        this.mainInventory = mainInventory.clone();
        this.armorContents = armorContents.clone();
        this.offHandItem = offHandItem.clone();
    }

    public ItemStack[] getMainInventory() {
        return mainInventory.clone();
    }

    public ItemStack[] getArmorContents() {
        return armorContents.clone();
    }

    public ItemStack getOffHandItem() {
        return offHandItem.clone();
    }
}