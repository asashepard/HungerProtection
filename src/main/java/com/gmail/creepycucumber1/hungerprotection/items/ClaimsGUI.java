package com.gmail.creepycucumber1.hungerprotection.items;

import com.gmail.creepycucumber1.hungerprotection.HungerProtection;
import com.gmail.creepycucumber1.hungerprotection.util.TextUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.BoundingBox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;

public class ClaimsGUI extends GUI {
    private Player player;

    public ClaimsGUI(HungerProtection plugin, Player player) {
        super(plugin, player.getUniqueId(), "Claims Dashboard", (plugin.cm().getClaims(player).size() + 1) / 9 + 1);
        this.player = player;

        int totalSize = 0;
        int index = 1;
        for(String claimID : plugin.cm().getClaims(player)) {

            Material material = Material.GRASS_BLOCK;
            if(plugin.cm().getWorld(claimID).getName().toLowerCase().contains("nether")) material = Material.NETHERRACK;
            if(plugin.cm().getWorld(claimID).getName().toLowerCase().contains("end")) material = Material.END_STONE;

            ItemStack claim = new ItemStack(material);
            ItemMeta claimMeta = claim.getItemMeta();
            claimMeta.setDisplayName(TextUtil.convertColor("&6" + claimID));
            ArrayList<String> lore = new ArrayList<>(Arrays.asList(plugin.cm().toString(player, claimID).split("\n")));
            claimMeta.setLore(lore);
            claim.setItemMeta(claimMeta);

            GUIItem claimItem = new GUIItem(claim, claimID);
            items[index++] = claimItem;

            BoundingBox box = plugin.cm().getBoundingBox(claimID);
            totalSize += (int) Math.abs(box.getMaxX() - box.getMinX()) * (int) Math.abs(box.getMaxZ() - box.getMinZ());

        }

        ItemStack info = new ItemStack(Material.PAPER);
        ItemMeta infoMeta = info.getItemMeta();
        infoMeta.setDisplayName(TextUtil.convertColor("&fCurrent Claims"));
        ArrayList<String> lore = new ArrayList<>();
        lore.add(TextUtil.convertColor("&7Total area claimed: &6" + totalSize + " blocks"));
        infoMeta.setLore(lore);
        info.setItemMeta(infoMeta);

    }

    @Override
    public void open() {
        player.openInventory(createInventory());
    }

    @Override
    public void clicked(Player p, GUIItem item) {
        //todo present option to abandon?
    }
}
