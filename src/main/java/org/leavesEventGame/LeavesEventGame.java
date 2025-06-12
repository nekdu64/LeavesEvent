package org.leavesEventGame;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.simpleEventManager.api.EventGame;
import org.leavesEventGame.commands.SetArenaCommand;
import org.leavesEventGame.game.MyMiniGame;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LeavesEventGame extends JavaPlugin implements EventGame {

    private MyMiniGame game;

    @Override
    public void onEnable() {
        this.getCommand("Leaves").setExecutor(new SetArenaCommand(this));
        getLogger().info("LeavesEvent enabled!");



        // 🔒 Enregistrement du listener de protection des feuilles
        Bukkit.getPluginManager().registerEvents(new org.leavesEventGame.Util.LeafDecayBlocker(this), this);

        // Sauvegarde le fichier config.yml depuis le JAR si non présent
        saveDefaultConfig();

        // Exemple de lecture depuis le config.ym
    }

    // Mon Manager (SEM) a besoin de ça
    @Override
    public void start(List<Player> players) {
        this.game = new MyMiniGame(players, this);
        game.start(getMode());
    }

    @Override
    public void stop() {
        if (game != null) {
            game.stop();
            game = null; // On check comme ca qu'un event est bien fini
        }
    }

    @Override
    public boolean hasWinner() {
        return game != null && !game.running;
    }

    @Override
    public List<Player> getWinners() {
        return game != null ? game.getWinners() : List.of();
    }

    @Override
    public void Removeplayer(Player player) {
        if (game != null && game.winner==null) {
            game.eliminate(player);
        }
    }

    @Override
    public String getEventName() {
        return "Leaves";
    }

    @Override
    public String getEventDescription() {
        return "Event Leaves, regardez en haut, ne vous faites pas applatir.";
    }

    private String mode;

    @Override
    public void setMode(String mode) {
        this.mode = (mode != null) ? mode.toLowerCase() : Randomconfig();
    }

    public String Randomconfig() {
        ConfigurationSection section = this.getConfig().getConfigurationSection("LeavesConfig");

        if (section == null || section.getKeys(false).isEmpty()) return null;

        List<String> keys = new ArrayList<>(section.getKeys(false));
        int randomIndex = (int) (Math.random() * keys.size());
        String chosenKey = keys.get(randomIndex);

        return chosenKey;
    }


    @Override
    public String getMode() {
        return mode;
    }


    public void resetarena(Location pos1, Location pos2) {

        // 📍 Restauration simple : reload les blocs directement depuis les coords


        for (int x = Math.min(pos1.getBlockX(), pos2.getBlockX()); x <= Math.max(pos1.getBlockX(), pos2.getBlockX()); x++) {
            for (int y = Math.min(pos1.getBlockY(), pos2.getBlockY()); y <= Math.max(pos1.getBlockY(), pos2.getBlockY()); y++) {
                for (int z = Math.min(pos1.getBlockZ(), pos2.getBlockZ()); z <= Math.max(pos1.getBlockZ(), pos2.getBlockZ()); z++) {
                    Location blockLoc = new Location(pos1.getWorld(), x, y, z);
                    if (blockLoc.getBlock().getType().toString().contains("Leaves")) {
                        blockLoc.getBlock().setType(Material.AIR); // 🧊 Tu enleve les Leaves
                    }
                }
            }
        }
    }

    public boolean isRunning() {
        return game.running;
    }
    public MyMiniGame getGame() {
        return game;
    }
}