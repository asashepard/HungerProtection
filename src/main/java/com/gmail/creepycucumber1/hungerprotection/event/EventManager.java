package com.gmail.creepycucumber1.hungerprotection.event;

import com.gmail.creepycucumber1.hungerprotection.HungerProtection;
import com.gmail.creepycucumber1.hungerprotection.items.ClaimInspectionTool;
import com.gmail.creepycucumber1.hungerprotection.util.TextUtil;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class EventManager implements Listener {

    private HungerProtection plugin;

    public EventManager(HungerProtection plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        if(plugin.getDataManager().getConfig().getConfigurationSection("players." + player.getUniqueId().toString()) == null)
            plugin.getPlayerManager().createNewPlayer(player);
    }

    @EventHandler
    public void onPlayerRightClick(PlayerInteractEvent e) {
        Player p = e.getPlayer();

        if(e.getAction().isRightClick() && p.getInventory().getItemInMainHand().equals(ClaimInspectionTool.claimInspectionTool)) {
            Block block = p.getTargetBlock(200);
            if(block == null) return;
            Location loc = block.getLocation();

            String claimID = plugin.getClaimManager().getClaim(loc);
            if(claimID.equalsIgnoreCase("none")) {
                p.sendMessage(TextUtil.convertColor("&6Nobody has claimed that block."));
                return;
            }

            String ownerName = plugin.getClaimManager().getOwner(claimID).getName();
            if(plugin.getClaimManager().getIsAdmin(claimID)) ownerName = "an administrator";

            p.sendMessage(TextUtil.convertColor("&eThat block has been claimed by " + ownerName + "."));

        }
    }

    // enforce claim rules

    //todo replace with packets?
    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        Player p = e.getPlayer();
        if(p.isOp()) return;
        String claimID = plugin.getClaimManager().getClaim(p.getLocation());
        if(claimID.equalsIgnoreCase("none")) return; //not in a claim
        if(plugin.getClaimManager().getBuilders(claimID).contains(p)) return; //is trusted or owns the claim

        e.setCancelled(true);
    }

    //todo replace with packets?
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        e.getAction().equals(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK);
        if(p.isOp()) return;
        String claimID = plugin.getClaimManager().getClaim(p.getLocation());
        if(claimID.equalsIgnoreCase("none")) return; //not in a claim
        if(plugin.getClaimManager().getBuilders(claimID).contains(p)) return; //is trusted or owns the claim

        e.setCancelled(true);
    }

}
