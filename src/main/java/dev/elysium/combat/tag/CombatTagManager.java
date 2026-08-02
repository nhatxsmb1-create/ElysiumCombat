package dev.elysium.combat.tag;

import dev.elysium.combat.ElysiumCombat;
import dev.elysium.core.util.ColorUtil;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

/**
 * Combat Tag: danh dau player dang trong trang thai chien dau.
 * Chặn cac lenh thoat an toan (/is home, /spawn, /tpa...) trong thoi gian tag.
 */
public class CombatTagManager {

    private final ElysiumCombat plugin;

    private final Map<UUID, BukkitTask> tagTasks   = new HashMap<>();
    private final Set<UUID>             tagged      = new HashSet<>();

    private static final int TAG_DURATION_TICKS = 100; // 5 giay
    // Lenh bi chan khi dang combat tag
    private static final List<String> BLOCKED_COMMANDS = List.of(
        "/is", "/island", "/spawn", "/hub", "/tpa", "/tpaccept", "/home", "/warp"
    );

    public CombatTagManager(ElysiumCombat plugin) {
        this.plugin = plugin;
    }

    /**
     * Tag ca 2 player khi danh nhau.
     */
    public void tag(Player attacker, Player victim) {
        tagOne(attacker);
        tagOne(victim);
    }

    /**
     * Tag player khi danh mob (chi tag attacker).
     */
    public void tagOne(Player player) {
        UUID uuid = player.getUniqueId();
        boolean wasTagged = tagged.contains(uuid);

        tagged.add(uuid);

        // Huy reset cu, dat reset moi
        BukkitTask old = tagTasks.remove(uuid);
        if (old != null) old.cancel();

        BukkitTask task = plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            untag(uuid, true);
        }, TAG_DURATION_TICKS);
        tagTasks.put(uuid, task);

        if (!wasTagged) {
            player.sendActionBar(ColorUtil.component("&c⚔ Dang chien dau! Khong the thoat trong &e5s"));
        }
    }

    public void untag(UUID uuid, boolean notify) {
        tagged.remove(uuid);
        BukkitTask t = tagTasks.remove(uuid);
        if (t != null) t.cancel();

        if (notify) {
            Player p = plugin.getServer().getPlayer(uuid);
            if (p != null && p.isOnline()) {
                p.sendActionBar(ColorUtil.component("&a✔ Thoat khoi chien dau."));
            }
        }
    }

    public boolean isTagged(Player player) {
        return tagged.contains(player.getUniqueId());
    }

    public boolean isBlockedCommand(String command) {
        String lower = command.toLowerCase();
        return BLOCKED_COMMANDS.stream().anyMatch(lower::startsWith);
    }

    public void cleanup(UUID uuid) {
        untag(uuid, false);
    }
}
