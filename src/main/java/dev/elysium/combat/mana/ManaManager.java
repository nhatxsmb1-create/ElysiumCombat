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
    private BukkitTask regenTask;
    private BukkitTask displayTask;

    public ManaManager(ElysiumCombat plugin) {
        this.plugin = plugin;
        startTasks();
    }

    private void startTasks() {
        int regenInterval    = plugin.getCombatConfig().getManaRegenInterval();
        int displayInterval  = plugin.getCombatConfig().getManaDisplayInterval();

        // Regen task
        regenTask = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            for (Player p : plugin.getServer().getOnlinePlayers()) {
                ElysiumPlayer ep = CoreAPI.getPlayer(p);
                if (ep == null) continue;
                CombatStats stats = plugin.getStatsManager().getStats(p);
                int regen = stats.getManaRegen();
                if (regen > 0) ep.addMana(regen);
            }
        }, regenInterval, regenInterval);

        // Display task (action bar)
        displayTask = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            for (Player p : plugin.getServer().getOnlinePlayers()) {
                ElysiumPlayer ep = CoreAPI.getPlayer(p);
                if (ep == null) continue;
                String bar = ColorUtil.progressBar(ep.getMana(), ep.getMaxMana(), 8, '■', '□', "&b", "&8");
                String text = bar + " &b" + ep.getMana() + "/" + ep.getMaxMana() + " Mana";
                p.sendActionBar(ColorUtil.component(text));
            }
        }, displayInterval, displayInterval);
    }

    public void stop() {
        if (regenTask   != null) regenTask.cancel();
        if (displayTask != null) displayTask.cancel();
    }

    public int getMana(Player player) {
        ElysiumPlayer ep = CoreAPI.getPlayer(player);
        return ep != null ? ep.getMana() : 0;
    }

    public boolean useMana(Player player, int amount) {
        ElysiumPlayer ep = CoreAPI.getPlayer(player);
        return ep != null && ep.useMana(amount);
    }
    }
