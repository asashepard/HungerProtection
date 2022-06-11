package com.gmail.creepycucumber1.hungerprotection.event;

import com.destroystokyo.paper.event.entity.ProjectileCollideEvent;
import com.gmail.creepycucumber1.hungerprotection.HungerProtection;
import com.gmail.creepycucumber1.hungerprotection.claim.PlayerManager;
import com.gmail.creepycucumber1.hungerprotection.claim.Subdivision;
import com.gmail.creepycucumber1.hungerprotection.items.ClaimInspectionTool;
import com.gmail.creepycucumber1.hungerprotection.items.ClaimTool;
import com.gmail.creepycucumber1.hungerprotection.util.TextUtil;
import jline.internal.Nullable;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BoundingBox;
import org.spigotmc.event.entity.EntityDismountEvent;

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
    public void onPlayerQuit(PlayerQuitEvent e) {
        plugin.getGuiManager().onLeave(e.getPlayer());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        boolean res = plugin.getGuiManager().onClick(Bukkit.getPlayer(e.getWhoClicked().getUniqueId()), e.getCurrentItem(), e.getView());
        if(res) e.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e){
        plugin.getGuiManager().onClose(Bukkit.getPlayer(e.getPlayer().getUniqueId()), e.getView());
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

            //end check
            if(location.getWorld().toString().toLowerCase().contains("end") &&
                    Math.abs(location.getX()) <= 150 && Math.abs(location.getZ()) <= 150) {
                p.sendMessage(TextUtil.convertColor("&7You can't stake a claim here!"));
                plugin.getPlayerManager().resetCurrentClaimingData(p);
                return;
            }

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
        int delta = (int) plugin.cm().getBoundingBox(cid).getWidthX() * (int) plugin.cm().getBoundingBox(cid).getWidthZ() - //old box
                (Math.abs(pm.getX1(p) - x2) * Math.abs(pm.getZ1(p) - z2)); //new box
        if(delta > 0) { //smaller
            pm.addClaimBlocks(p, delta);
        }
        else { //bigger
            if(Math.abs(delta) > pm.getClaimBlocks(p)) {
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
        else if(e.getMessage().toLowerCase().contains("warp") || e.getMessage().toLowerCase().contains("tp") ||
                e.getMessage().toLowerCase().contains("home")) {
            //extra layer of malicious claim prevention
            plugin.getPlayerManager().resetCurrentClaimingData(e.getPlayer());
        }
        else if(e.getMessage().toLowerCase().contains("suicide")) {
            if(plugin.cm().getIsAdmin(plugin.cm().getClaim(e.getPlayer().getLocation()))) {
                e.getPlayer().sendMessage(TextUtil.convertColor("&cNo suicide in the admin claim!"));
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
            return;
        }

        // op pickaxe
        ItemStack item = e.getPlayer().getInventory().getItem(EquipmentSlot.HAND);
        if(item.getItemMeta().hasLore() && item.getItemMeta().getLore().get(0).equals(TextUtil.convertColor("&aArea Destroyer"))){
            // From WorldEdit
            int ox = e.getBlock().getX();
            int oy = e.getBlock().getY();
            int oz = e.getBlock().getZ();
            Material type = e.getBlock().getType();

            if (type.isAir()) {
                return false;
            }

            if (type == Material.BEDROCK) {
                return false;
            }

            for (int x = ox - 5; x <= ox + 5; ++x) {
                for (int y = oy - 5; y <= oy + 5; ++y) {
                    for (int z = oz - 5; z <= oz + 5; ++z) {
                        Location l = new Location(x, y, z);
                        Block block = e.getBlock().getWorld().getBlockAt(l);
                        if (block.getType() != type) {
                            continue;
                        }

                        if(plugin.cm().getHasPermission(e.getPlayer(), plugin.cm().getClaim(l), 1)) {
                            block.breakNaturally(item, true);
                        }
                    }
                }
            }

            e.getBlock().breakNaturally()
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

    private static final List<Material> UNIVERSAL = List.of(Material.CRAFTING_TABLE, Material.LOOM, Material.LODESTONE,
            Material.CARTOGRAPHY_TABLE, Material.ENCHANTING_TABLE, Material.STONECUTTER,
            Material.GRINDSTONE, Material.LECTERN);
    private static final List<Material> CONTAINERS = List.of(Material.CHEST, Material.BARREL, Material.DROPPER,
            Material.HOPPER, Material.FURNACE, Material.SMOKER, Material.BLAST_FURNACE, Material.BEEHIVE,
            Material.JUKEBOX, Material.SHULKER_BOX, Material.DISPENSER, Material.BREWING_STAND);
    private static final List<Material> STRUCTURES = List.of(Material.CANDLE, Material.CAKE, Material.FLOWER_POT,
            Material.CAMPFIRE, Material.SOUL_CAMPFIRE, Material.CAULDRON, Material.COMPOSTER,
            Material.RESPAWN_ANCHOR, Material.REDSTONE_WIRE, Material.REPEATER, Material.COMPARATOR);

    private String claim = null;
    @EventHandler
    //left or right-click a block
    public void onPlayerInteract(PlayerInteractEvent e) {
        claim = null;
        
        if(e.getClickedBlock() == null) return;
        if(e.getAction().isLeftClick()) return;

        //placing block, should be handled by that
        if(e.getPlayer().getItemInUse() != null && e.getPlayer().getItemInUse().getType().isBlock() ||
                !(e.getClickedBlock().getType().isInteractable() && !e.getPlayer().isSneaking())) return;

        //exceptions
        if(Tag.DOORS.isTagged(e.getClickedBlock().getType())) return;

        if(Tag.BUTTONS.isTagged(e.getClickedBlock().getType()) &&
                plugin.cm().getIsAdmin(getClaim(e))) return;


        //private
        if(plugin.cm().isPrivatized(e.getClickedBlock().getLocation(), getClaim(e))) {
            if(!plugin.cm().getHasPermission(
                    e.getPlayer(),
                    getClaim(e),
                    1) && !UNIVERSAL.contains(e.getClickedBlock().getType())) {
                TextUtil.sendActionBarMessage(e.getPlayer(), TextUtil.MESSAGES.get(0));
                e.setCancelled(true);
                return;
            }
        }

        int level = 4; //default
        if(UNIVERSAL.contains(e.getClickedBlock().getType())) level = 5;
        else if(CONTAINERS.contains(e.getClickedBlock().getType())) level = 3;
        else if(STRUCTURES.contains(e.getClickedBlock().getType()) ||
                e.getClickedBlock().getType().toString().toLowerCase().contains("candle")) level = 2;

        if(!plugin.cm().getHasPermission(
                e.getPlayer(),
                getClaim(e),
                level)) {
            if((level != 4 || e.getClickedBlock().getType().isInteractable()) || Tag.STAIRS.isTagged(e.getClickedBlock().getType())) //send message
                TextUtil.sendActionBarMessage(e.getPlayer(), TextUtil.MESSAGES.get(level));
            e.setCancelled(true);
        }
    }
    // Below are helper functions to prevent repetitive calls to ClaimManager#getClaim (temporary fix)
    private String getClaim(Location l){
        if(claim == null) claim = plugin.cm().getClaim(l);

        return claim;
    }
    
    private String getClaim(Block l){
        return getClaim(l.getLocation());
    }
    
    private String getClaim(PlayerInteractEvent e){
        return getClaim(e.getClickedBlock().getLocation());
    }

    @EventHandler
    //dedicated lectern suppressor
    public void onLecternTakeBook(PlayerTakeLecternBookEvent e) {
        if(!plugin.cm().getHasPermission(
                e.getPlayer(),
                plugin.cm().getClaim(e.getLectern().getLocation()),
                3)) {
            TextUtil.sendActionBarMessage(e.getPlayer(), TextUtil.MESSAGES.get(3));
            e.setCancelled(true);
        }
    }

    @EventHandler
    //dedicated bone meal suppressor
    public void onBoneMeal(BlockFertilizeEvent e) {
        if(e.getPlayer() == null) return;
        for(BlockState b : e.getBlocks()) {
            if(!plugin.cm().getHasPermission(
                    e.getPlayer(),
                    plugin.cm().getClaim(b.getLocation()),
                    2)) {
                TextUtil.sendActionBarMessage(e.getPlayer(), TextUtil.MESSAGES.get(2));
                e.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    //dedicated bucket suppressor
    public void onBucketEmpty(PlayerBucketEmptyEvent e) {
        if(!plugin.cm().getHasPermission(
                e.getPlayer(),
                plugin.cm().getClaim(e.getBlock().getLocation()),
                2)) {
            TextUtil.sendActionBarMessage(e.getPlayer(), TextUtil.MESSAGES.get(2));
            e.setCancelled(true);
        }
    }

    @EventHandler
    //dedicated crop trample suppressor
    public void onCropTrample(PlayerInteractEvent e) {
        if(e.getAction().equals(Action.PHYSICAL) && e.getInteractionPoint() != null && e.getInteractionPoint().getBlock().getType().equals(Material.FARMLAND)) {
            if(!plugin.cm().getHasPermission(
                    e.getPlayer(),
                    plugin.cm().getClaim(e.getInteractionPoint()),
                    2)) {
                TextUtil.sendActionBarMessage(e.getPlayer(), TextUtil.MESSAGES.get(2));
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    //left-click a vehicle (minecart in admin claim)
    public void onVehicleDamage(VehicleDamageEvent e) {
        if(e.getAttacker() == null || !(e.getAttacker() instanceof Player player)) return;
        if(!(e.getVehicle() instanceof Minecart)) return;
        if(!plugin.cm().getHasPermission(
                player,
                plugin.cm().getClaim(e.getVehicle().getLocation()),
                2)) {
            TextUtil.sendActionBarMessage(player, TextUtil.MESSAGES.get(2));
            e.setCancelled(true);
        }
    }

    @EventHandler
    //right-click an entity
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent e) {
        //using an item while looking at the entity which will take priority
        List<Material> usable = List.of(Material.BOW, Material.CROSSBOW, Material.GOLDEN_APPLE, Material.ENCHANTED_GOLDEN_APPLE);
        if(e.getPlayer().getItemInUse() != null && usable.contains(e.getPlayer().getItemInUse().getType())) return;

        int level = 4;
        if(e.getRightClicked() instanceof ArmorStand)
            level = 3;
        if(e.getRightClicked() instanceof Sheep)
            level = 5;

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

        //build-protected entities
        if(!(e.getEntity() instanceof AbstractHorse || e.getEntity() instanceof Cat ||
                e.getEntity() instanceof Parrot || e.getEntity() instanceof ChestedHorse ||
                e.getEntity() instanceof Hanging || e.getEntity() instanceof ArmorStand ||
                e.getEntity() instanceof AbstractVillager || e.getEntity() instanceof EnderCrystal ||
                e.getEntity() instanceof Minecart || e.getEntity() instanceof Boat) ||
                e.getEntity() instanceof Cow || e.getEntity() instanceof Sheep ||
                e.getEntity() instanceof Pig || e.getEntity() instanceof Chicken) return;

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
        if(e.getEntity() instanceof Creeper) {
            e.blockList().clear();
            return;
        }
        e.blockList().removeIf(b -> !plugin.cm().getExplosions(
                b.getLocation()
        ) && !b.getType().equals(Material.TNT));
    }

    @EventHandler
    //enderman picks up block or entity tramples farmland - NOT CLAIM-SPECIFIC
    public void onChangeBlock(EntityChangeBlockEvent e) {
        if(e.getEntity() instanceof Enderman) {
            e.setCancelled(true);
        }
        else if(!(e.getEntity() instanceof Player) && e.getBlock().getType().equals(Material.FARMLAND)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    //door broken by monster - NOT CLAIM-SPECIFIC
    public void onChangeBlock(EntityBreakDoorEvent e) {
        if(e.getEntity() instanceof Monster) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    //explosion again
    public void onExplosion(BlockExplodeEvent e) {
        e.blockList().removeIf(b -> !plugin.cm().getExplosions(
                b.getLocation()
        ) && !b.getType().equals(Material.TNT));
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
    //projectile collide
    public void onProjectileCollide(ProjectileCollideEvent e) {
        if(!(e.getEntity().getShooter() instanceof Player player)) return;

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
    //ender pearl
    public void onProjectileHit(ProjectileHitEvent e) {
        if(e.getEntity() instanceof EnderPearl) {
            if(!(e.getEntity().getShooter() instanceof Player p)) return;
            if(plugin.cm().getClaim(e.getEntity().getLocation()).equals("none")) return;
            String claimID = plugin.cm().getClaim(e.getEntity().getLocation());

            if(plugin.cm().getHasPermission(p, claimID, 2)) return;

            OfflinePlayer owner = plugin.cm().getOwner(claimID);
            if(!owner.isOnline() || plugin.getEssentials().getUser(owner.getUniqueId()).isAfk()) return;

            for(Player player : e.getEntity().getLocation().getNearbyPlayers(20.0)) {
                if(owner.equals(player)) e.setCancelled(true);
            }
        }
    }

    @EventHandler
    //splash potion
    public void onSplashPotion(PotionSplashEvent e) {
        e.getAffectedEntities().removeIf(entity ->
                e.getEntity().getShooter() instanceof Player &&
                        (entity instanceof AbstractHorse || entity instanceof Cat ||
                        entity instanceof Parrot || entity instanceof ArmorStand ||
                        entity instanceof AbstractVillager || entity instanceof EnderCrystal) &&
                        !plugin.cm().getClaim(entity.getLocation()).equals("none") &&
                        !plugin.cm().getHasPermission((Player) e.getEntity().getShooter(), plugin.cm().getClaim(entity.getLocation()), 2)
        );
    }

    @EventHandler
    //hunger loss
    public void onHungerEvent(FoodLevelChangeEvent e) {
        if(!(e.getEntity() instanceof Player)) return;
        if(plugin.cm().getClaim(e.getEntity().getLocation()).equals("none")) return;

        String claimID = plugin.cm().getClaim(e.getEntity().getLocation());
        if(plugin.cm().getIsAdmin(claimID)) e.setCancelled(true);
    }

    @EventHandler
    //food eat
    public void onEatFood(PlayerItemConsumeEvent e) {
        if(plugin.cm().getClaim(e.getPlayer().getLocation()).equals("none")) return;

        String claimID = plugin.cm().getClaim(e.getPlayer().getLocation());
        if(plugin.cm().getIsAdmin(claimID)) e.setCancelled(true);
        else if(e.getItem().getType().equals(Material.CHORUS_FRUIT)) {
            if(!plugin.cm().getHasPermission(
                    e.getPlayer(),
                    plugin.cm().getClaim(e.getPlayer().getLocation()),
                    2)) {
                TextUtil.sendActionBarMessage(e.getPlayer(), TextUtil.MESSAGES.get(2));
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    //mob spawn (in admin claim)
    public void onSpawnEvent(EntitySpawnEvent e) {
        if(!(e.getEntity() instanceof Monster)) return;
        if(plugin.cm().getClaim(e.getLocation()).equals("none")) return;

        String claimID = plugin.cm().getClaim(e.getEntity().getLocation());
        if(plugin.cm().getIsAdmin(claimID)) e.setCancelled(true);
    }

    @EventHandler
    //liquid flows across claim border
    public void onLiquidFlow(BlockFromToEvent e) {
        if(e.getBlock().getWorld().toString().toLowerCase().contains("nether")) return;
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

    @EventHandler
    //piston extends across claim border
    public void onPistonExtend(BlockPistonExtendEvent e) {

        List<Block> blocks = e.getBlocks();

        //normal piston, not sticky
        if(e.getBlock().getType().equals(Material.PISTON)) {
            ArrayList<Block> connected = new ArrayList<>();
            Block b = e.getBlock().getRelative(e.getDirection());
            int count = 0;
            while(!b.getType().equals(Material.AIR) && count <= 12) {
                count++;
                connected.add(b);
                b = b.getRelative(e.getDirection());
            }
            blocks = connected;
        }

        for(Block b : blocks) {

            String claimID = plugin.cm().getClaim(b.getRelative(e.getDirection()).getLocation());
            if(claimID.equalsIgnoreCase("none")) return;

            BoundingBox box = plugin.cm().getVisualBox(claimID);

            //protect private subdivisions
            boolean isPrivateSub = false;
            for(Subdivision subdivision : plugin.cm().getSubdivisions(claimID)) {
                if(subdivision.getIsPrivate() && subdivision.getBoundingBox().contains(b.getLocation().toVector())) {
                    box = subdivision.getVisualBox();
                    isPrivateSub = true;
                    break;
                }
            }

            //if not private subdivision, protect only if admin claim
            if(!isPrivateSub && !plugin.cm().getIsAdmin(claimID)) return;

            if(Math.abs(b.getX() - box.getMinX()) <= 1 || Math.abs(b.getX() - box.getMaxX()) <= 1 ||
                    Math.abs(b.getZ() - box.getMinZ()) <= 1 || Math.abs(b.getZ() - box.getMaxZ()) <= 1) {
                e.setCancelled(true);
                break;
            }

        }

    }

    @EventHandler
    //dispenser dispenses across claim border
    public void onDispense(BlockDispenseEvent e) {

        Block b = e.getBlock();
        String cid = plugin.cm().getClaim(b.getLocation());

        List<Block> list = List.of(b.getRelative(BlockFace.NORTH), b.getRelative(BlockFace.EAST),
                b.getRelative(BlockFace.SOUTH), b.getRelative(BlockFace.WEST));
        boolean different = false;
        for(Block adj : list) {
            //different sides of admin claim border
            if(!plugin.cm().getClaim(adj.getLocation()).equalsIgnoreCase(cid) &&
                    (plugin.cm().getIsAdmin(cid) || plugin.cm().getIsAdmin(plugin.cm().getClaim(adj.getLocation()))))
                different = true;

            //different sides of private subdivision border
            if(plugin.cm().getClaim(adj.getLocation()).equalsIgnoreCase(cid)) {
                for(Subdivision subdivision : plugin.cm().getSubdivisions(cid)) {
                    if(subdivision.getIsPrivate() && (subdivision.getBoundingBox().contains(b.getLocation().toVector())
                           ^ subdivision.getBoundingBox().contains(adj.getLocation().toVector()))) {
                        different = true;
                        break;
                    }
                }
            }

            if(different) {
                e.setCancelled(true);
                return;
            }
        }

    }

    //--chairs--

    @EventHandler
    public void onPlayerSit(PlayerInteractEvent e) {
        if (e.getClickedBlock() != null && e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getHand().equals(EquipmentSlot.HAND) &&
                e.getPlayer().getInventory().getItemInMainHand().getType().equals(Material.AIR) &&
                (Tag.STAIRS.isTagged(e.getClickedBlock().getType()) || Tag.SLABS.isTagged(e.getClickedBlock().getType()))) {
            for(Entity entity : e.getClickedBlock().getLocation().getNearbyEntities(1.5, 1.5, 1.5)) {
                if(entity instanceof Egg) return;
            }
            if (e.getPlayer().isInsideVehicle()) return;

            Egg toSitOn = (Egg) e.getClickedBlock().getLocation().getWorld().spawn(
                    e.getClickedBlock().getLocation().add(0.5, 0, 0.5), Egg.class, (settings) -> {
                        settings.setGravity(false);
                        settings.setInvulnerable(true);
                    });
            toSitOn.addPassenger(e.getPlayer());

        }
    }

    @EventHandler
    public void onEntityDismount(EntityDismountEvent e) {
        if(e.getDismounted() instanceof Egg) {
            e.getDismounted().remove();
        }
    }

    @EventHandler
    public void onPlayerTeleportOffChair(PlayerTeleportEvent e) {
        if(e.getPlayer().getVehicle() instanceof Egg) {
            e.getPlayer().getVehicle().remove();
        }
    }

    @EventHandler
    public void onPlayerDeathOnChair(PlayerDeathEvent e) {
        if(e.getPlayer().getVehicle() instanceof Egg) {
            e.getPlayer().getVehicle().remove();
        }
    }

    @EventHandler
    public void onChairBreak(BlockBreakEvent e) {
        for(Entity entity : e.getBlock().getLocation().getNearbyEntities(1.5, 1.5, 1.5)) {
            if(entity instanceof Egg) entity.remove();
        }
    }

}
