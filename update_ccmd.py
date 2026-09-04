import os

def write_file(path, content):
    with open(path, 'w', encoding='utf-8') as f:
        f.write(content)

ccmd = '''package dev.elysium.combat;

import dev.elysium.combat.clazz.ClassData;
import dev.elysium.combat.clazz.PlayerClass;
import dev.elysium.core.api.CoreAPI;
import dev.elysium.core.player.ElysiumPlayer;
import dev.elysium.core.util.ColorUtil;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class CombatCommand implements CommandExecutor {

    private final ElysiumCombat plugin;

    public CombatCommand(ElysiumCombat plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("elysium.combat.reload")) {
                sender.sendMessage(ColorUtil.color("&cKhong co quyen!"));
                return true;
            }
            try {
                plugin.saveResource("classes.yml", true);
                plugin.saveResource("config.yml", true);
                plugin.saveResource("skills.yml", true);
                sender.sendMessage(ColorUtil.color("&e[Hệ Thống] Đã đồng bộ & cập nhật toàn bộ file YML từ lõi Source!"));
            } catch (Exception ex) {}
            plugin.reload();
            sender.sendMessage(ColorUtil.color("&a[ElysiumCombat] Đã reload thành công!"));
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Chi player dung duoc lenh nay!");
            return true;
        }

        if (args.length == 0) {
            handleMenu(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "class", "c", "menu", "m" -> handleMenu(player);
            case "info",  "i"              -> handleInfo(player);
            case "streak"                  -> handleStreak(player);
            case "admin"                   -> handleAdmin(sender);
            default                        -> sendHelp(sender);
        }
        return true;
    }

    private void handleMenu(Player player) {
        new dev.elysium.combat.gui.LifeClassGui(plugin).open(player);
    }

    private void handleInfo(Player player) {
        PlayerClass pc = plugin.getClassManager().getPlayerClass(player.getUniqueId());
        ElysiumPlayer ep = CoreAPI.getPlayer(player);
        ClassData cd = pc != null ? plugin.getClassManager().getClassData(pc) : null;

        player.sendMessage(ColorUtil.color("&c=== Combat Info ==="));
        player.sendMessage(ColorUtil.color("  &7Class: " + (cd != null ? cd.getDisplayName() : "&cChua chon – /combat")));
        if (ep != null) {
            player.sendMessage(ColorUtil.color("  &7Mana: &b" + ep.getMana() + "/" + ep.getMaxMana()));
            player.sendMessage(ColorUtil.color("  &7Level: &e" + ep.getLevel()));
        }
        if (cd != null) {
            player.sendMessage(ColorUtil.color("  &7Defense: &a" + cd.getDefense() + "%"));
            player.sendMessage(ColorUtil.color("  &7Mana Regen: &b+" + cd.getManaRegen() + "/2s"));
        }
        int streak = plugin.getKillNotifyManager().getKillstreak(player.getUniqueId());
        int combo  = plugin.getComboManager().getCombo(player.getUniqueId());
        player.sendMessage(ColorUtil.color("  &7Killstreak: &e" + streak));
        player.sendMessage(ColorUtil.color("  &7Combo hien tai: &f" + combo + "x"));
        player.sendMessage(ColorUtil.color("  &8/combat – mo menu chon class"));
    }

    private void handleStreak(Player player) {
        int streak = plugin.getKillNotifyManager().getKillstreak(player.getUniqueId());
        player.sendMessage(ColorUtil.color("&e⚔ Killstreak cua ban: &f&l" + streak));
    }

    private void handleAdmin(CommandSender sender) {
        if (!sender.hasPermission("elysium.combat.admin")) {
            sender.sendMessage(ColorUtil.color("&cKhong co quyen!"));
            return;
        }
        if (sender instanceof Player p) {
            new dev.elysium.combat.gui.AdminCoreGui(plugin).open(p);
        }
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ColorUtil.color("&c=== ElysiumCombat ==="));
        sender.sendMessage(ColorUtil.color("  &7/combat &f- Mo menu chon nghe nghiep"));
        sender.sendMessage(ColorUtil.color("  &7/combat info &f- Xem thong tin chi tiet"));
        sender.sendMessage(ColorUtil.color("  &7/combat streak &f- Xem killstreak"));
        if (sender.hasPermission("elysium.combat.admin")) {
            sender.sendMessage(ColorUtil.color("  &7/combat reload &f- Reload config"));
            sender.sendMessage(ColorUtil.color("  &7/combat admin &f- Mo kho Loi he phai"));
        }
    }
}
'''
write_file('src/main/java/dev/elysium/combat/CombatCommand.java', ccmd)
print("Updated CombatCommand.java")