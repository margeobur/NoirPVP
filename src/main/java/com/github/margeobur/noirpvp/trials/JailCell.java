package com.github.margeobur.noirpvp.trials;

import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

import java.util.*;

@SerializableAs("JailCell")
public class JailCell implements ConfigurationSerializable {

    private static List<JailCell> jailCells = new ArrayList<>();   // says whether or not each cell is occupied

    private Location warp;
    private List<UUID> occupants = new ArrayList<>();
    private UUID singleOccupant;
    private boolean canHouseMany;

    public JailCell(Location cellWarpLocation, boolean canHouseMany) {
        warp = cellWarpLocation;
        this.canHouseMany = canHouseMany;
    }

    public JailCell(Map<String, Object> serialMap) {
        if(serialMap.containsKey("warp")) {
            warp = (Location) serialMap.get("warp");
        }

        if(serialMap.containsKey("canHouseMany") && ((Boolean) serialMap.get("canHouseMany"))) {
            if(serialMap.containsKey("occupants")) {
                List<String> occupantIDStrs = (List<String>) serialMap.get("occupants");
                for (String occIDStr : occupantIDStrs) {
                    occupants.add(UUID.fromString(occIDStr));
                }
            }
        } else {
            if(serialMap.containsKey("singleOccupant")) {
                singleOccupant = UUID.fromString((String) serialMap.get("singleOccupant"));
            }
        }
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> serialMap = new HashMap<>();
        serialMap.put("warp", warp);

        serialMap.put("canHouseMany", canHouseMany);
        if(canHouseMany) {
            List<String> occupantIDStrs = new ArrayList<>();
            for(UUID occID: occupants) {
                occupantIDStrs.add(occID.toString());
            }
            serialMap.put("occupants", occupantIDStrs);
        } else {
            serialMap.put("singleOccupant", singleOccupant);
        }
        return serialMap;
    }

    /* static JailCell management */
    public static void setCells(List<JailCell> cells) {
        jailCells = cells;
    }

    public static List<JailCell> getCells() {
        return jailCells;
    }

    public static void addNewCell(Location cellWarpLocation) {
        if(jailCells.isEmpty()) {
            jailCells.add(new JailCell(cellWarpLocation, true));
        } else {
            jailCells.add(new JailCell(cellWarpLocation, false));
        }
    }

    /**
     * This method gets a cell that is free to be occupied. The cell might not in fact be empty, but if an empty
     * one is available it will be selected
     * @return the {@link Location} to warp a player to when jailing them
     */
    public static Location getVacantCellFor(UUID playerID) {
        for(JailCell cell: jailCells) {
             if(!cell.canHouseMany && cell.singleOccupant == null) {
                cell.singleOccupant = playerID;
                return cell.warp;
            }
        }
        for(JailCell cell: jailCells) {
            if(cell.canHouseMany && cell.occupants.isEmpty()) {
                cell.occupants.add(playerID);
                return cell.warp;
            }
        }
        jailCells.get(0).occupants.add(playerID);
        return jailCells.get(0).warp;
    }

    /**
     * Searches for the cell that the player is in and deletes them from the cell
     */
    public static void releasePlayer(UUID playerId) {
        for(JailCell cell: jailCells) {
            if(cell.canHouseMany && cell.occupants.contains(playerId)) {
                cell.occupants.remove(playerId);
                return;
            } else if(cell.singleOccupant.equals(playerId)) {
                cell.singleOccupant = null;
                return;
            }
        }
    }
}