package com.gmail.creepycucumber1.hungerprotection.event;

import com.destroystokyo.paper.event.entity.ProjectileCollideEvent;
import com.destroystokyo.paper.event.player.PlayerLaunchProjectileEvent;
import com.gmail.creepycucumber1.hungerprotection.HungerProtection;
import com.gmail.creepycucumber1.hungerprotection.claim.PlayerManager;
import com.gmail.creepycucumber1.hungerprotection.claim.Subdivision;
import com.gmail.creepycucumber1.hungerprotection.items.ClaimInspectionTool;
import com.gmail.creepycucumber1.hungerprotection.items.ClaimTool;
import com.gmail.creepycucumber1.hungerprotection.util.TextUtil;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.world.level.block.TrappedChestBlock;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.entity.*;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.util.BoundingBox;

import java.util.ArrayList;
import java.util.List;

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
    public void onInventoryClick(InventoryClickEvent e) {
        boolean res = plugin.getGuiManager().onClick(Bukkit.getPlayer(e.getWhoClicked().getUniqueId()), e.getCurrentItem(), e.getView());
        if(res) e.setCancelled(true);
    }

    @EventHandler
    public void onPlayerRightClick(PlayerInteractEvent e) {
        if(e.getHand() != null && e.getHand().equals(EquipmentSlot.OFF_HAND)) return;

        Player p = e.getPlayer();

        if(e.getAction().isRightClick() && p.getInventory().getItemInMainHand().equals(ClaimInspectionTool.claimInspectionTool)) {
            e.setCancelled(true);

            Block block = p.getTargetBlock(120);
            if(block == null || block.getType().equals(Material.AIR)) return;
            Location loc = block.getLocation();

            String claimID = plugin.cm().getClaim(loc);
            if(claimID.equalsIgnoreCase("none")) {
                p.sendMessage(TextUtil.convertColor("&aNo one has claimed that block."));
                return;
            }

            String ownerName = plugin.cm().getOwner(claimID).getName();
            if(plugin.cm().getIsAdmin(claimID)) ownerName = "an administrator";

            p.sendMessage(TextUtil.convertColor("&7That block has been claimed by &2" + ownerName + "&7."));
            PacketManager.highlightClaim(p, claimID, false);

        }
        else if(e.getAction().isRightClick() && p.getInventory().getItemInMainHand().equals(ClaimTool.claimTool)) {
            e.setCancelled(true);

            Block block = p.getTargetBlock(5);
            if(block == null || block.getType().equals(Material.AIR)) return;
            Location location = block.getLocation();

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
                        //second of resizing claim outside claim
                        String cid = pm.getActiveCID(p);

                        resizeClaim(p, cid, x2, z2);
                        pm.resetCurrentClaimingData(p);

                        return;
                    }
                    //first part of a new claim
                    pm.resetCurrentClaimingData(p);
                    PacketManager.highlightBlock(p, location, Material.GLOWSTONE);
                    pm.setIsClaiming(p, true);
                    pm.setX1(p, location.getBlockX());
                    pm.setZ1(p, location.getBlockZ());
                    p.sendMessage(TextUtil.convertColor("&aCreating new claim! Click on another block to finish."));
                }
                //p owns the claim
                else {
                    int x = location.getBlockX();
                    int z = location.getBlockZ();
                    BoundingBox box = plugin.cm().getVisualBox(claimID);
                    int xMax = (int) box.getMaxX();
                    int zMax = (int) box.getMaxZ();
                    int xMin = (int) box.getMinX();
                    int zMin = (int) box.getMinZ();
                    //is at a corner - first of resizing claim
                    if((x == xMax || x == xMin) && (z == zMax || z == zMin)) {
                        pm.resetCurrentClaimingData(p);
                        pm.setIsClaiming(p, true);
                        pm.setActiveCID(p, claimID);
                        pm.setX1(p, x);
                        pm.setZ1(p, z);
                        p.sendMessage(TextUtil.convertColor("&aResizing claim! Click on another block to finish."));
                    }
                    //second of resizing within own claim
                    else if(pm.isClaiming(p)) {
                        resizeClaim(p, claimID, x, z);
                    }
                    //first or second of subdivision
                    else {
                        //first
                        if(!pm.getActiveCID(p).equalsIgnoreCase(plugin.cm().getClaim(location))) {
                            pm.resetCurrentClaimingData(p);
                            pm.setActiveCID(p, claimID);
                            pm.setX1(p, x);
                            pm.setZ1(p, z);
                            p.sendMessage(TextUtil.convertColor("&aCreating a subdivision! Click on another block within the claim to finish."));
                            PacketManager.highlightBlock(p, location, Material.SEA_LANTERN);
                        }
                        //second
                        else {
                            World w = location.getWorld();
                            BoundingBox sBox = new BoundingBox(pm.getX1(p), w.getMinHeight(), pm.getZ1(p), location.getBlockX(), w.getMaxHeight(), location.getBlockZ());
                            sBox.expand(0, 0, 0, 1, 0, 1);
                            BoundingBox bBox = plugin.cm().getBoundingBox(claimID);
                            if(!bBox.contains(sBox)) {
                                p.sendMessage(TextUtil.convertColor("&7Make sure subdivisions are contained within their parent claim."));
                                pm.resetCurrentClaimingData(p);
                                return;
                            }
                            Subdivision subdivision = new Subdivision(sBox, plugin.cm().getExplosions(claimID), false);
                            PacketManager.highlightArea(p, subdivision.getVisualBox(), Material.SEA_LANTERN, Material.IRON_BLOCK, 6);
                            plugin.cm().addSubdivision(subdivision, claimID);
                            p.sendMessage(TextUtil.convertColor("&aNew subdivision created!"));
                            pm.resetCurrentClaimingData(p);
                        }
                    }

                }

            }
            else {
                p.sendMessage(TextUtil.convertColor("&7You can't stake a claim here!"));
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
                p.sendMessage(TextUtil.convertColor("&7You need &f" + Math.abs(delta) + " &7more claim blocks to resize in this way."));
                TextUtil.sendClickableCommand(p, TextUtil.convertColor("&aClick here to buy claim blocks"), "/buyclaimblocks", "Open the claim blocks GUI");
                return;
            }
            pm.removeClaimBlocks(p, Math.abs(delta));
        }
        plugin.cm().changeCorner(p, cid, pm.getX1(p), pm.getZ1(p), x2, z2);
        PacketManager.highlightClaim(p, cid, false);
    }

    // preprocess commands

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent e) {
        if(e.getMessage().toLowerCase().contains("sethome") || e.getMessage().toLowerCase().contains("pwarp set")) {
            if(!plugin.cm().getHasPermission(
                    e.getPlayer(),
                    plugin.cm().getClaim(e.getPlayer().getLocation()),
                    4)) {
                TextUtil.sendActionBarMessage(e.getPlayer(), TextUtil.MESSAGES.get(4));
                e.setCancelled(true);
            }
        }
    }

    // enforce claim rules

    @EventHandler
    //break a block
    public void onBlockBreak(BlockBreakEvent e) {

        if(plugin.cm().isPrivatized(e.getBlock().getLocation())) {
            if(!plugin.cm().getHasPermission(
                    e.getPlayer(),
                    plugin.cm().getClaim(e.getBlock().getLocation()),
                    1)) {
                TextUtil.sendActionBarMessage(e.getPlayer(), TextUtil.MESSAGES.get(0));
                e.setCancelled(true);
                return;
            }
        }

        if(!plugin.cm().getHasPermission(
                e.getPlayer(),
                plugin.cm().getClaim(e.getBlock().getLocation()),
                2)) {
            TextUtil.sendActionBarMessage(e.getPlayer(), TextUtil.MESSAGES.get(2));
            e.setCancelled(true);
        }
    }

    @EventHandler
    //place a block
    public void onBlockPlace(BlockPlaceEvent e) {

        if(plugin.cm().isPrivatized(e.getBlock().getLocation())) {
            if(!plugin.cm().getHasPermission(
                    e.getPlayer(),
                    plugin.cm().getClaim(e.getBlock().getLocation()),
                    1)) {
                TextUtil.sendActionBarMessage(e.getPlayer(), TextUtil.MESSAGES.get(0));
                e.setCancelled(true);
                return;
            }
        }

        if(!plugin.cm().getHasPermission(
                e.getPlayer(),
                plugin.cm().getClaim(e.getBlock().getLocation()),
                2)) {
            TextUtil.sendActionBarMessage(e.getPlayer(), TextUtil.MESSAGES.get(2));
            e.setCancelled(true);
        }
    }

    @EventHandler
    //left or right-click a block
    public void onPlayerInteract(PlayerInteractEvent e) {
        if(e.getClickedBlock() == null) return;
        if(e.getAction().isLeftClick()) return;

        //placing block, should be handled by that
        if(e.getPlayer().getItemInUse() != null && e.getPlayer().getItemInUse().getType().isBlock() ||
                !(e.getClickedBlock().getType().isInteractable() && !e.getPlayer().isSneaking())) return;

        if(e.getClickedBlock().getType().toString().toLowerCase().contains("door") &&
                !e.getClickedBlock().getType().toString().toLowerCase().contains("trap")) return;

        List<Material> universal = List.of(Material.CRAFTING_TABLE, Material.LOOM, Material.LODESTONE,
                Material.CARTOGRAPHY_TABLE, Material.ENCHANTING_TABLE, Material.STONECUTTER,
                Material.GRINDSTONE);
        List<Material> containers = List.of(Material.CHEST, Material.BARREL, Material.DROPPER,
                Material.HOPPER, Material.FURNACE, Material.SMOKER, Material.BLAST_FURNACE, Material.BEEHIVE,
                Material.JUKEBOX, Material.LECTERN, Material.SHULKER_BOX, Material.DISPENSER, Material.BREWING_STAND);
        List<Material> structures = List.of(Material.CANDLE, Material.CAKE, Material.FLOWER_POT,
                Material.CAMPFIRE, Material.SOUL_CAMPFIRE, Material.CAULDRON, Material.COMPOSTER,
                Material.RESPAWN_ANCHOR);

        if(plugin.cm().isPrivatized(e.getClickedBlock().getLocation())) {
            if(!plugin.cm().getHasPermission(
                    e.getPlayer(),
                    plugin.cm().getClaim(e.getClickedBlock().getLocation()),
                    1) && !universal.contains(e.getClickedBlock().getType())) {
                TextUtil.sendActionBarMessage(e.getPlayer(), TextUtil.MESSAGES.get(0));
                e.setCancelled(true);
                return;
            }
        }

        int level = 4; //default
        if(universal.contains(e.getClickedBlock().getType())) level = 5;
        else if(containers.contains(e.getClickedBlock().getType())) level = 3;
        else if(structures.contains(e.getClickedBlock().getType()) ||
                e.getClickedBlock().getType().toString().toLowerCase().contains("candle")) level = 2;

        if(!plugin.cm().getHasPermission(
                e.getPlayer(),
                plugin.cm().getClaim(e.getClickedBlock().getLocation()),
                level)) {
            TextUtil.sendActionBarMessage(e.getPlayer(), TextUtil.MESSAGES.get(level));
            e.setCancelled(true);
        }
    }

    /*@EventHandler
    //left-click a vehicle
    public void onVehicleDamage(VehicleDamageEvent e) {
        if(e.getAttacker() == null || !(e.getAttacker() instanceof Player player)) return;
        if(!plugin.cm().getHasPermission(
                player,
                plugin.cm().getClaim(e.getVehicle().getLocation()),
                2)) {
            TextUtil.sendActionBarMessage(player, TextUtil.MESSAGES.get(2));
            e.setCancelled(true);
        }
    }*/

    @EventHandler
    //right-click an entity
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent e) {
        //using an item while looking at the entity which will take priority
        List<Material> usable = List.of(Material.BOW, Material.CROSSBOW, Material.GOLDEN_APPLE, Material.ENCHANTED_GOLDEN_APPLE);
        if(e.getPlayer().getItemInUse() != null && usable.contains(e.getPlayer().getItemInUse().getType())) return;

        int level = 4;
        if(e.getRightClicked() instanceof ArmorStand)
            level = 3;

        if(plugin.cm().isPrivatized(e.getRightClicked().getLocation())) {
            if(!plugin.cm().getHasPermission(
                    e.getPlayer(),
                    plugin.cm().getClaim(e.getRightClicked().getLocation()),
                    1)) {
                TextUtil.sendActionBarMessage(e.getPlayer(), TextUtil.MESSAGES.get(0));
                e.setCancelled(true);
                return;
            }
        }

        if(!plugin.cm().getHasPermission(
                e.getPlayer(),
                plugin.cm().getClaim(e.getRightClicked().getLocation()),
                level)) {
            TextUtil.sendActionBarMessage(e.getPlayer(), TextUtil.MESSAGES.get(level));
            e.setCancelled(true);
        }
    }

    @EventHandler
    //rotate item frame
    public void onFrameRotate(PlayerInteractEntityEvent e) {
        if(e.getRightClicked() instanceof Hanging) {

            if(plugin.cm().isPrivatized(e.getRightClicked().getLocation())) {
                if(!plugin.cm().getHasPermission(
                        e.getPlayer(),
                        plugin.cm().getClaim(e.getRightClicked().getLocation()),
                        1)) {
                    TextUtil.sendActionBarMessage(e.getPlayer(), TextUtil.MESSAGES.get(0));
                    e.setCancelled(true);
                    return;
                }
            }

            if(!plugin.cm().getHasPermission(
                    e.getPlayer(),
                    plugin.cm().getClaim(e.getRightClicked().getLocation()),
                    2)) {
                TextUtil.sendActionBarMessage(e.getPlayer(), TextUtil.MESSAGES.get(2));
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    //left-click an entity
    public void onPlayerAttackEvent(EntityDamageByEntityEvent e) {
        //protect armor stands and villagers from explosions
        if(e.getEntity() instanceof ArmorStand || e.getEntity() instanceof AbstractVillager) {
            if(e.getDamager() instanceof TNTPrimed && !plugin.cm().getExplosions(plugin.cm().getClaim(e.getEntity().getLocation()))) {
                e.setCancelled(true);
                return;
            }
        }
        if(!(e.getDamager() instanceof Player player)) return;

        if(!(e.getEntity() instanceof AbstractHorse || e.getEntity() instanceof Cat ||
                e.getEntity() instanceof Parrot || e.getEntity() instanceof ChestedHorse ||
                e.getEntity() instanceof Hanging || e.getEntity() instanceof ArmorStand ||
                e.getEntity() instanceof AbstractVillager || e.getEntity() instanceof EnderCrystal ||
                e.getEntity() instanceof Minecart || e.getEntity() instanceof Boat)) return;

        if(!plugin.cm().getHasPermission(
                player,
                plugin.cm().getClaim(e.getEntity().getLocation()),
                2)) {
            TextUtil.sendActionBarMessage(player, TextUtil.MESSAGES.get(2));
            e.setCancelled(true);
        }
    }

    @EventHandler
    //explosion breaks hanging
    public void onHangingBreak(HangingBreakEvent e) {
        if(e.getCause().equals(HangingBreakEvent.RemoveCause.EXPLOSION) && !plugin.cm().getExplosions(e.getEntity().getLocation())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    //explosion destroys blocks
    public void onEntityExplode(EntityExplodeEvent e) {
        e.blockList().removeIf(b -> !plugin.cm().getExplosions(
                b.getLocation()
        ));
    }

    @EventHandler
    //entity damage in admin claim
    public void onEntityDamage(EntityDamageEvent e) {
        if(!plugin.cm().getIsAdmin(plugin.cm().getClaim(e.getEntity().getLocation()))) return;
        if(!(e.getEntity() instanceof Player)) return;

        e.setCancelled(true);
        Location loc = e.getEntity().getOrigin();
        Particle.DustOptions dust = new Particle.DustOptions(Color.fromRGB(0,255,0), 1);
        e.getEntity().getWorld().spawnParticle(Particle.REDSTONE, loc.getX(), loc.getY(), loc.getZ(), 10, 0, 0, 0, dust);
    }

    @EventHandler
    //projectile hit
    public void onProjectileHit(ProjectileCollideEvent e) {
        if(!(e.getEntity().getShooter() instanceof Player player)) return;
        if(e.getEntity() instanceof EnderPearl) return;

        if(!(e.getCollidedWith() instanceof AbstractHorse || e.getCollidedWith() instanceof Cat ||
                e.getCollidedWith() instanceof Parrot || e.getCollidedWith() instanceof ChestedHorse ||
                e.getCollidedWith() instanceof Hanging || e.getCollidedWith() instanceof ArmorStand ||
                e.getCollidedWith() instanceof AbstractVillager || e.getCollidedWith() instanceof EnderCrystal ||
                e.getCollidedWith() instanceof Minecart || e.getCollidedWith() instanceof Boat)) return;

        if(plugin.cm().isPrivatized(e.getCollidedWith().getLocation())) {
            if(!plugin.cm().getHasPermission(
                    player,
                    plugin.cm().getClaim(e.getCollidedWith().getLocation()),
                    1)) {
                TextUtil.sendActionBarMessage(player, TextUtil.MESSAGES.get(0));
                e.setCancelled(true);
            }
        }
        else if(!plugin.cm().getHasPermission(
                player,
                plugin.cm().getClaim(e.getCollidedWith().getLocation()),
                2)) {
            TextUtil.sendActionBarMessage(player, TextUtil.MESSAGES.get(2));
            e.setCancelled(true);
        }
        else
            return;

        if((e.getCollidedWith() instanceof Hanging || e.getCollidedWith() instanceof EnderCrystal)
                && e.getCollidedWith().isVisualFire()) e.getCollidedWith().setVisualFire(false);
    }

    @EventHandler
    //liquid flows across claim border
    public void onLiquidFlow(BlockFromToEvent e) {
        if(!e.getBlock().getType().equals(Material.LAVA)) {
            if(plugin.cm().getIsAdmin(plugin.cm().getClaim(e.getToBlock().getLocation()))) {
                if(!e.getBlock().getType().equals(Material.WATER)) return; //water flow protected in admin claims
            }
            else
                return;
        }
        Block b = e.getToBlock();

        String claimID = plugin.cm().getClaim(b.getLocation());
        if(claimID.equalsIgnoreCase("none")) return;

        BoundingBox box = plugin.cm().getVisualBox(claimID);

        if(b.getX() == box.getMinX() || b.getX() == box.getMaxX() ||
                b.getZ() == box.getMinZ() || b.getZ() == box.getMaxZ()) {
            e.setCancelled(true);
        }

    }

}
