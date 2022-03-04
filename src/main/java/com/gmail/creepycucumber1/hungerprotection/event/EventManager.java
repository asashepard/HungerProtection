package com.gmail.creepycucumber1.hungerprotection.event;

import com.gmail.creepycucumber1.hungerprotection.HungerProtection;
import com.gmail.creepycucumber1.hungerprotection.claim.PlayerManager;
import com.gmail.creepycucumber1.hungerprotection.claim.Subdivision;
import com.gmail.creepycucumber1.hungerprotection.items.ClaimInspectionTool;
import com.gmail.creepycucumber1.hungerprotection.items.ClaimTool;
import com.gmail.creepycucumber1.hungerprotection.util.TextUtil;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.util.BoundingBox;

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

            String claimID = plugin.cm().getClaim(loc);
            if(claimID.equalsIgnoreCase("none")) {
                p.sendMessage(TextUtil.convertColor("&6Nobody has claimed that block."));
                return;
            }

            String ownerName = plugin.cm().getOwner(claimID).getName();
            if(plugin.cm().getIsAdmin(claimID)) ownerName = "an administrator";

            p.sendMessage(TextUtil.convertColor("&eThat block has been claimed by " + ownerName + "."));

        }
        else if(e.getAction().isRightClick() && p.getInventory().getItemInMainHand().equals(ClaimTool.claimTool)) {
            Location location = p.getTargetBlock(4).getLocation();
            String claimID = plugin.cm().getClaim(location);
            PlayerManager pm = plugin.getPlayerManager();
            if(claimID.equalsIgnoreCase("none") || plugin.cm().getOwner(claimID).equals(p)) {
                //no claim at clicked location - new claim or resizing
                if(claimID.equalsIgnoreCase("none")) {
                    //second part of a new claim or resizing
                    if(pm.isClaiming(p)) {
                        int x2 = location.getBlockX();
                        int z2 = location.getBlockZ();
                        //second part of a new claim
                        if(pm.getActiveCID(p).equalsIgnoreCase("none")) {
                            plugin.cm().createNewClaim(pm.getX1(p), pm.getZ1(p), x2, z2, p.isOp(), p.getWorld().getName(), p);
                            pm.resetCurrentClaimingData(p);
                            return;
                        }
                        //second of resizing claim
                        String cid = pm.getActiveCID(p);

                        resizeClaim(p, cid, x2, z2);

                        return;
                    }
                    //first part of a new claim
                    PacketManager.highlightBlock(p, location, Material.GLOWSTONE);
                    plugin.getPlayerManager().setIsClaiming(p, true);
                    p.sendMessage(TextUtil.convertColor("&6Creating new claim! Click on another block to finish."));
                    return;
                }
                //p owns the claim
                else {
                    int x = location.getBlockX();
                    int z = location.getBlockZ();
                    BoundingBox box = plugin.cm().getBoundingBox(claimID);
                    int xMax = (int) box.getMaxX();
                    int zMax = (int) box.getMaxZ();
                    int xMin = (int) box.getMinX();
                    int zMin = (int) box.getMinZ();
                    //is at a corner - first of resizing claim
                    if((x == xMax || x == xMin) && (z == zMax || z == zMin)) {
                        pm.setIsClaiming(p, true);
                        pm.setActiveCID(p, claimID);
                        pm.setX1(p, x);
                        pm.setZ1(p, z);
                        p.sendMessage(TextUtil.convertColor("&6Resizing claim! Click on another block to finish."));
                        return;
                    }
                    //second of resizing within own claim
                    else if(pm.getActiveCID(p).equalsIgnoreCase(plugin.cm().getClaim(location))) {

                        resizeClaim(p, claimID, x, z);

                    }
                    //first or second of subdivision
                    else {
                        //first
                        if(pm.isClaiming(p)) {
                            pm.setIsClaiming(p, true);
                            pm.setX1(p, x);
                            pm.setZ1(p, z);
                            p.sendMessage(TextUtil.convertColor("&6Creating a subdivision! Click on another block within the claim to finish."));
                            PacketManager.highlightBlock(p, location, Material.IRON_BLOCK);
                        }
                        //second
                        else {
                            World w = location.getWorld();
                            BoundingBox sBox = new BoundingBox(pm.getX1(p), pm.getZ1(p), w.getMinHeight(), location.getBlockX(), location.getBlockZ(), w.getMaxHeight());
                            if(!plugin.cm().overlaps(sBox, w, p, claimID)) { return; }
                            Subdivision subdivision = new Subdivision(sBox, plugin.cm().getExplosions(claimID), false);
                            PacketManager.highlightArea(p, subdivision.getBoundingBox(), Material.SEA_LANTERN, Material.IRON_BLOCK, 6);
                            p.sendMessage(TextUtil.convertColor("&6New subdivision created!"));
                            pm.resetCurrentClaimingData(p);
                        }
                    }

                }

            }
            else {
                p.sendMessage(TextUtil.convertColor("&cYou can't stake a claim here!"));
            }

        }
    }

    private void resizeClaim(Player p, String cid, int x2, int z2) {
        PlayerManager pm = plugin.getPlayerManager();
        int delta = (int) plugin.cm().getBoundingBox(cid).getWidthX() * (int) plugin.cm().getBoundingBox(cid).getWidthZ() -
                (Math.abs(pm.getX1(p) - x2) * Math.abs(pm.getZ1(p) - z2));
        if(delta > 0) {
            pm.addClaimBlocks(p, delta);
        }
        else {
            if(delta > pm.getClaimBlocks(p)) {
                p.sendMessage(TextUtil.convertColor("&cYou need " + Math.abs(delta) + " more claim blocks to resize in this way."));
                TextUtil.sendClickableCommand(p, TextUtil.convertColor("&6&nBuy claim blocks"), "/buyclaimblocks", "Open the claim blocks GUI");
                return;
            }
            pm.removeClaimBlocks(p, Math.abs(delta));
        }
        plugin.cm().changeCorner(p, cid, pm.getX1(p), pm.getZ1(p), x2, z2);
        PacketManager.highlightClaim(p, cid, false);
    }

    // enforce claim rules

    //todo replace with packets?
    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        Player p = e.getPlayer();
        if(p.isOp()) return;
        String claimID = plugin.cm().getClaim(p.getLocation());
        if(claimID.equalsIgnoreCase("none")) return; //not in a claim
        if(plugin.cm().getBuilders(claimID).contains(p)) return; //is trusted or owns the claim

        e.setCancelled(true);
    }

    //todo replace with packets?
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        e.getAction().equals(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK);
        if(p.isOp()) return;
        String claimID = plugin.cm().getClaim(p.getLocation());
        if(claimID.equalsIgnoreCase("none")) return; //not in a claim
        if(plugin.cm().getBuilders(claimID).contains(p)) return; //is trusted or owns the claim

        e.setCancelled(true);
    }

}
