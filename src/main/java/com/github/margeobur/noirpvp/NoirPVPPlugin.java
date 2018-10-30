package com.github.margeobur.noirpvp;

import com.github.margeobur.noirpvp.commands.AdminCommands;
import com.github.margeobur.noirpvp.commands.JudicialCommands;
import com.github.margeobur.noirpvp.listeners.PlayerCommandListener;
import com.github.margeobur.noirpvp.listeners.PlayerDeathListener;
import com.github.margeobur.noirpvp.listeners.PlayerServerListener;
import com.github.margeobur.noirpvp.listeners.PlayerCombatListener;
import com.github.margeobur.noirpvp.trials.JailCell;
import com.github.margeobur.noirpvp.trials.TrialEventListener;
import com.github.margeobur.noirpvp.trials.TrialManager;
import me.ryanhamshire.GriefPrevention.GriefPrevention;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

/**
 * The NoirPVP plugin class. Here you'll find command and event registration, initialisation of
 * database and config, etc...
 */
public class NoirPVPPlugin extends JavaPlugin {

    private static NoirPVPPlugin instance;

    public static NoirPVPPlugin getPlugin() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        NoirPVPConfig.getInstance().initConfig();
        GriefPrevention gp = (GriefPrevention) Bukkit.getPluginManager().getPlugin("GriefPrevention");

        // ---------- listeners ----------
        getServer().getPluginManager().registerEvents(new PlayerServerListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerCombatListener(gp), this);
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerCommandListener(), this);
        getServer().getPluginManager().registerEvents(new TrialEventListener(), this);

        JudicialCommands commandHandler = new JudicialCommands();
        getCommand("innocent").setExecutor(commandHandler);
        getCommand("guilty").setExecutor(commandHandler);
        getCommand("crime").setExecutor(commandHandler);
        AdminCommands adminCH = new AdminCommands();
        getCommand("jail").setExecutor(adminCH);
        getCommand("setdock").setExecutor(adminCH);
        getCommand("setrelease").setExecutor(adminCH);
        //this.getCommand("jail").setExecutor(commandHandler);

        JailCell.refreshJailShortlist();
    }
    @Override
    public void onDisable() {
        JailCell.saveJailShortlist();
        FSDatabase.getInstance().saveDatabase();
    }
}
