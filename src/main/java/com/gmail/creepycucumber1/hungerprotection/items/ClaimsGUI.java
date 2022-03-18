package com.gmail.creepycucumber1.hungerprotection.items;

import com.gmail.creepycucumber1.hungerprotection.HungerProtection;
import com.gmail.creepycucumber1.hungerprotection.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.util.BoundingBox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

public class ClaimsGUI extends GUI {
    private Player player;

    public ClaimsGUI(HungerProtection plugin, Player player) {
        super(plugin, player.getUniqueId(), "&lClaims Dashboard", (plugin.cm().getClaims(player).size() + 1) / 9 + 1);
        this.player = player;

        int totalSize = 0;
        int index = 1;
        for(String claimID : plugin.cm().getClaims(player)) {

            Material material = Material.GRASS_BLOCK;
            if(plugin.cm().getWorld(claimID).getName().toLowerCase().contains("nether")) material = Material.NETHERRACK;
            if(plugin.cm().getWorld(claimID).getName().toLowerCase().contains("end")) material = Material.END_STONE;

            ItemStack claim = new ItemStack(material);
            ItemMeta claimMeta = claim.getItemMeta();
            claimMeta.setDisplayName(TextUtil.convertColor("&2" + claimID));
            ArrayList<String> lore = new ArrayList<>(Arrays.asList(plugin.cm().toString(player, claimID).split("\n")));
            lore.remove(1);
            claimMeta.setLore(lore);
            claim.setItemMeta(claimMeta);

            GUIItem claimItem = new GUIItem(claim, claimID);
            items[index++] = claimItem;

            BoundingBox box = plugin.cm().getBoundingBox(claimID);
            totalSize += (int) box.getWidthX() * (int) box.getWidthZ();

        }

        ItemStack info = new ItemStack(Material.PAPER);
        ItemMeta infoMeta = info.getItemMeta();
        infoMeta.setDisplayName(TextUtil.convertColor("&fCurrent Claims"));
        ArrayList<String> lore = new ArrayList<>();
        lore.add(TextUtil.convertColor("&7Total area claimed: &a" + totalSize + " blocks"));
        infoMeta.setLore(lore);
        info.setItemMeta(infoMeta);
        GUIItem infoItem = new GUIItem(info, "info");
        items[0] = infoItem;

    }

    @Override
    public void open() {
        player.openInventory(createInventory());
    }

    @Override
    public void clicked(Player p, GUIItem item) {
        if(item.getItem().getType().equals(Material.GRASS_BLOCK) || item.getItem().getType().equals(Material.NETHERRACK)
                || item.getItem().getType().equals(Material.END_STONE)) {
            p.closeInventory();
            plugin.getGuiManager().openGUI(p, new ClaimGUI(plugin, p, item.getItemId()));
        }
    }
}
