package dev.elysium.combat.listener;

import dev.elysium.combat.ElysiumCombat;
import dev.elysium.combat.clazz.PlayerClass;
import dev.elysium.combat.gui.SkillMenuGui;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;

public class SkillListener implements Listener {

    private final ElysiumCombat plugin;
    private BukkitTask cooldownTask;

    public SkillListener(ElysiumCombat plugin) {
        this.plugin = plugin;
        startCooldownTask();
    }

    // ── Kich hoat skill bang chuot phai ──────────────────────────────────────

    @EventHandler
    public void onRightClick(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_AIR
         && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Player player = e.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (!isSkillItem(item)) return;
        e.setCancelled(true);
        int slot = item.getItemMeta().getCustomModelData() - 2000;
        if (slot >= 1 && slot <= 3) plugin.getSkillManager().activate(player, slot);
    }

    // ── Bao ve: khong cho drop skill item ────────────────────────────────────

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        if (isSkillItem(e.getItemDrop().getItemStack())) {
            e.setCancelled(true);
        }
    }

    // ── Bao ve: khong cho di chuyen skill item ────────────────────────────────

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;

        ItemStack current = e.getCurrentItem();
        ItemStack cursor  = e.getCursor();

        // Khong cho tuong tac voi skill item
        if (current != null && isSkillItem(current)) {
            e.setCancelled(true); return;
        }
        // Khong cho dat skill item vao o khac
        if (cursor != null && !cursor.getType().isAir() && isSkillItem(cursor)) {
            e.setCancelled(true); return;
        }
        // Khong cho dat item khac vao slot skill
        if (e.getClickedInventory() instanceof PlayerInventory) {
            for (int ss : getSkillSlots()) {
                if (e.getSlot() == ss) { e.setCancelled(true); return; }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDrag(InventoryDragEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        if (isSkillItem(e.getOldCursor())) { e.setCancelled(true); return; }
        // Khong cho keo vao slot skill
        for (int ss : getSkillSlots()) {
            if (e.getRawSlots().contains(ss)) { e.setCancelled(true); return; }
        }
    }

    // ── Bao ve: khong mat khi chet ────────────────────────────────────────────

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        // Xoa skill item khoi drop list
        e.getDrops().removeIf(item -> isSkillItem(item));
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        // Tra lai skill item sau 5 tick
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            plugin.getClassManager().giveSkillItems(e.getPlayer());
        }, 5L);
    }

    // ── Cap nhat cooldown lore moi giay ───────────────────────────────────────

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

    // ── Helpers ───────────────────────────────────────────────────────────────

    private boolean isSkillItem(ItemStack item) {
        return plugin.getSkillManager().isSkillItem(item);
    }

    private int[] getSkillSlots() {
        return new int[]{
            plugin.getCombatConfig().getSkillSlot(1),
            plugin.getCombatConfig().getSkillSlot(2),
            plugin.getCombatConfig().getSkillSlot(3)
        };
    }
}
