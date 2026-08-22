package dev.elysium.combat.skill;

import dev.elysium.combat.ElysiumCombat;
import dev.elysium.combat.clazz.PlayerClass;
import dev.elysium.core.api.CoreAPI;
import dev.elysium.core.player.ElysiumPlayer;
import dev.elysium.core.util.ColorUtil;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Random;

public class PassiveManager {

    private final ElysiumCombat plugin;
    private final Random random = new Random();

    public PassiveManager(ElysiumCombat plugin) {
        this.plugin = plugin;
        startSupportAuraTask();
    }

    /**
     * SUPPORT: Hào Quang Sinh Mệnh
     * Chạy mỗi 3 giây, buff regen nhẹ cho đồng minh xung quanh.
     */
    private void startSupportAuraTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player p : plugin.getServer().getOnlinePlayers()) {
                    if (plugin.getClassManager().getPlayerClass(p.getUniqueId()) == PlayerClass.SUPPORT) {
                        boolean healedAnyone = false;
                        for (org.bukkit.entity.Entity e : p.getNearbyEntities(8, 5, 8)) {
                            if (e instanceof Player ally && !ally.isDead() && ally.getHealth() < ally.getMaxHealth()) {
                                ally.setHealth(Math.min(ally.getMaxHealth(), ally.getHealth() + 1.0));
                                ally.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, ally.getLocation().add(0, 1, 0), 3, 0.3, 0.3, 0.3, 0);
                                healedAnyone = true;
                            }
                        }
                        if (healedAnyone) {
                            p.getWorld().spawnParticle(Particle.END_ROD, p.getLocation().add(0, 1, 0), 10, 0.5, 0.5, 0.5, 0.05);
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 60L, 60L);
    }

    /**
     * Kích hoạt nội tại khi gây sát thương.
     * Trả về hệ số sát thương bổ sung (vd 1.5x cho Backstab)
     */
    public double processAttackerPassives(Player attacker, LivingEntity victim, boolean isArrow) {
        PlayerClass pc = plugin.getClassManager().getPlayerClass(attacker.getUniqueId());
        double multiplier = 1.0;

        switch (pc) {
            case RANGER:
                if (isArrow && random.nextDouble() < 0.20) {
                    // 20% gây độc
                    victim.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 100, 1));
                    attacker.playSound(attacker.getLocation(), Sound.ENTITY_SPIDER_AMBIENT, 0.5f, 1.5f);
                    if (plugin.getActionBarManager() != null) {
                        plugin.getActionBarManager().sendTemporaryMessage(attacker, "&a&l[Nội Tại] &fĐộc Tố Kích Hoạt!", 40);
                    }
                }
                break;
            case ASSASSIN:
                // Backstab logic
                Vector attackerDir = attacker.getLocation().getDirection().setY(0).normalize();
                Vector victimDir = victim.getLocation().getDirection().setY(0).normalize();
                
                // Nếu góc giữa hướng nhìn của cả 2 < 45 độ (tức là đứng sau lưng và nhìn cùng hướng)
                if (attackerDir.dot(victimDir) > 0.7) {
                    multiplier = 1.5;
                    attacker.playSound(attacker.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.0f, 1.2f);
                    victim.getWorld().spawnParticle(Particle.ENCHANTED_HIT, victim.getLocation().add(0, 1, 0), 15, 0.5, 0.5, 0.5, 0.1);
                    if (plugin.getActionBarManager() != null) {
                        plugin.getActionBarManager().sendTemporaryMessage(attacker, "&5&l[Nội Tại] &fĐâm Lén Chí Mạng (x1.5)!", 40);
                    }
                }
                break;
            default:
                break;
        }

        return multiplier;
    }

    /**
     * Kích hoạt nội tại khi nhận sát thương.
     */
    public void processVictimPassives(Player victim, double damage) {
        PlayerClass pc = plugin.getClassManager().getPlayerClass(victim.getUniqueId());

        switch (pc) {
            case WARRIOR:
                if (victim.getHealth() - damage <= victim.getMaxHealth() * 0.3) {
                    if (!victim.hasPotionEffect(PotionEffectType.RESISTANCE)) {
                        victim.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 100, 1)); // 5s Res II
                        victim.playSound(victim.getLocation(), Sound.ITEM_SHIELD_BLOCK, 1.0f, 0.8f);
                        if (plugin.getActionBarManager() != null) {
                            plugin.getActionBarManager().sendTemporaryMessage(victim, "&c&l[Nội Tại] &fHuyết Loạn Kích Hoạt!", 40);
                        }
                    }
                }
                break;
            case MAGE:
                if (random.nextDouble() < 0.15) { // 15% hồi Mana
                    ElysiumPlayer ep = CoreAPI.getPlayer(victim);
                    if (ep != null) {
                        ep.addMana(10);
                        victim.playSound(victim.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.5f, 2.0f);
                        victim.getWorld().spawnParticle(Particle.ENCHANT, victim.getLocation().add(0, 1, 0), 10, 0.5, 0.5, 0.5, 0.5);
                    }
                }
                break;
            default:
                break;
        }
    }
}
