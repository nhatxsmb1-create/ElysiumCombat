package dev.elysium.combat.mana;

import dev.elysium.combat.ElysiumCombat;
import dev.elysium.combat.stats.CombatStats;
import dev.elysium.core.api.CoreAPI;
import dev.elysium.core.player.ElysiumPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public class ManaManager {

    private final ElysiumCombat plugin;
    private BukkitTask regenTask;

    public ManaManager(ElysiumCombat plugin) {
        this.plugin = plugin;
        start();
    }

    private void start() {
        // Regen mana moi 2 giay
        regenTask = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            for (Player p : plugin.getServer().getOnlinePlayers()) {
                ElysiumPlayer ep = CoreAPI.getPlayer(p);
                if (ep == null) continue;
                CombatStats st = plugin.getStatsManager().getStats(p);
                if (st.getManaRegen() > 0) {
                    CoreAPI.addMana(p, st.getManaRegen());
                }
            }
        }, 40L, 40L);
        // Display task da bi xoa — mana chi hien thi trong Profile GUI va Placeholder
    }

    public void stop() {
        if (regenTask != null) regenTask.cancel();
    }
}
