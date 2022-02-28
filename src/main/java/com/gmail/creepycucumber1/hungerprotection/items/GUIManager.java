/*
 * Copyright 2020 Gabriel Keller
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.gmail.creepycucumber1.hungerprotection.items;

import com.gmail.creepycucumber1.hungerprotection.HungerProtection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.UUID;

public class GUIManager {
    HungerProtection plugin;
    HashMap<UUID, GUI> guiMap;

    public GUIManager(HungerProtection plugin){
        this.plugin = plugin;
        this.guiMap = new HashMap<>();
    }

    public void openGUI(Player p, GUI gui){
        guiMap.remove(p.getUniqueId());
        guiMap.put(p.getUniqueId(), gui);
        gui.open();
    }

    public boolean onClick(Player p, ItemStack item, InventoryView view){
        GUI gui = getGUI(p);
        if(gui==null) return false;

        if(gui.getName().equalsIgnoreCase(view.getTitle())){
            GUIItem[] items = gui.getItems();
            for(GUIItem i : items){
                if(i==null) continue;
                if(i.equals(item)){
                    gui.clicked(p, i);
                    return true;
                }
            }

            return true;
        }

        return false;
    }

    public void onClose(Player p, InventoryView view){
        GUI gui = getGUI(p);

        if(gui!=null) {
            if(gui.getName().equals(view.getTitle()))
                guiMap.remove(p.getUniqueId());
        }
    }

    public GUI getGUI(Player p){
        return guiMap.get(p.getUniqueId());
    }

    public void onLeave(Player p){
        guiMap.remove(p.getUniqueId());
    }
}
