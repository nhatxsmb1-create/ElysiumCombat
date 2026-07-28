package dev.elysium.combat.listener;

import dev.elysium.combat.ElysiumCombat;
import dev.elysium.combat.clazz.PlayerClass;
import dev.elysium.combat.stats.CombatStats;
import dev.elysium.core.api.CoreAPI;
import dev.elysium.core.event.ElysiumLevelUpEvent;
import dev.elysium.core.util.ColorUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class CombatListener implements Listener {

    private final ElysiumCombat plugin;
    public CombatListener(ElysiumCombat plugin) { this.plugin = plugin; }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            Player p = e.getPlayer();
            plugin.getClassManager().loadPlayerClass(p);
            plugin.getStatsManager().apply(p);
            plugin.getClassManager().giveSkillItems(p);
        }, 20L);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        plugin.getStatsManager().remove(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player victim)) return;
        CombatStats stats = plugin.getStatsManager().getStats(victim);
        if (stats.getDefense() > 0) e.setDamage(stats.applyDefense(e.getDamage()));
    }

    /**
     * Khi player len level:
     * 1. Refresh skill item lore tren hotbar
     * 2. Notify level milestone cho combat (unlock skill tier)
     */
    @EventHandler
    public void onLevelUp(ElysiumLevelUpEvent e) {
        Player player  = e.getPlayer();
        int    newLevel = e.getNewLevel();
        PlayerClass pc = plugin.getClassManager().getPlayerClass(player.getUniqueId());

        // Refresh skill items
        if (pc != PlayerClass.NONE) {
            plugin.getSkillManager().refreshHotbarSkills(player, pc);
        }

        // Thong bao moc level quan trong
        if (newLevel == 10 || newLevel == 25 || newLevel == 50) {
            player.sendMessage(ColorUtil.color(
                "&c[Combat] &7Dat Level &e" + newLevel +
                "&7! Skill tier moi co the duoc mo khoa trong tuong lai."));
        }
    }
}
