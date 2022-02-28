/*
 * Copyright 2020 Gabriel Keller
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.gmail.creepycucumber1.hungerprotection.items;

import com.gmail.creepycucumber1.hungerprotection.HungerProtection;
import com.gmail.creepycucumber1.hungerprotection.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public abstract class GUI {
    protected HungerProtection plugin;
    protected UUID owner;
    protected String name;
    protected GUIItem[] items;
    protected boolean useBlankItem = false;
    //    protected static final GUIItem BLANK_ITEM = new GUIItem(Material.BLACK_STAINED_GLASS_PANE, "blankitem",  " ");
    protected GUIItem blankItem;

    public GUI(HungerProtection plugin, UUID owner, String name, int rows){
        this.plugin = plugin;
        this.owner = owner;
        this.name = TextUtil.convertColor(name);
        items = new GUIItem[rows*9];

        ItemStack blank = GUIItem.createItemStack(Material.BLACK_STAINED_GLASS_PANE, " ");
        blankItem = new GUIItem(blank, "blankitem");
    }

    public abstract void open();
    public abstract void clicked(Player p, GUIItem item);

    protected Inventory createInventory(){
        Inventory inv = Bukkit.createInventory(Bukkit.getPlayer(owner), items.length, name);
        addItems(inv);

        return inv;
    }

    protected void addItems(Inventory inv){
        for(int i = 0; i < items.length; i++){
            GUIItem item = items[i];
            if(item==null) {
                if(useBlankItem) {
                    inv.setItem(i, blankItem.getItem());
                }
            }
            else{
                inv.setItem(i, item.getItem());
            }
        }
    }

    public UUID getOwner() {
        return owner;
    }

    public String getName() {
        return name;
    }

    public GUIItem[] getItems() {
        return items;
    }
}
