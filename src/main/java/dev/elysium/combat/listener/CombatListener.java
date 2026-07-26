package dev.elysium.combat.listener;

import dev.elysium.combat.ElysiumCombat;
import dev.elysium.combat.clazz.PlayerClass;
import dev.elysium.combat.stats.CombatStats;
import dev.elysium.core.api.CoreAPI;
import org.bukkit.entity.Arrow;
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
            plugin.getClassManager().loadPlayerClass(e.getPlayer());
            plugin.getStatsManager().apply(e.getPlayer());
        }, 20L); // 1 giay sau join (cho ElysiumCore load xong)
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        plugin.getStatsManager().remove(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent e) {
        // --- Ten tu skill (arrow rain / fire arrow) ---
        if (e.getDamager() instanceof Arrow arrow && arrow.hasMetadata("elysium_skill_damage")) {
            double skillDmg = (double) arrow.getMetadata("elysium_skill_damage").get(0).value();
            e.setDamage(skillDmg);
            arrow.removeMetadata("elysium_skill_damage", plugin);
            return;
        }

        // --- Xu ly damage player danh ---
        if (e.getDamager() instanceof Player attacker) {
            PlayerClass pc = plugin.getClassManager().getPlayerClass(attacker.getUniqueId());
            if (pc == PlayerClass.NONE) return;
            // Bonus damage da duoc ap dung qua Attribute modifier, khong can nhan them
        }

        // --- Xu ly player nhan damage ---
        if (e.getEntity() instanceof Player victim) {
            CombatStats stats = plugin.getStatsManager().getStats(victim);
            if (stats.getDefense() > 0) {
                double reduced = stats.applyDefense(e.getDamage());
                e.setDamage(reduced);
            }
        }
    }
          }
