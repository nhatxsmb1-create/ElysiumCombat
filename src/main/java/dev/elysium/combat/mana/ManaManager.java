package dev.elysium.combat.mana;

import dev.elysium.combat.ElysiumCombat;
import dev.elysium.combat.stats.CombatStats;
import dev.elysium.core.api.CoreAPI;
import dev.elysium.core.player.ElysiumPlayer;
import dev.elysium.core.util.ColorUtil;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public class ManaManager {

    private final ElysiumCombat plugin;
    private BukkitTask regenTask, displayTask;

    public ManaManager(ElysiumCombat plugin) {
        this.plugin = plugin;
        start();
    }

    private void start() {
        // Regen mana moi 2 giay — dung CoreAPI de fire ElysiumManaChangeEvent
        regenTask = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            for (Player p : plugin.getServer().getOnlinePlayers()) {
                ElysiumPlayer ep = CoreAPI.getPlayer(p);
                if (ep == null) continue;
                CombatStats st = plugin.getStatsManager().getStats(p);
                if (st.getManaRegen() > 0) {
                    CoreAPI.addMana(p, st.getManaRegen()); // fires ElysiumManaChangeEvent
                }
            }
        }, 40L, 40L);

        // Hien thi mana bar tren action bar
        displayTask = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            for (Player p : plugin.getServer().getOnlinePlayers()) {
                ElysiumPlayer ep = CoreAPI.getPlayer(p);
                if (ep == null) continue;
                String bar = ColorUtil.progressBar(
                    ep.getMana(), ep.getMaxMana(), 8, '■', '□', "&b", "&8");
                p.sendActionBar(ColorUtil.component(
                    bar + " &b" + ep.getMana() + "/" + ep.getMaxMana() + " Mana"));
            }
        }, 20L, 20L);
    }

    public void stop() {
        if (regenTask   != null) regenTask.cancel();
        if (displayTask != null) displayTask.cancel();
    }
}
