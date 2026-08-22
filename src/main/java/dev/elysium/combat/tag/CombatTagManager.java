package dev.elysium.combat.tag;

import dev.elysium.combat.ElysiumCombat;
import dev.elysium.core.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

/**
 * Combat Tag: danh dau player dang trong trang thai chien dau.
 * Chặn cac lenh thoat an toan (/is home, /spawn, /tpa...) trong thoi gian tag.
 * Su dung BossBar de dem nguoc VIP PRO.
 */
public class CombatTagManager {

    private final ElysiumCombat plugin;

    private final Map<UUID, Long>    tagEndTimes = new HashMap<>();
    private final Map<UUID, BossBar> bossBars    = new HashMap<>();
    private BukkitTask               updateTask;

    private static final int TAG_DURATION_MILLIS = 5000; // 5 giay
    // Lenh bi chan khi dang combat tag
    private static final List<String> BLOCKED_COMMANDS = List.of(
        "/is", "/island", "/spawn", "/hub", "/tpa", "/tpaccept", "/home", "/warp"
    );

    public CombatTagManager(ElysiumCombat plugin) {
        this.plugin = plugin;
        startUpdateTask();
    }

    private void startUpdateTask() {
        updateTask = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            long now = System.currentTimeMillis();
            Iterator<Map.Entry<UUID, Long>> it = tagEndTimes.entrySet().iterator();

            while (it.hasNext()) {
                Map.Entry<UUID, Long> entry = it.next();
                UUID uuid = entry.getKey();
                long endTime = entry.getValue();
                
                Player p = Bukkit.getPlayer(uuid);
                
                if (now >= endTime) {
                    it.remove();
                    removeBossBar(uuid, p);
                    if (p != null && p.isOnline()) {
                        p.sendMessage(ColorUtil.color("&a✔ Bạn đã an toàn! Có thể sử dụng lệnh dịch chuyển."));
                    }
                } else {
                    if (p != null && p.isOnline()) {
                        updateBossBar(uuid, p, endTime, now);
                    }
                }
            }
        }, 2L, 2L); // Chạy mỗi 2 tick (0.1s) để BossBar mượt
    }

    /**
     * Tag ca 2 player khi danh nhau.
     */
    public void tag(Player attacker, Player victim) {
        tagOne(attacker);
        tagOne(victim);
    }

    /**
     * Tag player khi danh mob hoac bi danh.
     */
    public void tagOne(Player player) {
        UUID uuid = player.getUniqueId();
        boolean wasTagged = tagEndTimes.containsKey(uuid);

        tagEndTimes.put(uuid, System.currentTimeMillis() + TAG_DURATION_MILLIS);

        if (!wasTagged) {
            BossBar bar = Bukkit.createBossBar(ColorUtil.color("&c⚔ TRẠNG THÁI CHIẾN ĐẤU ⚔"), BarColor.RED, BarStyle.SOLID);
            bar.addPlayer(player);
            bossBars.put(uuid, bar);
            player.sendMessage(ColorUtil.color("&c⚔ Bạn đang trong trạng thái chiến đấu! Không thể thoát game hoặc dịch chuyển."));
        }
    }

    private void updateBossBar(UUID uuid, Player p, long endTime, long now) {
        BossBar bar = bossBars.get(uuid);
        if (bar != null) {
            long remaining = endTime - now;
            double progress = (double) remaining / TAG_DURATION_MILLIS;
            if (progress < 0) progress = 0;
            if (progress > 1) progress = 1;
            bar.setProgress(progress);
            
            double seconds = remaining / 1000.0;
            bar.setTitle(ColorUtil.color("&c⚔ CHIẾN ĐẤU &8(&e" + String.format("%.1f", seconds) + "s&8) ⚔"));
        }
    }

    private void removeBossBar(UUID uuid, Player p) {
        BossBar bar = bossBars.remove(uuid);
        if (bar != null) {
            bar.removeAll();
        }
    }

    public void untag(UUID uuid, boolean notify) {
        tagEndTimes.remove(uuid);
        Player p = Bukkit.getPlayer(uuid);
        removeBossBar(uuid, p);

        if (notify && p != null && p.isOnline()) {
            p.sendMessage(ColorUtil.color("&a✔ Thoát khỏi trạng thái chiến đấu."));
        }
    }

    public boolean isTagged(Player player) {
        return tagEndTimes.containsKey(player.getUniqueId());
    }

    public boolean isBlockedCommand(String command) {
        String lower = command.toLowerCase();
        return BLOCKED_COMMANDS.stream().anyMatch(lower::startsWith);
    }

    public void cleanup(UUID uuid) {
        untag(uuid, false);
    }
}
