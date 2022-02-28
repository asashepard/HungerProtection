package com.gmail.creepycucumber1.hungerprotection.items;

import com.gmail.creepycucumber1.hungerprotection.HungerProtection;
import com.gmail.creepycucumber1.hungerprotection.util.TextUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ClaimBlocksGUI extends GUI {
    private Player player;

    public ClaimBlocksGUI(HungerProtection plugin, Player player) {
        super(plugin, player.getUniqueId(), "Buy Claim Blocks", 1);
        this.player = player;

        ItemStack dirt = new ItemStack(Material.DIRT);
        ItemMeta dirtMeta = dirt.getItemMeta();
        dirtMeta.setDisplayName(TextUtil.convertColor("&6Buy 1 claim block &e$2"));
        dirt.setItemMeta(dirtMeta);
        ItemStack grass = new ItemStack(Material.GRASS_BLOCK);
        ItemMeta grassMeta = dirt.getItemMeta();
        grassMeta.setDisplayName(TextUtil.convertColor("&6Buy 10 claim blocks &e$20"));
        grass.setItemMeta(grassMeta);
        ItemStack moss = new ItemStack(Material.MOSS_BLOCK);
        ItemMeta mossMeta = dirt.getItemMeta();
        mossMeta.setDisplayName(TextUtil.convertColor("&6Buy 100 claim blocks &e$200"));
        moss.setItemMeta(mossMeta);

        GUIItem dirtItem = new GUIItem(dirt, "dirt");
        items[2] = dirtItem;
        GUIItem grassItem = new GUIItem(grass, "grass");
        items[4] = grassItem;
        GUIItem mossItem = new GUIItem(moss, "moss");
        items[6] = mossItem;

    }

    @Override
    public void open() {
        player.openInventory(createInventory());
    }

    @Override
    public void clicked(Player p, GUIItem item) {
        if(item.getItem().getType().equals(Material.DIRT)) {
            if(plugin.getVault().getBalance(p) < 2) {
                p.sendMessage(TextUtil.convertColor("&cYou do not have enough money."));
                return;
            }
            plugin.getVault().withdrawPlayer(p, 2);
            p.sendMessage(TextUtil.convertColor("&6You have purchased &e1 &6claim block for &e$2."));
        }
        else if(item.getItem().getType().equals(Material.GRASS_BLOCK)) {
            if(plugin.getVault().getBalance(p) < 20) {
                p.sendMessage(TextUtil.convertColor("&cYou do not have enough money."));
                return;
            }
            plugin.getVault().withdrawPlayer(p, 20);
            p.sendMessage(TextUtil.convertColor("&6You have purchased &e10 &6claim blocks for &e$20."));
        }
        else if(item.getItem().getType().equals(Material.MOSS_BLOCK)) {
            if(plugin.getVault().getBalance(p) < 200) {
                p.sendMessage(TextUtil.convertColor("&cYou do not have enough money."));
                return;
            }
            plugin.getVault().withdrawPlayer(p, 200);
            p.sendMessage(TextUtil.convertColor("&6You have purchased &e100 &6claim blocks for &e$200."));
        }
    }
}
