package com.gmail.creepycucumber1.hungerprotection.event;

import com.gmail.creepycucumber1.hungerprotection.HungerProtection;
import com.gmail.creepycucumber1.hungerprotection.claim.Subdivision;
import com.gmail.creepycucumber1.hungerprotection.util.Util;
import io.netty.channel.*;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBlockBreakAckPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.util.BoundingBox;

import java.util.HashMap;
import java.util.List;

public class PacketManager implements Listener {

    private static HungerProtection plugin;

    public PacketManager(HungerProtection plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        injectPlayer(e.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        removePlayer(e.getPlayer());
    }

    public void initialize() {
        for(Player p : Bukkit.getOnlinePlayers())
            injectPlayer(p);
    }

    private void removePlayer(Player player) {
        Channel channel = ((CraftPlayer) player).getHandle().connection.connection.channel;
        channel.eventLoop().submit(() -> {
            channel.pipeline().remove(player.getName());
            return null;
        });
    }

    private void injectPlayer(Player player) {
        ChannelDuplexHandler channelDuplexHandler = new ChannelDuplexHandler(){

            @Override
            public void channelRead(ChannelHandlerContext channelHandlerContext, Object object) throws Exception {
                boolean doPacket = true;
                if(object instanceof Packet<? extends PacketListener> packet)
                    doPacket = onPacket(player, packet);
                if(doPacket) super.channelRead(channelHandlerContext, object);
            }

            @Override
            public void write(ChannelHandlerContext channelHandlerContext, Object object, ChannelPromise channelPromise) throws Exception {
                boolean doPacket = true;
                if(object instanceof Packet<? extends PacketListener> packet)
                    doPacket = onPacket(player, packet);
                if(doPacket) super.write(channelHandlerContext, object, channelPromise);
            }

        };

        ChannelPipeline pipeline = ((CraftPlayer) player).getHandle().connection.connection.channel.pipeline();
        pipeline.addBefore("packet_handler", player.getName(), channelDuplexHandler);
    }

    private boolean onPacket(Player player, Packet<? extends PacketListener> packet) { //reaction to packets
        //block breaking (level = 2)
        if(packet instanceof ServerboundPlayerActionPacket pkt) {
            if(pkt.getAction() == ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK) {
                return plugin.cm().getHasPermission(player, new Location(player.getWorld(),
                        pkt.getPos().getX(), pkt.getPos().getY(), pkt.getPos().getZ()), 2);
            }
        }
        else if(packet instanceof ClientboundBlockBreakAckPacket pkt) {
            return plugin.cm().getHasPermission(player, new Location(player.getWorld(),
                    pkt.pos().getX(), pkt.pos().getY(), pkt.pos().getZ()), 2);
        }
        //todo add more later (entities, containers, buttons, etc.)
        return true;
    }

    public static void highlightClaim(Player player, String claimID, boolean obstructing) {
        String type = "";
        if(plugin.cm().getIsAdmin(claimID)) type = "admin";
        else if(plugin.cm().getOwner(claimID).equals(player)) type = "player";
        else if(obstructing) type = "obstructing";

        Material material1 = Material.SEA_LANTERN;
        Material material2 = Material.IRON_BLOCK;
        switch(type) {
            case "admin" -> {
                material1 = Material.BEDROCK;
                material2 = Material.CRYING_OBSIDIAN;
            }
            case "player" -> {
                material1 = Material.GLOWSTONE;
                material2 = Material.GOLD_BLOCK;
            }
            case "obstructing" -> {
                material1 = Material.REDSTONE_BLOCK;
                material2 = Material.DEEPSLATE_REDSTONE_ORE;
            }
        }

        BoundingBox box = plugin.cm().getBoundingBox(claimID);

        highlightArea(player, box, material1, material2, 6);

        if(!player.equals(plugin.cm().getOwner(claimID))) return;
        for(Subdivision subdivision : plugin.cm().getSubdivisions(claimID)) {
            highlightArea(player, subdivision.getBoundingBox(), Material.SEA_LANTERN, Material.IRON_BLOCK, 6);
        }

    }

    public static void highlightArea(Player player, BoundingBox box, Material material1, Material material2, int step) {

        HashMap<Location, Material> changed = new HashMap<>(); //location, original material before change

        //corners
        for(int x : List.of((int) box.getMinX(), (int) box.getMaxX()))
            for(int z : List.of((int) box.getMinZ(), (int) box.getMaxZ())) {
                Location loc = Util.getHighest(player, x, z);
                changed.put(loc, loc.getBlock().getType());
                ((CraftPlayer) player).sendBlockChange(loc, material1, (byte) 0);
            }

        //sides
        int x = (int) box.getMinX();
        int z = (int) box.getMinZ();
        //x+
        while(x < (int) box.getMaxX()) {
            if(x != box.getMinX() && x != box.getMaxX()) {
                Location loc = Util.getHighest(player, x, z);
                changed.put(loc, loc.getBlock().getType());
                ((CraftPlayer) player).sendBlockChange(loc, material2, (byte) 0);
            }
            x += step;
        }
        x = (int) box.getMaxX();
        //z+
        while(z < (int) box.getMaxZ()) {
            if(z != box.getMinZ() && z != box.getMaxZ()) {
                Location loc = Util.getHighest(player, x, z);
                changed.put(loc, loc.getBlock().getType());
                ((CraftPlayer) player).sendBlockChange(loc, material2, (byte) 0);
            }
            z += step;
        }
        z = (int) box.getMaxZ();
        //x-
        while(x > (int) box.getMinX()) {
            if(x != box.getMinX() && x != box.getMaxX()) {
                Location loc = Util.getHighest(player, x, z);
                changed.put(loc, loc.getBlock().getType());
                ((CraftPlayer) player).sendBlockChange(loc, material2, (byte) 0);
            }
            x -= step;
        }
        x = (int) box.getMinX();
        //z-
        while(z > (int) box.getMinZ()) {
            if(z != box.getMinZ() && z != box.getMaxZ()) {
                Location loc = Util.getHighest(player, x, z);
                changed.put(loc, loc.getBlock().getType());
                ((CraftPlayer) player).sendBlockChange(loc, material2, (byte) 0);
            }
            z -= step;
        }

        Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
            //reset all changed blocks later
            @Override
            public void run() {
                for(Location loc : changed.keySet()) {
                    Material material = changed.get(loc);
                    ((CraftPlayer) player).sendBlockChange(loc, material, (byte) 0);
                }
            }
        }, 400); //20 seconds
    }

    public static void highlightBlock(Player player, Location location, Material material) {
        ((CraftPlayer) player).sendBlockChange(location, material, (byte) 0);
    }

}
