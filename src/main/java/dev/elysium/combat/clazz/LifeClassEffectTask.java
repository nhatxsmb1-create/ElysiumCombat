package dev.elysium.combat.clazz;

import dev.elysium.combat.ElysiumCombat;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class LifeClassEffectTask extends BukkitRunnable {
    private final ElysiumCombat plugin;

    public LifeClassEffectTask(ElysiumCombat plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            LifeClass lifeClass = plugin.getLifeClassManager().getLifeClass(player);
            
            if (lifeClass == LifeClass.FORGER) {
                // Haste 3 (Amplifier 2) for 60 ticks (3 seconds)
                player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 100, 2, true, false, false));
            } else if (lifeClass == LifeClass.STRIKER) {
                // Mining Fatigue 2 (Amplifier 1) for 60 ticks
                player.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 100, 1, true, false, false));
            }
        }
    }
}