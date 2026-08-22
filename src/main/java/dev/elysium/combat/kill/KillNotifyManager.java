package dev.elysium.combat.kill;

import dev.elysium.combat.ElysiumCombat;
import dev.elysium.combat.clazz.ClassData;
import dev.elysium.combat.clazz.PlayerClass;
import dev.elysium.core.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Thong bao kill va theo doi killstreak.
 * Killstreak reset khi player chet.
 */
public class KillNotifyManager {

    private final ElysiumCombat plugin;

    // UUID → killstreak hien tai
    private final Map<UUID, Integer> killstreaks = new HashMap<>();

    // Moc killstreak can thong bao toan server
    private static final int[] BROADCAST_MILESTONES = {3, 5, 8, 10, 15, 20};

    // Tin nhan ung voi moc killstreak
    private static final Map<Integer, String> MILESTONE_MSG = Map.of(
        3,  "&6Triple Kill!",
        5,  "&6&lPenta Kill!!",
        8,  "&c&lRAMPAGE!!!",
        10, "&4&l✦ UNSTOPPABLE ✦",
        15, "&4&l⚡ GODLIKE ⚡",
        20, "&4&l☠ LEGENDARY ☠"
    );

    public KillNotifyManager(ElysiumCombat plugin) {
        this.plugin = plugin;
    }

    /**
     * Goi khi player giet duoc enemy (player hoac mob).
     * killedName = ten hien thi cua nan nhan.
     */
    public void onKill(Player killer, String killedName, boolean isPlayerKill) {
        UUID uuid = killer.getUniqueId();
        int streak = killstreaks.getOrDefault(uuid, 0) + 1;
        killstreaks.put(uuid, streak);

        PlayerClass pc = plugin.getClassManager().getPlayerClass(uuid);
        ClassData cd   = plugin.getClassManager().getClassData(pc);
        String className = cd != null ? cd.getDisplayName() : "&7???";

        // ── Thong bao ca server (chi khi kill player) ─────────────────────
        if (isPlayerKill) {
            String killMsg = ColorUtil.color(
                "&7[&cKill&7] " + className + " &f" + killer.getName() +
                " &7ha guc &c" + killedName +
                (streak >= 2 ? " &8(&e" + streak + "x Killstreak&8)" : "")
            );
            Bukkit.broadcastMessage(killMsg);

            // Sound toan server khi co player bi kill
            Bukkit.getOnlinePlayers().forEach(p ->
                p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.6f, 1.2f));

            // Thong bao killstreak milestone
            for (int milestone : BROADCAST_MILESTONES) {
                if (streak == milestone) {
                    String milestoneColor = streak >= 10 ? "&4" : "&6";
                    String msMsg = MILESTONE_MSG.getOrDefault(milestone,
                        milestoneColor + "&l" + milestone + " KILLSTREAK!");
                    Bukkit.broadcastMessage(ColorUtil.color(
                        "&8[&6Killstreak&8] " + msMsg +
                        " &8— &f" + killer.getName()
                    ));
                    Bukkit.getOnlinePlayers().forEach(p ->
                        p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.7f, 1.0f));
                    break;
                }
            }

            // Hieu ung VIP
            if (streak >= 15) {
                // Set danh khong sat thuong
                killer.getWorld().strikeLightningEffect(killer.getLocation());
                Bukkit.getOnlinePlayers().forEach(p ->
                    p.playSound(p.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 0.8f));
            } else if (streak >= 8) {
                // Phao hoa
                org.bukkit.entity.Firework fw = killer.getWorld().spawn(killer.getLocation(), org.bukkit.entity.Firework.class);
                org.bukkit.inventory.meta.FireworkMeta fwm = fw.getFireworkMeta();
                fwm.addEffect(org.bukkit.FireworkEffect.builder().withColor(org.bukkit.Color.RED).withFade(org.bukkit.Color.ORANGE).with(org.bukkit.FireworkEffect.Type.BALL_LARGE).trail(true).flicker(true).build());
                fwm.setPower(1);
                fw.setFireworkMeta(fwm);
            }
        }

        // ── Thong bao rieng cho killer ─────────────────────────────────────
        if (plugin.getActionBarManager() != null) {
            plugin.getActionBarManager().sendTemporaryMessage(killer, 
                "&a+1 Kill &8| &eStreak: &f" + streak +
                (streak >= 3 ? " &6(" + getStreakTitle(streak) + ")" : ""), 
                60);
        }
    }

    /**
     * Goi khi player chet — reset killstreak, thong bao streak bi pha.
     */
    public void onDeath(Player victim, String killerName) {
        UUID uuid = victim.getUniqueId();
        int streak = killstreaks.getOrDefault(uuid, 0);

        if (streak >= 3) {
            Bukkit.broadcastMessage(ColorUtil.color(
                "&8[&cStreak Ended&8] &f" + killerName +
                " &7pha vo chuoi &e" + streak + "x &7cua &f" + victim.getName()
            ));
        }

        killstreaks.remove(uuid);
    }

    public int getKillstreak(UUID uuid) {
        return killstreaks.getOrDefault(uuid, 0);
    }

    public void cleanup(UUID uuid) {
        killstreaks.remove(uuid);
    }

    private String getStreakTitle(int streak) {
        if (streak >= 20) return "LEGENDARY";
        if (streak >= 15) return "GODLIKE";
        if (streak >= 10) return "UNSTOPPABLE";
        if (streak >= 8)  return "RAMPAGE";
        if (streak >= 5)  return "PENTA";
        if (streak >= 3)  return "TRIPLE";
        return streak + "x";
    }
}
