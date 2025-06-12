package org.leavesEventGame.Util;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.leavesEventGame.LeavesEventGame;
import org.leavesEventGame.game.MyMiniGame;

import java.util.ArrayList;
import java.util.List;

public class Countdown {
    private int countdownTaskId = -1;
    private final LeavesEventGame plugin;
    private final List<Integer> taskIds = new ArrayList<>();

    public Countdown(LeavesEventGame plugin) {
        this.plugin = plugin;
    }

    public void startCountdownChat(List<Player> players, int initialCountdown, Runnable onFinish) {
        final int[] countdown = {initialCountdown};

        countdownTaskId = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
            @Override
            public void run() {
                if (countdown[0] <= 0) {
                    Bukkit.broadcastMessage("§a§lGO !");
                    for (Player player : players) {
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 1.0f, 1f);
                        player.sendTitle("§a§lGO !", "", 10, 20, 20);
                    }

                    if (onFinish != null) {
                        onFinish.run(); // << instructions personnalisées
                    }

                    Bukkit.getScheduler().cancelTask(countdownTaskId);
                    return;
                }

                if (countdown[0] <= 3) {
                    for (Player player : players) {
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 1.0f, 0.5f);
                        player.sendTitle("§e" + countdown[0], "", 10, 20, 20);
                    }
                }

                Bukkit.broadcastMessage("§eDébut dans §l" + countdown[0] + "s...");
                countdown[0]--;
            }
        }, 0L, 20L).getTaskId();
        taskIds.add(countdownTaskId);
    }

    public void startCountdown(List<Player> players, int initialCountdown, Runnable onFinish) {
        final int[] countdown = { initialCountdown };

        countdownTaskId = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
            @Override
            public void run() {
                if (countdown[0] <= 0) {
                    for (Player player : players) {
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 1.0f, 1f);
                        player.sendTitle("§a§lGO !", "", 10, 20, 20);
                    }

                    if (onFinish != null) {
                        onFinish.run(); // << instructions personnalisées
                    }

                    Bukkit.getScheduler().cancelTask(countdownTaskId);
                    return;
                }

                if (countdown[0] <= 3) {
                    for (Player player : players) {
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 1.0f, 0.5f);
                        player.sendTitle("§e" + countdown[0], "", 10, 20, 20);
                    }
                }
                countdown[0]--;
            }
        }, 0L, 20L).getTaskId();
        taskIds.add(countdownTaskId);
    }
    public void cancelAll() {
        for (int taskId : taskIds) {
            Bukkit.getScheduler().cancelTask(taskId);
        }
        taskIds.clear();
    }
}