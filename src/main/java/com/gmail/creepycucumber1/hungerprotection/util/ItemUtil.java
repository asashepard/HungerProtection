package com.gmail.creepycucumber1.hungerprotection.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

public class ItemUtil {
    public static ItemStack createItemStack(Material material, String name, String... lore){
        ItemStack item = new ItemStack(material, 1);

        ItemMeta meta = item.getItemMeta();
        if(meta!=null){
            meta.setDisplayName(TextUtil.convertColor(name));
            ArrayList<String> loreList = new ArrayList<>();
            for(String loreStr : lore){
                loreList.add(TextUtil.convertColor(loreStr));
            }
            meta.setLore(loreList);

            item.setItemMeta(meta);
        }

        return item;
    }
}