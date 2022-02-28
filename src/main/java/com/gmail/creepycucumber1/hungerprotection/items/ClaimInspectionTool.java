package com.gmail.creepycucumber1.hungerprotection.items;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ClaimInspectionTool {

    public static ItemStack claimInspectionTool;

    public static void init() { createClaimInspectionTool(); }

    private static void createClaimInspectionTool() {
        ItemStack item = new ItemStack(Material.STICK);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "Claim Inspection Stick");
        List<String> lore = new ArrayList<String>();
        lore.add(ChatColor.WHITE + (ChatColor.ITALIC + "Right-click with this stick to inspect claims"));
        meta.setLore(lore);

        meta.setCustomModelData(1);

        item.setItemMeta(meta);
        claimInspectionTool = item;
    }

}
