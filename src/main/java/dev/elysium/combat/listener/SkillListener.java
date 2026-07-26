package dev.elysium.combat.listener;

import dev.elysium.combat.ElysiumCombat;
import dev.elysium.combat.clazz.PlayerClass;
import dev.elysium.combat.skill.SkillManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;

public class SkillListener implements Listener {

    private final ElysiumCombat plugin;
    private BukkitTask cooldownTask;

    public SkillListener(ElysiumCombat plugin) {
        this.plugin = plugin;
        startCooldownTask();
    }

    /**
     * Nhan chuot phai vao skill item o hotbar -> kich hoat skill.
     * Skill item duoc nhan dien bang CustomModelData: 2001, 2002, 2003.
     */
    @EventHandler
    public void onRightClick(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_AIR
         && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = e.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (!isSkillItem(item)) return;

        e.setCancelled(true);

        int cmd  = item.getItemMeta().getCustomModelData();
        int slot = cmd - 2000; // 2001->1, 2002->2, 2003->3
        if (slot < 1 || slot > 3) return;

        plugin.getSkillManager().activate(player, slot);
    }

    private boolean isSkillItem(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;
        if (!item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (!meta.hasCustomModelData()) return false;
        int cmd = meta.getCustomModelData();
        return cmd >= SkillManager.CMD_SKILL_1 && cmd <= SkillManager.CMD_SKILL_3;
    }

    /** Cap nhat lore cooldown dem nguoc moi giay */
    private void startCooldownTask() {
        int interval = plugin.getCombatConfig().getCooldownUpdateInterval();
        cooldownTask = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                PlayerClass pc = plugin.getClassManager().getPlayerClass(player.getUniqueId());
                if (pc == PlayerClass.NONE) continue;
                plugin.getSkillManager().refreshHotbarSkills(player, pc);
            }
        }, interval, interval);
    }

    public void stop() { if (cooldownTask != null) cooldownTask.cancel(); }
}
