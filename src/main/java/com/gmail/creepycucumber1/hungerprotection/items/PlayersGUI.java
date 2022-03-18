package com.gmail.creepycucumber1.hungerprotection.items;

import com.gmail.creepycucumber1.hungerprotection.HungerProtection;
import com.gmail.creepycucumber1.hungerprotection.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.UUID;

public class PlayersGUI extends GUI {
    private Player player;

    public PlayersGUI(HungerProtection plugin, Player player, String claimID, String command) {
        super(plugin, player.getUniqueId(), "&l/" + command + " in " + claimID.substring(0, 4) + "...", Bukkit.getOnlinePlayers().size() / 9 + 1);
        this.player = player;

        int index = 1;
        for(Player p : Bukkit.getOnlinePlayers()) {
            if(p.equals(player)) continue;
            ItemStack skull = getSkull(p.getUniqueId().toString());
            GUIItem skullItem = new GUIItem(skull, command + " " + (command.equalsIgnoreCase("transferclaim") ? claimID + " " : "") + p.getName());
            items[index++] = skullItem;
        }

        ItemStack info = new ItemStack(Material.PAPER);
        ItemMeta infoMeta = info.getItemMeta();
        infoMeta.setDisplayName(TextUtil.convertColor("&fOnline players are listed"));
        ArrayList<String> lore = new ArrayList<>();
        lore.add(TextUtil.convertColor("&7Click on a player head to &o/" + command + " &r&7for that player."));
        lore.add(TextUtil.convertColor("&7Alternatively, use &o/" + command + " [player name]&r&7 as a command."));
        infoMeta.setLore(lore);
        info.setItemMeta(infoMeta);
        GUIItem infoItem = new GUIItem(info, "info");
        items[0] = infoItem;

    }

    public ItemStack getSkull(String uuid) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
        skullMeta.setOwningPlayer(player);
        skullMeta.setDisplayName(TextUtil.convertColor("&a" + Bukkit.getOfflinePlayer(UUID.fromString(uuid)).getName()));
        skull.setItemMeta(skullMeta);
        return skull;
    }

    @Override
    public void open() {
        player.openInventory(createInventory());
    }

    @Override
    public void clicked(Player p, GUIItem item) {
        if(item.getItem().getType().equals(Material.PLAYER_HEAD)) {
            p.closeInventory();
            Bukkit.getServer().dispatchCommand(p, item.getItemId());
            player.sendMessage(TextUtil.convertColor("&aSuccessfully completed the command &o" + item.getItemId() + "&r&a."));
        }
    }
}
