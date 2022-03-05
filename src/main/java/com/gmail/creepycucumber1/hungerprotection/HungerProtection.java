package com.gmail.creepycucumber1.hungerprotection;

import com.gmail.creepycucumber1.hungerprotection.claim.ClaimManager;
import com.gmail.creepycucumber1.hungerprotection.claim.PlayerManager;
import com.gmail.creepycucumber1.hungerprotection.command.*;
import com.gmail.creepycucumber1.hungerprotection.data.DataManager;
import com.gmail.creepycucumber1.hungerprotection.event.EventManager;
import com.gmail.creepycucumber1.hungerprotection.event.PacketManager;
import com.gmail.creepycucumber1.hungerprotection.items.ClaimInspectionTool;
import com.gmail.creepycucumber1.hungerprotection.items.ClaimTool;
import com.gmail.creepycucumber1.hungerprotection.items.GUIManager;
import com.gmail.creepycucumber1.hungerprotection.runnable.GeneralMonitor;
import com.gmail.creepycucumber1.hungerprotection.util.TextUtil;
import net.ess3.api.IEssentials;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;

public final class HungerProtection extends JavaPlugin {

    private IEssentials ess;
    private Economy vault;
    private ArrayList<CommandBase> commands;
    private PacketManager packetManager;
    private DataManager dataManager;
    private ClaimManager claimManager;
    private PlayerManager playerManager;
    private GUIManager guiManager;

    public static HungerProtection plugin;

    @Override
    public void onEnable() {
        // Plugin startup logic

        GeneralMonitor generalMonitor = new GeneralMonitor(this);
        generalMonitor.monitorPlayers();
        generalMonitor.monitorPlayerHand();

        packetManager = new PacketManager(this);

        dataManager = new DataManager(this);
        claimManager = new ClaimManager(this);
        playerManager = new PlayerManager(this);
        guiManager = new GUIManager(this);

        commands = new ArrayList<>(registerCommands());

        try{
            ess = (IEssentials) getServer().getPluginManager().getPlugin("Essentials");

            if(ess==null){
                getLogger().warning("HungerProtection failed to hook in with Essentials!");
            }
        }
        catch(Exception ex){
            getLogger().warning("HungerProtection failed to hook in with Essentials!");
        }

        if(getServer().getPluginManager().getPlugin("Vault") != null) {
            RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
            if(rsp != null){
                vault = rsp.getProvider();
            }
        }

        PluginManager pm = Bukkit.getServer().getPluginManager();
        pm.registerEvents(new EventManager(this), this);
        pm.registerEvents(packetManager, this);

        packetManager.initialize();

        ClaimTool.init();
        ClaimInspectionTool.init();

        getLogger().info("HungerProtection has started.");
    }

    @Override
    public void onDisable() {
        getLogger().info("HungerProtection has been disabled.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
        for(CommandBase c : commands)
            if(c.getCommand().equalsIgnoreCase(label) || (c.getAliases() != null && c.getAliases().contains(label))) {
                boolean result = c.execute(sender, args);
                if(!result)
                    sender.sendMessage(TextUtil.convertColor("&cInvalid command usage!\n&7/" + label + " " + c.getUsage()));
            }

        return true;
    }

    private ArrayList<CommandBase> registerCommands() {
        ArrayList<CommandBase> commands = new ArrayList<>();
        commands.add(new AbandonClaimCommand(this));
        commands.add(new AccessTrustCommand(this));
        commands.add(new BuyClaimBlocksCommand(this));
        commands.add(new ClaimCommand(this));
        commands.add(new ClaimExplosionsCommand(this));
        commands.add(new ClaimsCommand(this));
        commands.add(new ClaimToolsCommand(this));
        commands.add(new ClaimTopCommand(this));
        commands.add(new ContainerTrustCommand(this));
        commands.add(new TransferClaimCommand(this));
        commands.add(new TrustCommand(this));
        commands.add(new UntrustCommand(this));

        return commands;
    }

    public IEssentials getEssentials() {
        return ess;
    }
    public Economy getVault() {
        return vault;
    }
    public PacketManager getPacketManager() {
        return packetManager;
    }
    public DataManager getDataManager() {
        return dataManager;
    }
    public ClaimManager cm() {
        return claimManager;
    }
    public PlayerManager getPlayerManager() {
        return playerManager;
    }
    public GUIManager getGuiManager() {
        return guiManager;
    }

}
