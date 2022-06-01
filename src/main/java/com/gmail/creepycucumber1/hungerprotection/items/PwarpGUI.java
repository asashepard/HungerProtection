package com.gmail.creepycucumber1.hungerprotection.items;

import com.gmail.creepycucumber1.hungerprotection.HungerProtection;
import com.gmail.creepycucumber1.hungerprotection.util.ItemUtil;
import com.gmail.creepycucumber1.hungerprotection.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

public class PwarpGUI extends GUI {
    private Player player;

    private static final int SIZE = 18; //# spaces

    public PwarpGUI(HungerProtection plugin, Player player, int page) {
        super(plugin, player.getUniqueId(), "&lPlayer Warp Menu", 3); //18 spaces
        this.player = player;

        int amount = 0;
        ArrayList<ItemStack> warps = new ArrayList<>(); //ItemStack for GUI display
        ArrayList<String> names = new ArrayList<>();
        for(OfflinePlayer p : plugin.getPlayerManager().getPlayers()) {
            if(plugin.getPlayerManager().getPwarp(p).size() > 0) {
                ItemStack warp = getSkull(p.getUniqueId().toString());
                ItemMeta meta = warp.getItemMeta();
                meta.setDisplayName(TextUtil.convertColor("&a" + plugin.getPlayerManager().getPwarp(p).get(0)));
                meta.setLore(List.of(TextUtil.convertColor("&7&o" + plugin.getPlayerManager().getPwarp(p).get(1))));
                warp.setItemMeta(meta);

                warps.add(warp);
                names.add(plugin.getPlayerManager().getPwarp(p).get(0));
                amount++;
            }
        }

        int index = page * SIZE;
        for(int i = 0; i < SIZE; i++) {
            try {
                ItemStack warp = warps.get(index++); //oobe possible
                items[i] = new GUIItem(warp, names.get(index - 1));
            } catch (IndexOutOfBoundsException e) {
                break;
            }
        }

        int totalPages = (amount - 1) / SIZE + 1;
        if(page != totalPages - 1) {
            ItemStack next = ItemUtil.createItemStack(Material.ARROW, "&7Next Page");
            items[SIZE + 8] = new GUIItem(next, "n3xt " + page);
        }
        if(page != 0) {
            ItemStack previous = ItemUtil.createItemStack(Material.ARROW, "&7Previous Page");
            items[SIZE] = new GUIItem(previous, "pr3vious " + page);
        }
        ItemStack info = ItemUtil.createItemStack(Material.PAPER, "&7Click on a warp to teleport to it!");
        items[SIZE + 4] = new GUIItem(info, "1nfo");

    }

    @Override
    public void open() {
        player.openInventory(createInventory());
    }

    @Override
    public void clicked(Player p, GUIItem item) {
        if(item.getId().startsWith("n3xt")) {
            p.closeInventory();
            plugin.getGuiManager().openGUI(p, new PwarpGUI(plugin, p, Integer.parseInt(item.getId().split(" ")[1]) + 1));
        } else if(item.getId().startsWith("pr3vious")) {
            p.closeInventory();
            plugin.getGuiManager().openGUI(p, new PwarpGUI(plugin, p, Integer.parseInt(item.getId().split(" ")[1]) - 1));
        } else if(item.getId().equals("1nfo")) {
            //do nothing
        }
        else {
            p.closeInventory();
            Bukkit.getServer().dispatchCommand(p, "pwarp " + item.getItemId());
        }
    }

    private ItemStack getSkull(String uuid) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
        skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(uuid));
        skullMeta.setDisplayName(TextUtil.convertColor("&a" + Bukkit.getOfflinePlayer(UUID.fromString(uuid)).getName()));
        skull.setItemMeta(skullMeta);
        return skull;
    }
}
