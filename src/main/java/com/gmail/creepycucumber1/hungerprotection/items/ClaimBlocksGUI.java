package com.gmail.creepycucumber1.hungerprotection.items;

import com.gmail.creepycucumber1.hungerprotection.HungerProtection;
import com.gmail.creepycucumber1.hungerprotection.util.TextUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

public class ClaimBlocksGUI extends GUI {
    private Player player;

    public ClaimBlocksGUI(HungerProtection plugin, Player player) {
        super(plugin, player.getUniqueId(), "&lBuy Claim Blocks", 1);
        this.player = player;

        ItemStack info = new ItemStack(Material.PAPER);
        ItemMeta infoMeta = info.getItemMeta();
        infoMeta.setDisplayName(TextUtil.convertColor("&7Current claim blocks: &f" + plugin.getPlayerManager().getClaimBlocks(player)));
        info.setItemMeta(infoMeta);

        ItemStack dirt = new ItemStack(Material.DIRT);
        ItemMeta dirtMeta = dirt.getItemMeta();
        dirtMeta.setDisplayName(TextUtil.convertColor("&aBuy 1 claim block &e$2"));
        dirt.setItemMeta(dirtMeta);

        ItemStack grass = new ItemStack(Material.GRASS_BLOCK);
        ItemMeta grassMeta = dirt.getItemMeta();
        grassMeta.setDisplayName(TextUtil.convertColor("&aBuy 10 claim blocks &e$20"));
        grass.setItemMeta(grassMeta);

        ItemStack moss = new ItemStack(Material.MOSS_BLOCK);
        ItemMeta mossMeta = dirt.getItemMeta();
        mossMeta.setDisplayName(TextUtil.convertColor("&aBuy 100 claim blocks &e$200"));
        moss.setItemMeta(mossMeta);

        GUIItem infoItem = new GUIItem(info, "info");
        items[0] = infoItem;
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
            plugin.getPlayerManager().addClaimBlocks(player, 1);
            p.sendMessage(TextUtil.convertColor("&aYou have purchased &f1 &aclaim block for &f$2."));

            p.closeInventory();
            plugin.getGuiManager().openGUI(p, new ClaimBlocksGUI(plugin, p));
        }
        else if(item.getItem().getType().equals(Material.GRASS_BLOCK)) {
            if(plugin.getVault().getBalance(p) < 20) {
                p.sendMessage(TextUtil.convertColor("&cYou do not have enough money."));
                return;
            }
            plugin.getVault().withdrawPlayer(p, 20);
            plugin.getPlayerManager().addClaimBlocks(player, 10);
            p.sendMessage(TextUtil.convertColor("&aYou have purchased &f10 &aclaim blocks for &f$20."));

            p.closeInventory();
            plugin.getGuiManager().openGUI(p, new ClaimBlocksGUI(plugin, p));
        }
        else if(item.getItem().getType().equals(Material.MOSS_BLOCK)) {
            if(plugin.getVault().getBalance(p) < 200) {
                p.sendMessage(TextUtil.convertColor("&cYou do not have enough money."));
                return;
            }
            plugin.getVault().withdrawPlayer(p, 200);
            plugin.getPlayerManager().addClaimBlocks(player, 100);
            p.sendMessage(TextUtil.convertColor("&aYou have purchased &f100 &aclaim blocks for &f$200."));

            p.closeInventory();
            plugin.getGuiManager().openGUI(p, new ClaimBlocksGUI(plugin, p));
        }
    }
}
