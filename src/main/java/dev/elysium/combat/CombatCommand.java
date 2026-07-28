package dev.elysium.combat;

import dev.elysium.combat.clazz.ClassData;
import dev.elysium.combat.clazz.PlayerClass;
import dev.elysium.core.achievement.AchievementType;
import dev.elysium.core.api.CoreAPI;
import dev.elysium.core.event.ElysiumClassChangeEvent;
import dev.elysium.core.player.ElysiumPlayer;
import dev.elysium.core.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class CombatCommand implements CommandExecutor {

    private final ElysiumCombat plugin;
    public CombatCommand(ElysiumCombat plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) { sender.sendMessage("Chi player dung duoc!"); return true; }
        if (args.length == 0) { sendHelp(player); return true; }

        switch (args[0].toLowerCase()) {
            case "class", "c" -> handleClass(player, args);
            case "info", "i"  -> handleInfo(player);
            default           -> sendHelp(player);
        }
        return true;
    }

    private void handleClass(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ColorUtil.color("&c=== Chon Class ==="));
            for (ClassData cd : plugin.getClassManager().getAllClasses()) {
                PlayerClass cur = plugin.getClassManager().getPlayerClass(player.getUniqueId());
                boolean active = cur != null && cur.name().equals(cd.getId());
                player.sendMessage(ColorUtil.color("  &7/combat class &e"
                    + cd.getId().toLowerCase() + " &f- " + cd.getDisplayName()
                    + (active ? " &a[Dang dung]" : "")));
                player.sendMessage(ColorUtil.color("    &8" + cd.getDescription()));
            }
            return;
        }

        try {
            PlayerClass pc = PlayerClass.valueOf(args[1].toUpperCase());
            ClassData cd = plugin.getClassManager().getClassData(pc);
            if (cd == null) { player.sendMessage(ColorUtil.color("&cClass chua duoc config!")); return; }

            ElysiumPlayer ep = CoreAPI.getPlayer(player);
            String oldClass = ep != null ? ep.getPlayerClass() : "NONE";

            // Fire ElysiumClassChangeEvent — cho phep cancel
            ElysiumClassChangeEvent event = new ElysiumClassChangeEvent(player, ep, oldClass, pc.name());
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                player.sendMessage(ColorUtil.color("&cKhong the doi class luc nay!"));
                return;
            }

            // Apply class
            if (ep != null) ep.setPlayerClass(pc.name());
            plugin.getClassManager().setPlayerClass(player.getUniqueId(), pc);
            plugin.getStatsManager().apply(player);
            plugin.getClassManager().giveSkillItems(player);

            // Award achievement lan dau chon class
            CoreAPI.awardAchievement(player, AchievementType.CLASS_CHOSEN);

            player.sendMessage(ColorUtil.color("&a[CLASS] Ban da chon: " + cd.getDisplayName()));
            player.sendMessage(ColorUtil.color("&73 skill item da xuat hien o slot 7-8-9"));
            player.sendMessage(ColorUtil.color("&7Chuot phai vao item de kich hoat skill!"));

        } catch (IllegalArgumentException e) {
            player.sendMessage(ColorUtil.color("&cClass khong ton tai! Xem: /combat class"));
        }
    }

    private void handleInfo(Player player) {
        PlayerClass pc = plugin.getClassManager().getPlayerClass(player.getUniqueId());
        ElysiumPlayer ep = CoreAPI.getPlayer(player);
        ClassData cd = pc != null ? plugin.getClassManager().getClassData(pc) : null;

        player.sendMessage(ColorUtil.color("&c=== Combat Info ==="));
        player.sendMessage(ColorUtil.color("  &7Class: " + (cd != null ? cd.getDisplayName() : "&cChua chon - /combat class")));
        if (ep != null) {
            player.sendMessage(ColorUtil.color("  &7Mana: &b" + ep.getMana() + "/" + ep.getMaxMana()));
            player.sendMessage(ColorUtil.color("  &7Level: &e" + ep.getLevel()));
        }
        if (cd != null) {
            player.sendMessage(ColorUtil.color("  &7Defense: &a" + cd.getDefense() + "%"));
            player.sendMessage(ColorUtil.color("  &7Mana Regen: &b+" + cd.getManaRegen() + "/2s"));
            player.sendMessage(ColorUtil.color("  &8Skill o slot 7-8-9, chuot phai de dung"));
        }
    }

    private void sendHelp(Player player) {
        player.sendMessage(ColorUtil.color("&c=== ElysiumCombat ==="));
        player.sendMessage(ColorUtil.color("  &7/combat class &f- Xem va chon class"));
        player.sendMessage(ColorUtil.color("  &7/combat info &f- Xem thong tin combat"));
    }
}
