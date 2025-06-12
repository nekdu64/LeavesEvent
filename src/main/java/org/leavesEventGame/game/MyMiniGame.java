package org.leavesEventGame.game;

import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitTask;
import org.leavesEventGame.Util.Countdown;
import org.leavesEventGame.Util.DelayUtil;
import org.leavesEventGame.Util.EliminationMessage;
import org.bukkit.*;
import org.bukkit.block.BlockState;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.meta.FireworkMeta;
import org.leavesEventGame.Util.EventInfoDisplayer;
import org.simpleEventManager.SimpleEventManager;
import org.simpleEventManager.utils.EventUtils;
import org.leavesEventGame.LeavesEventGame;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class MyMiniGame implements Listener {

    private BukkitTask waterCheckTask;
    private final DelayUtil delayUtil;
    private final Countdown countdown;
    private final List<Player> players;
    public final int countdowntime;
    private final List<Player> ranking = new ArrayList<>();
    private final List<BlockState> originalBlocks = new ArrayList<>();
    private final LeavesEventGame plugin;
    public Player winner;
    public boolean running = false;
    private boolean Finale = false;
    private int startPlayerCount;
    private BossBar bossBar;
    private List<Location> Alldispenser = new ArrayList<>();
    private boolean pvp = false;


    public MyMiniGame(List<Player> players, LeavesEventGame plugin) {
        this.countdown = new Countdown(plugin);
        this.countdowntime = 1;
        this.players = new ArrayList<>(players);
        this.plugin = plugin;
        this.startPlayerCount = players.size();
        this.delayUtil = new DelayUtil(plugin);
    }


    public void start(String Forceconfig) { // Start l'event

        Bukkit.getPluginManager().registerEvents(MyMiniGame.this, plugin);
        //reference a mon manager
        SimpleEventManager sem = (SimpleEventManager) Bukkit.getPluginManager().getPlugin("SimpleEventManager");
        // Pour tp les joueurs au spawn de l'event grace au spawn set dans le manager avec /event setspawn Leaves
        Location loc = EventUtils.getEventSpawnLocation(sem, plugin.getEventName());
        pvp = false;
        startWaterCheck();
        for (Player player : players) {
            player.teleport(loc);
        }
        // Enregistre pour reset la map
        Location pos1 = getLoc("arena.pos1");
        Location pos2 = getLoc("arena.pos2");


        originalBlocks.clear();
        for (int x = Math.min(pos1.getBlockX(), pos2.getBlockX()); x <= Math.max(pos1.getBlockX(), pos2.getBlockX()); x++) {
            for (int y = Math.min(pos1.getBlockY(), pos2.getBlockY()); y <= Math.max(pos1.getBlockY(), pos2.getBlockY()); y++) {
                for (int z = Math.min(pos1.getBlockZ(), pos2.getBlockZ()); z <= Math.max(pos1.getBlockZ(), pos2.getBlockZ()); z++) {
                    Location blockLoc = new Location(pos1.getWorld(), x, y, z);
                    originalBlocks.add(blockLoc.getBlock().getState());
                }
            }
        }

         // Important pour que tout se reset bien (l'instance de l'event + les recompenses ect)
        // Créer la bossbar - HUD
        bossBar = Bukkit.createBossBar("§eJoueurs restants : " + players.size(), BarColor.BLUE, BarStyle.SEGMENTED_10);
        bossBar.setProgress(1.0);
        for (Player player : players) {
            bossBar.addPlayer(player);
            player.setGameMode(GameMode.SURVIVAL);
        }

        running = true;
        pos1.getWorld().setDifficulty(Difficulty.EASY);
        pos1.getWorld().setGameRule(GameRule.DO_TILE_DROPS, false);
        pos1.getWorld().setGameRule(GameRule.RANDOM_TICK_SPEED, 0);
// Vérifie si la config existe dans LeavesConfig, sinon sélectionne une config aléatoire
        ConfigurationSection listConfig = plugin.getConfig().getConfigurationSection("LeavesConfig");
        String configChoisie = null;
        if (listConfig != null) {
            for (String key : listConfig.getKeys(false)) {
                if (key.equalsIgnoreCase(Forceconfig)) {
                    configChoisie = key; // utiliser le vrai nom avec la bonne casse
                    break;
                }
            }
        }
        if (configChoisie == null) {
            configChoisie = plugin.Randomconfig();
        }
        for (Player player : players) {
            player.sendTitle("§c§lFeuilles", "§e"+configChoisie, 10, 100, 20);
        }

        String finalConfigChoisie = configChoisie;
        delayUtil.delay(100, () -> {

            // Start Countdown -
            countdown.startCountdownChat(players, 10, () -> {
                // Instructions à exécuter une fois le décompte terminé
                DépartLeaves("LeavesConfig."+finalConfigChoisie); //StartEvent
            });;

        });
    }


    public void normalstop() {

        Location pos1 = getLoc("arena.pos1");
        HandlerList.unregisterAll(this);
        String WorldEvent = pos1.getWorld().getName();
        Bukkit.getWorld(WorldEvent).setGameRule(GameRule.RANDOM_TICK_SPEED, 3);
        Bukkit.getWorld(WorldEvent).setGameRule(GameRule.DO_TILE_DROPS, true);
        Bukkit.getWorld(WorldEvent).setDifficulty(Difficulty.PEACEFUL);
        stopWaterCheck();
        pvp = true;
        if (bossBar != null) {
            bossBar.removeAll();
            bossBar = null;
        }
        delayUtil.delay(220, () -> {
            running = false;
            resetArena();
            delayUtil.cancelAll(); // Stop toutes les tâches programmées
            countdown.cancelAll(); // Stop toutes les tâches programmées
        });

    }




    private void DépartLeaves(String Configchoisie) {

        ConfigurationSection ListPhases = plugin.getConfig().getConfigurationSection(Configchoisie);
        int totalDelay = 0;
        int i= 0;
        
        EventInfoDisplayer Chathandler = new EventInfoDisplayer();
        for (String key : ListPhases.getKeys(false)) {

            i++;
            int phaseIndex = i;
            int delay = plugin.getConfig().getInt(Configchoisie + ".Phase" + phaseIndex + ".delay");
            int delayintick = Math.max(20 * delay, 160); //on s'assure que delay est au moins a 5 seconde
            int randomtickspeed = plugin.getConfig().getInt(Configchoisie + ".Phase" + phaseIndex + ".randomtickspeed");
            int dispenserMin = plugin.getConfig().getInt(Configchoisie + ".Phase" + phaseIndex + ".dispenser.min");
            int dispenserMax = plugin.getConfig().getInt(Configchoisie + ".Phase" + phaseIndex + ".dispenser.max");
            List<String> commands = (List<String>) plugin.getConfig().getList(Configchoisie + ".Phase" + phaseIndex + ".dispenser.command");
            String commandDescription = plugin.getConfig().getString(Configchoisie + ".Phase" + phaseIndex + ".dispenser.command_description");
            boolean pvptemp = plugin.getConfig().getBoolean(Configchoisie + ".Phase" + phaseIndex + ".pvp");

            delayUtil.delay(totalDelay-160, () -> {
                //Afficher les info de la prochaine vague dans le chat
                Chathandler.afficherChat(players, phaseIndex, delay, randomtickspeed, pvptemp, commandDescription);

                countdown.startCountdown(players, 8, () -> {
                    LauchNextPhase(delay,randomtickspeed,pvptemp,dispenserMax,dispenserMin,commands);
                });

            });
            totalDelay+=delayintick;

        }
    }

    private void LauchNextPhase(int delay,int randomtickspeed, boolean pvptemp, int dispenserMax, int dispenserMin, List<String> commands) {
        Location pos1 = getLoc("arena.pos1");
        Location pos2 = getLoc("arena.pos2");
        World world = pos1.getWorld();

        world.setGameRule(GameRule.RANDOM_TICK_SPEED, randomtickspeed);
        pvp = pvptemp ;
        int nbDispenser = ThreadLocalRandom.current().nextInt(dispenserMin, dispenserMax+1);
        delay = 20*delay;
        if (nbDispenser == 0 || commands.isEmpty()) {return;}
        // on récupère tous les bloc de leaves encore dans l'arene
        List<BlockState> leavesBlocks = new ArrayList<>();
        leavesBlocks.clear();
        for (int x = Math.min(pos1.getBlockX(), pos2.getBlockX()); x <= Math.max(pos1.getBlockX(), pos2.getBlockX()); x++) {
            for (int y = Math.min(pos1.getBlockY(), pos2.getBlockY()); y <= Math.max(pos1.getBlockY(), pos2.getBlockY()); y++) {
                for (int z = Math.min(pos1.getBlockZ(), pos2.getBlockZ()); z <= Math.max(pos1.getBlockZ(), pos2.getBlockZ()); z++) {
                    Location blockLoc = new Location(world, x, y, z);
                    if (blockLoc.getBlock().getType().toString().contains("LEAVES")) {
                        leavesBlocks.add(blockLoc.getBlock().getState());
                    }
                }
            }
        }

        //on place des dispenser random en y max au dessus de ces blocks de leaves choisi aléatoirement
        //Enlève les anciens dispenser
        for (Location oldLoc : Alldispenser) {
            oldLoc.getBlock().setType(Material.AIR); // ou remettre des feuilles si tu préfères
        }
        Alldispenser.clear();

        int actualDispenserCount = Math.min(nbDispenser, leavesBlocks.size());
        Collections.shuffle(leavesBlocks); // mélange la liste comme ça le block 0 est random
        for (int i = 0; i < actualDispenserCount; i++) {
            Location dispenserBlock = leavesBlocks.get(i).getLocation();
            dispenserBlock.setY(Math.max(pos1.getBlockY(), pos2.getBlockY()));
            dispenserBlock.getBlock().setType(Material.DISPENSER);
            Directional directional = (Directional) dispenserBlock.getBlock().getBlockData();
            directional.setFacing(BlockFace.DOWN);
            dispenserBlock.getBlock().setBlockData(directional);
            Alldispenser.add(dispenserBlock);
        }

        delayUtil.delay(delay/4, () -> {
            //on execute la commande pour faire pop le creeper sous le dispenser MOUHAHAHAH
            for (int i =0 ; i<Alldispenser.size();i++) {
                Location commandLoc = Alldispenser.get(i).clone().subtract(0, 1, 0);
                int x = commandLoc.getBlockX();
                int y = commandLoc.getBlockY();
                int z = commandLoc.getBlockZ();
                Random random = new Random();
                String command = commands.get(random.nextInt(commands.size()));

                String fullCommand = String.format(
                        "execute in %s positioned %d %d %d run %s",
                        world.getKey(), x, y, z, command
                );

                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), fullCommand);

                for (Player player : players) {
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
                }
            }
        });

    }

    private void updateBossBar() {
        if (bossBar == null) return;

        double progress = Math.max(0.0, (double) players.size() / startPlayerCount);
        bossBar.setProgress(progress);
        bossBar.setTitle("§eJoueurs restants : " + players.size());
    }


    public void startWaterCheck() {
        waterCheckTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!running) return;
            for (Player player : players) {
                Material blockType = player.getLocation().getBlock().getType();
                if (blockType == Material.WATER || blockType == Material.LAVA) {
                    eliminate(player);
                }
            }
        }, 0L, 10L); // tous les 10 ticks (0.5 sec)
    }

    public void stopWaterCheck() {
        if (waterCheckTask != null) {
            waterCheckTask.cancel();
            waterCheckTask = null;
        }
    }

    public void eliminate(Player player) {
        if (!players.contains(player)) return;
        if (!running) return;

        players.remove(player);
        ranking.add(0, player);
        player.setGameMode(GameMode.SPECTATOR);
        SimpleEventManager sem = (SimpleEventManager) Bukkit.getPluginManager().getPlugin("SimpleEventManager");
        player.teleport(EventUtils.getEventSpawnLocation(sem, plugin.getEventName()));
        updateBossBar();


        delayUtil.delay(1, () -> {
            int remaining = players.size();
            if (remaining == 1) {

                EliminationMessage eliminationMessage = new EliminationMessage();
                eliminationMessage.broadcastElimination(player,remaining);
                endgame(players.getFirst());

            } else if (remaining <= 0 && !Finale) {
                Bukkit.broadcastMessage("§c[Leaves] Égalité ! IL FAUT UN VAINQUEUR....");
                delayUtil.delay(80, () -> {
                    Bukkit.broadcastMessage("§c[Leaves] Égalité ! les jurées délibères");
                });
                delayUtil.delay(160, () -> {
                    Bukkit.broadcastMessage("§c[Leaves] Égalité ! les jurées ont tranché");
                });
                delayUtil.delay(240, () -> {
                    Bukkit.broadcastMessage("§c[Leaves] Ayant plus de style c'est "+ranking.getLast()+" qui triomphe");
                });
                endgame(ranking.getLast());
            } else {
                EliminationMessage eliminationMessage = new EliminationMessage();
                eliminationMessage.broadcastElimination(player,remaining);
            }
        });

    }


    private void endgame(Player lastPlayer) {

        if (lastPlayer != null && !ranking.contains(lastPlayer) && lastPlayer.isOnline()) {
            delayUtil.cancelAll(); // Stop toutes les tâches programmées
            countdown.cancelAll(); // Stop toutes les tâches programmées
            winner = lastPlayer;
            // Limite les feux d'artifice à 3
            for (int i = 0; i < 3; i++) {
                delayUtil.delay(i*15, () -> {
                    spawnFireworks(lastPlayer);
                });
            }

            ranking.add(0, winner);
            Bukkit.broadcastMessage("§6[Leaves] Le gagnant est : §e" + winner.getName());
        } else {
            Bukkit.broadcastMessage("§c[Leaves] Erreur : dernier joueur vivant introuvable ou déjà éliminé.");
        }

        // Termine proprement le jeu après 7,5 secondes
        normalstop();


    }

    public void spawnFireworks(Player player) {
        Location loc = player.getLocation().add(0, 1, 0); // légèrement au-dessus du joueur

        for (int i = 0; i < 3; i++) {

            Firework firework = (Firework) player.getWorld().spawnEntity(loc, EntityType.FIREWORK_ROCKET);
            FireworkMeta meta = firework.getFireworkMeta();

            FireworkEffect effect = FireworkEffect.builder()
                    .withColor(Color.RED)
                    .withColor(Color.YELLOW)
                    .with(FireworkEffect.Type.BALL_LARGE)
                    .flicker(true)
                    .trail(true)
                    .build();

            meta.addEffect(effect);
            meta.setPower(1);
            firework.setFireworkMeta(meta);
        }
    }
    public List<Player> getWinners() {
        return new ArrayList<>(ranking);
    }

    private void resetArena() {
        for (BlockState state : originalBlocks) {
            state.update(true, false);
        }
    }

    private Location getLoc(String path) {
        String world = plugin.getConfig().getString(path + ".world");
        double x = plugin.getConfig().getDouble(path + ".x");
        double y = plugin.getConfig().getDouble(path + ".y");
        double z = plugin.getConfig().getDouble(path + ".z");
        return new Location(Bukkit.getWorld(world), x, y, z);
    }
    
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (!players.contains(player)) return;
        if (!running) return;
        // pas le droit de poser de blocs
        event.setCancelled(true);
    }
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (!players.contains(player)) return;
        if (!running) return;
        // pas le droit de casser de blocs
        event.setCancelled(true);
    }
    @EventHandler
    public void onDamage(EntityDamageEvent event) {

        if (event.getEntity() instanceof  Player) {
            Player player = (Player) event.getEntity();
            if (!players.contains(player)) return;
            if (!running) return;
            event.setDamage(0);
        }
        else if (event.getEntity().getLocation().getWorld().getName().equals(players.getFirst().getWorld().getName())) {
            event.setDamage(0);
        }
    }


    @EventHandler
    public void togglePvp(EntityDamageByEntityEvent event) {
        if (!pvp) { // ta variable de contrôle du PvP

            Entity damager = event.getDamager();
            Entity victim = event.getEntity();

            // Empêche uniquement les dégâts entre joueurs
            if (damager instanceof Player && victim instanceof Player) {
                event.setCancelled(true);
                damager.sendMessage("§cPVP désactivé");
            }
        }
    }

    public void stop() {

        Location pos1 = getLoc("arena.pos1");
        HandlerList.unregisterAll(this);
        String WorldEvent = pos1.getWorld().getName();
        Bukkit.getWorld(WorldEvent).setGameRule(GameRule.RANDOM_TICK_SPEED, 3);
        Bukkit.getWorld(WorldEvent).setGameRule(GameRule.DO_TILE_DROPS, true);
        Bukkit.getWorld(WorldEvent).setDifficulty(Difficulty.PEACEFUL);
        stopWaterCheck();
        pvp = true;
        if (bossBar != null) {
            bossBar.removeAll();
            bossBar = null;
        }
        delayUtil.delay(220, () -> {
            running = false;
            Bukkit.getWorld(WorldEvent).setDifficulty(Difficulty.EASY);
            resetArena();
            delayUtil.cancelAll(); // Stop toutes les tâches programmées
            countdown.cancelAll(); // Stop toutes les tâches programmées
        });

    }
}
