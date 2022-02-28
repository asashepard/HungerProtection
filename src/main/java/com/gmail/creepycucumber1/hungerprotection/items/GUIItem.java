/*
 * Copyright 2020 Gabriel Keller
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.gmail.creepycucumber1.hungerprotection.items;

import com.gmail.creepycucumber1.hungerprotection.util.TextUtil;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

public class GUIItem {

    protected String itemId;
    protected ItemStack item;

    public GUIItem(Material mat, int amount, String ID, String name, String... lore){
        this.item = createItemStack(mat, name, lore);
        this.itemId = ID;
    }

    public GUIItem(Material mat, String itemId, String name, String... lore){
        new GUIItem(mat, 1, itemId, name, lore);
    }

    public GUIItem(ItemStack item, String itemId) {
        this.item = item;
        this.itemId = itemId;
    }

    public ItemStack getItem() {
        return item;
    }

    public String getId() {
        return itemId;
    }

    public boolean equals(ItemStack itemStack){
        if(itemStack==null) return false;
        return itemStack.equals(item);
    }

    public String getItemId() {
        return itemId;
    }

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
