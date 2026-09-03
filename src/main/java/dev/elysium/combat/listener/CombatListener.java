package dev.elysium.combat.listener;

import dev.elysium.combat.ElysiumCombat;
import dev.elysium.combat.clazz.PlayerClass;
import dev.elysium.combat.stats.CombatStats;
import dev.elysium.core.api.CoreAPI;
import dev.elysium.core.event.ElysiumLevelUpEvent;
import dev.elysium.core.util.ColorUtil;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;

public class CombatListener implements Listener {

    private final ElysiumCombat plugin;

    public CombatListener(ElysiumCombat plugin) { this.plugin = plugin; }

    // â”€â”€ Join / Quit â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

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
        Player p = e.getPlayer();
        plugin.getStatsManager().remove(p);
        plugin.getComboManager().cleanup(p.getUniqueId());
        plugin.getCombatTagManager().cleanup(p.getUniqueId());
        plugin.getKillNotifyManager().cleanup(p.getUniqueId());
    }

    // â”€â”€ Damage: Defense + Combo + Hit Particle + Combat Tag â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent e) {
        // Xac dinh attacker la player
        Player attacker = null;
        if (e.getDamager() instanceof Player p) {
            attacker = p;
        } else if (e.getDamager() instanceof Projectile proj
                && proj.getShooter() instanceof Player p) {
            attacker = p;
        }

        // â”€â”€ Defense & Victim Passives â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        if (e.getEntity() instanceof Player victim) {
            CombatStats stats = plugin.getStatsManager().getStats(victim);
            if (stats.getDefense() > 0) {
                e.setDamage(stats.applyDefense(e.getDamage()));
            }

            // Victim Passives
            plugin.getPassiveManager().processVictimPassives(victim, e.getDamage());

            // Combat Tag: victim bi danh â†’ tag ca hai
            if (attacker != null) {
                plugin.getCombatTagManager().tag(attacker, victim);
            }

            // Reset combo cua victim khi bi trung
            plugin.getComboManager().resetCombo(victim.getUniqueId());
        }

        // â”€â”€ Attacker Passives, Combo, Hit Particle, Tag â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        if (attacker != null && e.getEntity() instanceof LivingEntity target) {
            boolean isArrow = e.getDamager() instanceof Projectile;
            
            // Attacker Passives
            double passiveMultiplier = plugin.getPassiveManager().processAttackerPassives(attacker, target, isArrow);
            
            dev.elysium.combat.clazz.LifeClass lifeClass = plugin.getLifeClassManager().getLifeClass(attacker);
            if (lifeClass == dev.elysium.combat.clazz.LifeClass.STRIKER) {
                e.setDamage(e.getDamage() * 1.5);
            } else if (lifeClass == dev.elysium.combat.clazz.LifeClass.FORGER) {
                e.setDamage(e.getDamage() * 0.5);
            }
            if (passiveMultiplier != 1.0) {
                e.setDamage(e.getDamage() * passiveMultiplier);
            }

            // Combo: tinh bonus damage
            double comboMultiplier = plugin.getComboManager().onHit(attacker);
            if (comboMultiplier > 1.0) {
                e.setDamage(e.getDamage() * comboMultiplier);
            }

            // Hit particle theo class
            plugin.getHitParticleManager().spawnHitEffect(attacker, target);

            // Combat tag khi danh mob
            if (!(e.getEntity() instanceof Player)) {
                plugin.getCombatTagManager().tagOne(attacker);
            }
        }
        
        // Spawn Damage Indicator
        boolean isCrit = false;
        if (attacker != null) {
            // Very basic crit check (falling, not sprinting, not in water, etc. or just based on bonus multiplier)
            isCrit = !attacker.isOnGround() && attacker.getFallDistance() > 0 && !e.getDamager().getClass().getSimpleName().contains("Arrow");
        }
        plugin.getDamageIndicatorManager().spawnIndicator(e.getEntity().getLocation(), e.getDamage(), isCrit);
    }

    // â”€â”€ Death: Kill Notify + Streak + Particle â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player victim = e.getEntity();
        plugin.getHitParticleManager().spawnDeathEffect(victim);
        plugin.getCombatTagManager().cleanup(victim.getUniqueId());
        plugin.getComboManager().resetCombo(victim.getUniqueId());

        // Xac dinh killer
        Player killer = victim.getKiller();
        String killerName = killer != null ? killer.getName() : "Moi truong";

        plugin.getKillNotifyManager().onDeath(victim, killerName);

        if (killer != null) {
            plugin.getKillNotifyManager().onKill(killer, victim.getName(), true);
            // Reset combat tag cho killer sau kill
            plugin.getCombatTagManager().untag(killer.getUniqueId(), false);
        }
    }

    // â”€â”€ Chan lenh khi dang combat tag â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @EventHandler(priority = EventPriority.HIGH)
    public void onCommand(PlayerCommandPreprocessEvent e) {
        Player player = e.getPlayer();
        if (!plugin.getCombatTagManager().isTagged(player)) return;
        if (!plugin.getCombatTagManager().isBlockedCommand(e.getMessage())) return;

        e.setCancelled(true);
        player.sendActionBar(ColorUtil.component(
            "&câ” Dang chien dau! Khong the dung lenh nay!"));
    }

    // â”€â”€ Level Up â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @EventHandler
    public void onLevelUp(ElysiumLevelUpEvent e) {
        Player player   = e.getPlayer();
        int    newLevel = e.getNewLevel();
        PlayerClass pc  = plugin.getClassManager().getPlayerClass(player.getUniqueId());

        if (pc != PlayerClass.NONE) {
            plugin.getSkillManager().refreshHotbarSkills(player, pc);
        }

        if (newLevel == 10 || newLevel == 25 || newLevel == 50) {
            player.sendMessage(ColorUtil.color(
                "&c[Combat] &7Dat Level &e" + newLevel +
                "&7! Skill tier moi co the duoc mo khoa trong tuong lai."));
        }
    }
}
