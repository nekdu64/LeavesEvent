package org.leavesEventGame.Util;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.List;

public class EventInfoDisplayer {

    public static void afficherChat(List<Player> players,int phaseIndex, int delay, int randomTickSpeed, boolean pvp, String commandDescription) {
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage(ChatColor.DARK_GREEN + "=== §a§lÉVÉNEMENT FEUILLE §r" + ChatColor.DARK_GREEN + "===");
        Bukkit.broadcastMessage("§aPhase "+phaseIndex);
        Bukkit.broadcastMessage("§f⏱️ Durée phase : §e" + delay +"secondes");
        Bukkit.broadcastMessage("§f🍃 RandomTickSpeed : §e" + randomTickSpeed);
        Bukkit.broadcastMessage("§f⚔️ PVP activé : " + (pvp ? "§aOui" : "§cNon"));
        Bukkit.broadcastMessage("§f📜 Largage : §e" + commandDescription);
        Bukkit.broadcastMessage(ChatColor.DARK_GREEN + "===========================");
        Bukkit.broadcastMessage("");

        for (Player player : players) {
            player.sendTitle("§a§lPhase: " + phaseIndex, "§ePvP: " + " X " + (pvp ? "§aOui" : "§cNon")+ " &eLargage: "+commandDescription, 10, 60, 20);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 1.0f, 0.5f);
        }

    }
}