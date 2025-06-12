package org.leavesEventGame.Util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.LeavesDecayEvent;
import org.leavesEventGame.LeavesEventGame;

public class LeafDecayBlocker implements Listener {

    private final LeavesEventGame plugin;
    private final Location pos1;
    private final Location pos2;

    public LeafDecayBlocker(LeavesEventGame plugin) {
        this.plugin = plugin;
        String world = plugin.getConfig().getString("arena.pos1.world");
        Bukkit.getLogger().info("TESTTSSTSTSTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT");
// pos1
        double x1 = plugin.getConfig().getDouble("arena.pos1.x");
        double y1 = plugin.getConfig().getDouble("arena.pos1.y");
        double z1 = plugin.getConfig().getDouble("arena.pos1.z");
        Location pos1 = new Location(Bukkit.getWorld(world), x1, y1, z1);

// pos2
        double x2 = plugin.getConfig().getDouble("arena.pos2.x");
        double y2 = plugin.getConfig().getDouble("arena.pos2.y");
        double z2 = plugin.getConfig().getDouble("arena.pos2.z");
        Location pos2 = new Location(Bukkit.getWorld(world), x2, y2, z2);

        this.pos1 = pos1;
        this.pos2 = pos2;
    }

    @EventHandler
    public void onLeafDecay(LeavesDecayEvent event) {
        // Si le jeu n'existe pas ou qu'il est terminé => empêcher la disparition des feuilles
        if (plugin == null || plugin.getGame() == null || !plugin.getGame().running) {
            Location loc = event.getBlock().getLocation();
            if (isLeafBlock(event.getBlock().getType()) && isInArena(loc)) {
                event.setCancelled(true);
            }
        }
    }
    private boolean isInArena(Location loc) {
        int x1 = Math.min(pos1.getBlockX(), pos2.getBlockX());
        int x2 = Math.max(pos1.getBlockX(), pos2.getBlockX());
        int y1 = Math.min(pos1.getBlockY(), pos2.getBlockY());
        int y2 = Math.max(pos1.getBlockY(), pos2.getBlockY());
        int z1 = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        int z2 = Math.max(pos1.getBlockZ(), pos2.getBlockZ());

        boolean in = loc.getWorld().equals(pos1.getWorld()) &&
                loc.getBlockX() >= x1 && loc.getBlockX() <= x2 &&
                loc.getBlockY() >= y1 && loc.getBlockY() <= y2 &&
                loc.getBlockZ() >= z1 && loc.getBlockZ() <= z2;

        return in;
    }


    private boolean isLeafBlock(Material material) {
        return material.toString().contains("LEAVES");
    }
}