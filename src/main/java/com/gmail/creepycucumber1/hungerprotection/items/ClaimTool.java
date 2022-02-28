package com.gmail.creepycucumber1.hungerprotection.items;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ClaimTool {

    public static ItemStack claimTool;

    public static void init() { createClaimTool(); }

    private static void createClaimTool() {
        ItemStack item = new ItemStack(Material.GOLDEN_SHOVEL);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "Claim Shovel");
        List<String> lore = new ArrayList<String>();
        lore.add(ChatColor.WHITE + (ChatColor.ITALIC + "Right-click with this shovel to claim land"));
        meta.setLore(lore);

        meta.setCustomModelData(1);

        item.setItemMeta(meta);
        claimTool = item;
    }

}
