package dev.elysium.combat.combo;

import dev.elysium.combat.ElysiumCombat;
import dev.elysium.combat.clazz.PlayerClass;
import dev.elysium.core.util.ColorUtil;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

/**
 * Theo doi chuoi don danh lien tiep cua player.
 * Moi don danh vao mob/player trong 2.5s tiep theo tinh la combo.
 * Combo cang cao → bonus damage cang lon (cap 10 don).
 */
public class ComboManager {

    private final ElysiumCombat plugin;

    // UUID → so don combo hien tai
    private final Map<UUID, Integer>    comboCount   = new HashMap<>();
    // UUID → task reset combo neu qua 2.5s khong danh
    private final Map<UUID, BukkitTask> resetTasks   = new HashMap<>();

    private static final int    COMBO_WINDOW_TICKS = 50; // 2.5s
    private static final int    MAX_COMBO          = 10;
    // Bonus damage moi don combo (% tich luy, cap o MAX_COMBO)
    // vi du: combo 3 → +9% damage, combo 10 → +30%
    private static final double BONUS_PER_COMBO    = 0.03; // 3% moi combo

    public ComboManager(ElysiumCombat plugin) {
        this.plugin = plugin;
    }

    /**
     * Goi moi khi player danh trung entity.
     * Tra ve he so nhan damage (1.0 = binh thuong, 1.09 = +9%, ...).
     */
    public double onHit(Player player) {
        UUID uuid = player.getUniqueId();

        int current = comboCount.getOrDefault(uuid, 0) + 1;
        current = Math.min(current, MAX_COMBO);
        comboCount.put(uuid, current);

        // Huy reset cu, dat reset moi
        cancelReset(uuid);
        BukkitTask task = plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            resetCombo(uuid);
        }, COMBO_WINDOW_TICKS);
        resetTasks.put(uuid, task);

        // Hien thi action bar neu combo >= 2
        if (current >= 2) {
            String bar = buildComboBar(player, current);
            player.sendActionBar(ColorUtil.component(bar));
        }

        return 1.0 + (current - 1) * BONUS_PER_COMBO;
    }

    /**
     * Reset combo khi player bi thuong, chet, hoac het thoi gian.
     */
    public void resetCombo(UUID uuid) {
        int old = comboCount.getOrDefault(uuid, 0);
        comboCount.remove(uuid);
        cancelReset(uuid);

        if (old >= 3) {
            Player p = plugin.getServer().getPlayer(uuid);
            if (p != null && p.isOnline()) {
                p.sendActionBar(ColorUtil.component("&7Combo &f" + old + "x &7ket thuc."));
            }
        }
    }

    public int getCombo(UUID uuid) {
        return comboCount.getOrDefault(uuid, 0);
    }

    public void cleanup(UUID uuid) {
        comboCount.remove(uuid);
        cancelReset(uuid);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void cancelReset(UUID uuid) {
        BukkitTask old = resetTasks.remove(uuid);
        if (old != null) old.cancel();
    }

    private String buildComboBar(Player player, int combo) {
        PlayerClass pc = plugin.getClassManager().getPlayerClass(player.getUniqueId());
        String color = switch (pc) {
            case WARRIOR -> "&c";
            case MAGE    -> "&9";
            case ARCHER  -> "&a";
            case ROGUE   -> "&5";
            default      -> "&f";
        };

        // Tao thanh combo: ▮▮▮▯▯▯▯▯▯▯
        StringBuilder bar = new StringBuilder();
        for (int i = 1; i <= MAX_COMBO; i++) {
            bar.append(i <= combo ? color + "▮" : "&8▯");
        }

        int bonusPct = (int) Math.round((combo - 1) * BONUS_PER_COMBO * 100);
        return color + "&l" + combo + "x COMBO &r" + bar + " &7(+" + bonusPct + "% dmg)";
    }
}
