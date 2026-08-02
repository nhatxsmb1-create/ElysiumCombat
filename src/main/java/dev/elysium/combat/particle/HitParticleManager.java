package dev.elysium.combat.particle;

import dev.elysium.combat.ElysiumCombat;
import dev.elysium.combat.clazz.PlayerClass;
import org.bukkit.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

/**
 * Hieu ung particle khi player danh trung target.
 * Moi class co particle rieng bieu trung cho phong cach chien dau.
 */
public class HitParticleManager {

    private final ElysiumCombat plugin;

    public HitParticleManager(ElysiumCombat plugin) {
        this.plugin = plugin;
    }

    /**
     * Spawn particle tai vi tri bi trung dua theo class cua attacker.
     */
    public void spawnHitEffect(Player attacker, LivingEntity target) {
        PlayerClass pc = plugin.getClassManager().getPlayerClass(attacker.getUniqueId());
        Location loc   = target.getLocation().clone().add(0, target.getHeight() / 2.0, 0);
        World world    = target.getWorld();

        switch (pc) {
            case WARRIOR -> {
                // Tia lua do — chiem si chiem dap manh
                world.spawnParticle(Particle.CRIT, loc, 10, 0.3, 0.3, 0.3, 0.2);
                world.spawnParticle(Particle.SWEEP_ATTACK, loc, 2, 0.1, 0.1, 0.1, 0);
                world.playSound(loc, Sound.ENTITY_IRON_GOLEM_HURT, 0.4f, 1.4f);
            }
            case MAGE -> {
                // Phep thuat xanh duong
                world.spawnParticle(Particle.ENCHANTED_HIT, loc, 12, 0.3, 0.3, 0.3, 0.15);
                world.spawnParticle(Particle.WITCH, loc, 5, 0.2, 0.2, 0.2, 0.05);
                world.playSound(loc, Sound.BLOCK_AMETHYST_BLOCK_HIT, 0.5f, 1.6f);
            }
            case ARCHER -> {
                // Xanh la tu nhien
                world.spawnParticle(Particle.HAPPY_VILLAGER, loc, 8, 0.3, 0.3, 0.3, 0);
                world.spawnParticle(Particle.CRIT, loc, 5, 0.2, 0.2, 0.2, 0.1);
                world.playSound(loc, Sound.ENTITY_ARROW_HIT_PLAYER, 0.5f, 1.3f);
            }
            case ROGUE -> {
                // Tim - bong toi, sat thuong nhanh
                world.spawnParticle(Particle.PORTAL, loc, 12, 0.2, 0.3, 0.2, 0.3);
                world.spawnParticle(Particle.SMOKE, loc, 4, 0.1, 0.2, 0.1, 0.05);
                world.playSound(loc, Sound.ENTITY_ENDERMAN_TELEPORT, 0.3f, 2.0f);
            }
            default -> {
                // Khong co class: hit binh thuong
                world.spawnParticle(Particle.CRIT, loc, 5, 0.2, 0.2, 0.2, 0.1);
            }
        }
    }

    /**
     * Hieu ung khi player bi killed.
     */
    public void spawnDeathEffect(Player victim) {
        Location loc  = victim.getLocation().clone().add(0, 1, 0);
        World world   = victim.getWorld();
        world.spawnParticle(Particle.EXPLOSION, loc, 3, 0.5, 0.5, 0.5, 0.1);
        world.spawnParticle(Particle.LARGE_SMOKE, loc, 15, 0.5, 0.5, 0.5, 0.05);
        world.playSound(loc, Sound.ENTITY_WITHER_DEATH, 0.5f, 1.8f);
    }
}
