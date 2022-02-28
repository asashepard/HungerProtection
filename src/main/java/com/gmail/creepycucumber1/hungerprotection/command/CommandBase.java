/*
 * Copyright 2020 Gabriel Keller
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.gmail.creepycucumber1.hungerprotection.command;

import com.gmail.creepycucumber1.hungerprotection.HungerProtection;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;

public abstract class CommandBase {

    protected HungerProtection plugin;
    protected String command, description, usage;
    protected List<String> aliases;

    public CommandBase(HungerProtection plugin, String command, String description, String usage, String... aliases){
        this.plugin = plugin;
        this.command = command;
        this.description = description;
        this.usage = usage;
        if(aliases!=null)
            this.aliases = Arrays.asList(aliases);
    }

    public abstract boolean execute(CommandSender sender, String[] args);

    public String getCommand() {
        return command;
    }

    public String getDescription() {
        return description;
    }

    public String getUsage() {
        return usage;
    }

    public List<String> getAliases() {
        return aliases;
    }

    public HungerProtection getPlugin() {
        return plugin;
    }
}