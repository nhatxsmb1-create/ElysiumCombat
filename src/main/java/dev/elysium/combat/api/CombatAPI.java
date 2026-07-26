package dev.elysium.combat.api;

import dev.elysium.combat.ElysiumCombat;
import dev.elysium.combat.clazz.ClassData;
import dev.elysium.combat.clazz.PlayerClass;
import dev.elysium.combat.stats.CombatStats;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Public API cho ElysiumCombat.
 * Cac plugin khac (ElysiumWar, ElysiumAdventure...) goi class nay.
 *
 * Vi du:
 *   PlayerClass pc = CombatAPI.getClass(player);
 *   CombatStats stats = CombatAPI.getStats(player);
 *   double defense = stats.getDefense(); // % giam sat thuong
 */
public final class CombatAPI {

    private static ElysiumCombat combat;
    public static void init(ElysiumCombat plugin) { combat = plugin; }

    public static PlayerClass  getClass(Player p)     { return combat.getClassManager().getPlayerClass(p.getUniqueId()); }
    public static PlayerClass  getClass(UUID uuid)    { return combat.getClassManager().getPlayerClass(uuid); }
    public static ClassData    getClassData(Player p) { return combat.getClassManager().getClassData(getClass(p)); }
    public static CombatStats  getStats(Player p)     { return combat.getStatsManager().getStats(p); }
    public static int          getMana(Player p)      { return combat.getManaManager().getMana(p); }
    public static boolean      useMana(Player p, int amount) { return combat.getManaManager().useMana(p, amount); }
    public static ElysiumCombat getCombat()           { return combat; }
}
