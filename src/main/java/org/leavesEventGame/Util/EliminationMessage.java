package org.leavesEventGame.Util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class EliminationMessage {
    private static final List<String> eliminationMessages = Arrays.asList(
            "§c%player% n’a pas vu la feuille tourner...",
            "§c%player% a marché sur du vide en toute confiance.",
            "§c%player% pensait que la nature l’aimait. Elle l’a laissé tomber.",
            "§c%player% a cru que les feuilles étaient éternelles.",
            "§c%player% a découvert que l’automne, c’est traître.",
            "§c%player% a pris une décision... fatale.",
            "§c%player% a testé la solidité d’une feuille. Mauvaise idée.",
            "§c%player% s’est fait balayer comme une vieille feuille.",
            "§c%player% est tombé... comme une feuille morte."
    );

    private static final List<String> top3Messages = Arrays.asList(
            "§6%player% termine à la §e3ᵉ place§6, pas mal !",
            "§e%player% finit §e2ᵉ§e, battu par une feuille trop timide.",
            "§a%player% est resté sur ses feuilles jusqu’au bout. Roi du feuillage !"
    );

    public void broadcastElimination(Player player, int remaining) {
        String message;

        if (remaining == 2) {
            message = top3Messages.get(0); // 3e place
        } else if (remaining == 1) {
            message = top3Messages.get(1); // 2e place
        } else {
            message = eliminationMessages.get(new Random().nextInt(eliminationMessages.size()));
        }

        Bukkit.broadcastMessage(message.replace("%player%", player.getName()));
    }
}
