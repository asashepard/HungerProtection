package com.gmail.creepycucumber1.hungerprotection.items;

import com.gmail.creepycucumber1.hungerprotection.HungerProtection;
import com.gmail.creepycucumber1.hungerprotection.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;

public class ClaimGUI extends GUI {
    private Player player;

    public ClaimGUI(HungerProtection plugin, Player player, String claimID) {
        super(plugin, player.getUniqueId(), "&lClaim " + claimID.split("-")[0] + "...", 1);
        this.player = player;

        Material material = Material.GRASS_BLOCK;
        if(plugin.cm().getWorld(claimID).getName().toLowerCase().contains("nether")) material = Material.NETHERRACK;
        if(plugin.cm().getWorld(claimID).getName().toLowerCase().contains("end")) material = Material.END_STONE;

        ItemStack claim = new ItemStack(material);
        ItemMeta claimMeta = claim.getItemMeta();
        claimMeta.setDisplayName(TextUtil.convertColor("&6This Claim"));
        ArrayList<String> lore = new ArrayList<>(Arrays.asList(plugin.cm().toString(player, claimID).split("\n")));
        lore.remove(1);
        claimMeta.setLore(lore);
        claim.setItemMeta(claimMeta);
        GUIItem claimItem = new GUIItem(claim, claimID);
        items[4] = claimItem;

        ItemStack abandon = new ItemStack(Material.RED_WOOL);
        ItemMeta abandonMeta = abandon.getItemMeta();
        abandonMeta.setDisplayName(TextUtil.convertColor("&cAbandon this claim"));
        abandon.setItemMeta(abandonMeta);
        GUIItem abandonItem = new GUIItem(abandon, claimID);
        items[8] = abandonItem;

        ItemStack transfer = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta transferMeta = abandon.getItemMeta();
        transferMeta.setDisplayName(TextUtil.convertColor("&cTransfer ownership"));
        transfer.setItemMeta(transferMeta);
        GUIItem transferItem = new GUIItem(transfer, claimID);
        items[0] = transferItem;

    }

    @Override
    public void open() {
        player.openInventory(createInventory());
    }

    @Override
    public void clicked(Player p, GUIItem item) {
        if(item.getItem().getType().equals(Material.RED_WOOL)) {
            p.closeInventory();
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "abandonclaim " + item.getItemId());
            p.sendMessage(TextUtil.convertColor("&6Claim successfully removed. You now have " +
                    plugin.getPlayerManager().getClaimBlocks(p) + " claim blocks remaining."));
        }
        else if(item.getItem().getType().equals(Material.PLAYER_HEAD)) {
            p.closeInventory();
            plugin.getGuiManager().openGUI(p, new PlayersGUI(plugin, p, item.getItemId()));
        }
    }
}
