package com.github.margeobur.noirpvp.listeners;

import com.github.margeobur.noirpvp.FSDatabase;
import com.github.margeobur.noirpvp.NoirPVPConfig;
import com.github.margeobur.noirpvp.NoirPVPPlugin;
import com.github.margeobur.noirpvp.PVPPlayer;
import com.github.margeobur.noirpvp.trials.TrialManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Responsible for initiating the loading of player data into memory, as well as the saving of player data
 * to the disk.
 *
 * So that there isn't yet another class, this class is responsible for stopping player movement when they are
 * jailed.
 */
public class PlayerServerListener implements Listener {

    // the time to wait before committing the player data to disk and removing from memory
    private static final int WAIT_TIME_BEFORE_SAVING = 5;
    private static BukkitRunnable printTask;
    private LocalDateTime lastTeleport = LocalDateTime.now();

    public PlayerServerListener() { }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        UUID playerID = event.getPlayer().getUniqueId();
        // addIfNotPresent makes a call to FSDatabase#getPlayerPVPbyUUID if it needs to
        PVPPlayer.addIfNotPresent(playerID);
        PVPPlayer playerPVP = PVPPlayer.getPlayerByUUID(playerID);
        playerPVP.resumeCooldowns();
        TrialManager.getInstance().rescheduleJailReleasePotentially(playerID);
        TrialManager.getInstance().retryOfflineTrialPotentially(playerID);
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PVPPlayer playerPVP = PVPPlayer.getPlayerByUUID(player.getUniqueId());

        playerPVP.pauseCooldowns();

        FSDatabase.getInstance().savePlayerPVP(playerPVP);
        BukkitRunnable runInfoDel = new BukkitRunnable() {
            @Override
            public void run() {
                if(!player.isOnline()) {
                    FSDatabase.getInstance().savePlayerPVP(playerPVP);
                    PVPPlayer.removePlayer(player.getUniqueId());
                }
            }
        };

        int ticks = WAIT_TIME_BEFORE_SAVING * 60 * 20;
        runInfoDel.runTaskLater(NoirPVPPlugin.getInstance(), ticks);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        UUID playerID = event.getPlayer().getUniqueId();
        PVPPlayer possibleDefendant = TrialManager.getInstance().currentDefendant();
//        if(!JailCell.playerOnShortlist(playerID)) {
        if(possibleDefendant == null || !possibleDefendant.getID().equals(playerID)) {
            return;
        }
//        }

        Location trialDock = NoirPVPConfig.getInstance().getCourtDock();
        Location playerLocation = event.getTo();
        if(playerLocation.getBlockX() > trialDock.getBlockX() + 1
            || playerLocation.getBlockX() < trialDock.getBlockX() - 1
            || playerLocation.getBlockY() > trialDock.getBlockY() + 1
            || playerLocation.getBlockY() < trialDock.getBlockY() - 1
            || playerLocation.getBlockZ() > trialDock.getBlockZ() + 2 // +2 to allow them to jump
            || playerLocation.getBlockZ() < trialDock.getBlockZ() - 1) {

            if(lastTeleport.plusSeconds(3).isBefore(LocalDateTime.now())) {
                lastTeleport = LocalDateTime.now();
                event.getPlayer().teleport(trialDock);
            }
        }
    }
}
