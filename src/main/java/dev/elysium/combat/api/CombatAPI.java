package dev.elysium.combat.api;

import dev.elysium.combat.ElysiumCombat;
import dev.elysium.combat.clazz.ClassData;
import dev.elysium.combat.clazz.PlayerClass;
import dev.elysium.combat.stats.CombatStats;
import dev.elysium.core.api.CoreAPI;
import org.bukkit.entity.Player;
import java.util.UUID;

public final class CombatAPI {
    private static ElysiumCombat combat;
    public static void init(ElysiumCombat plugin) { combat = plugin; }

    public static PlayerClass  getClass(Player p)     { return combat.getClassManager().getPlayerClass(p.getUniqueId()); }
    public static PlayerClass  getClass(UUID uuid)    { return combat.getClassManager().getPlayerClass(uuid); }
    public static ClassData    getClassData(Player p) { return combat.getClassManager().getClassData(getClass(p)); }
    public static CombatStats  getStats(Player p)     { return combat.getStatsManager().getStats(p); }

    // Dung CoreAPI thay vi ManaManager truc tiep
    public static int     getMana(Player p)          { return CoreAPI.getMana(p); }
    public static boolean useMana(Player p, int amt) { return CoreAPI.useMana(p, amt); }

    public static ElysiumCombat getCombat()           { return combat; }
}
