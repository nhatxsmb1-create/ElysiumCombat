package dev.elysium.combat.listener;

import dev.elysium.combat.ElysiumCombat;
import dev.elysium.combat.clazz.PlayerClass;
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

    // ── Chan drop (phim Q) ────────────────────────────────────────────────────

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        if (isSkillItem(e.getItemDrop().getItemStack())) {
            e.setCancelled(true);
        }
    }

    // ── Chan di chuyen ra NGOAI inventory (rương, lò, etc.) ─────────────────
    // Cho phep di chuyen TU DO ben trong inventory cua player

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;

        ItemStack current = e.getCurrentItem();
        ItemStack cursor  = e.getCursor();

        // Skill item dang o trong o duoc click
        if (current != null && isSkillItem(current)) {
            // Neu click trong PlayerInventory -> cho phep (di chuyen tu do)
            if (e.getClickedInventory() instanceof PlayerInventory) {
                // Chan shift+click khi co inventory ngoai mo (se gui ra ngoai)
                if (e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                    e.setCancelled(true);
                }
                // Cac action khac trong player inventory -> cho phep
            } else {
                // Dang click vao inventory ngoai (ruong, etc.) -> chan
                e.setCancelled(true);
            }
            return;
        }

        // Skill item dang tren cursor -> khong cho dat vao inventory ngoai
        if (cursor != null && !cursor.getType().isAir() && isSkillItem(cursor)) {
            if (!(e.getClickedInventory() instanceof PlayerInventory)) {
                e.setCancelled(true);
            }
        }
    }

    // Chan keo (drag) skill item ra ngoai
    @EventHandler(priority = EventPriority.HIGH)
    public void onDrag(InventoryDragEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        if (!isSkillItem(e.getOldCursor())) return;
        // Chan neu drag vao inventory ngoai
        boolean hasExternalSlot = e.getRawSlots().stream()
            .anyMatch(slot -> slot < e.getView().getTopInventory().getSize());
        if (hasExternalSlot) e.setCancelled(true);
    }

    // ── Khong mat khi chet ────────────────────────────────────────────────────

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        e.getDrops().removeIf(item -> isSkillItem(item));
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        plugin.getServer().getScheduler().runTaskLater(plugin, () ->
            plugin.getClassManager().giveSkillItems(e.getPlayer()), 5L);
    }

    // ── Cap nhat cooldown lore moi giay ───────────────────────────────────────

    private void startCooldownTask() {
        int interval = plugin.getCombatConfig().getCooldownUpdateInterval();
        cooldownTask = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            for (Player p : plugin.getServer().getOnlinePlayers()) {
                PlayerClass pc = plugin.getClassManager().getPlayerClass(p.getUniqueId());
                if (pc == PlayerClass.NONE) continue;
                plugin.getSkillManager().refreshHotbarSkills(p, pc);
            }
        }, interval, interval);
    }

    public void stop() { if (cooldownTask != null) cooldownTask.cancel(); }

    private boolean isSkillItem(ItemStack item) {
        return plugin.getSkillManager().isSkillItem(item);
    }
}