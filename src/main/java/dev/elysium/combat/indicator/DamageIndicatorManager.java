package dev.elysium.combat.indicator;

import dev.elysium.combat.ElysiumCombat;
import dev.elysium.core.util.ColorUtil;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.scheduler.BukkitRunnable;

import java.text.DecimalFormat;
import java.util.Random;

public class DamageIndicatorManager {

    private final ElysiumCombat plugin;
    private final Random random = new Random();
    private final DecimalFormat format = new DecimalFormat("#.##");

    public DamageIndicatorManager(ElysiumCombat plugin) {
        this.plugin = plugin;
    }

    public void spawnIndicator(Location loc, double damage, boolean isCrit) {
        if (damage <= 0) return;

        double offsetX = (random.nextDouble() - 0.5) * 1.5;
        double offsetY = random.nextDouble() * 1.0 + 0.5;
        double offsetZ = (random.nextDouble() - 0.5) * 1.5;

        Location spawnLoc = loc.clone().add(offsetX, offsetY, offsetZ);

        ArmorStand as = (ArmorStand) spawnLoc.getWorld().spawnEntity(spawnLoc, EntityType.ARMOR_STAND);
        as.setVisible(false);
        as.setMarker(true);
        as.setSmall(true);
        as.setGravity(false);
        as.setBasePlate(false);
        as.setCustomNameVisible(true);

        String dmgStr = format.format(damage);
        String name = isCrit 
            ? ColorUtil.color("&e&l⚡ " + dmgStr + "!") 
            : ColorUtil.color("&c-" + dmgStr + " ❤");
        
        as.setCustomName(name);

        // Hieu ung bay len nhe nhang trong 1 giay (20 tick)
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks >= 20 || !as.isValid()) {
                    as.remove();
                    this.cancel();
                    return;
                }
                Location current = as.getLocation();
                current.add(0, 0.05, 0);
                as.teleport(current);
                ticks++;
            }
        }.runTaskTimer(plugin, 1L, 1L);
    }
}
