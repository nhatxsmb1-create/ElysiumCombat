package dev.elysium.combat;

import dev.elysium.combat.clazz.ClassData;
import dev.elysium.combat.clazz.PlayerClass;
import dev.elysium.combat.gui.ClassSelectGui;
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
            if (sender instanceof Player p) handleMenu(p);
            else sender.sendMessage("Lenh nay chi dung trong game.");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "class", "c", "menu", "m" -> {
                if (sender instanceof Player p) handleMenu(p);
            }
            case "info",  "i"              -> {
                if (sender instanceof Player p) handleInfo(p);
            }
            case "streak"                  -> {
                if (sender instanceof Player p) handleStreak(p);
            }
            case "giveitem", "classitem"   -> handleGiveItem(sender, args);
            default                        -> sendHelp(sender);
        }
        return true;
    }

    private void handleGiveItem(CommandSender sender, String[] args) {
        if (!sender.hasPermission("elysium.combat.admin")) {
            sender.sendMessage(ColorUtil.color("&cKhong co quyen!"));
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(ColorUtil.color("&cSu dung: /combat giveitem <player> [soluong]"));
            return;
        }
        Player target = org.bukkit.Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage(ColorUtil.color("&cNguoi choi khong online!"));
            return;
        }
        int amount = 1;
        if (args.length > 2) {
            try { amount = Integer.parseInt(args[2]); }
            catch (NumberFormatException ignored) {}
        }
        
        org.bukkit.inventory.ItemStack item = plugin.getCombatConfig().getChangeClassItem();
        if (item == null) {
            sender.sendMessage(ColorUtil.color("&cTinh nang doi class dang bi tat trong config."));
            return;
        }
        item.setAmount(amount);
        target.getInventory().addItem(item);
        
        sender.sendMessage(ColorUtil.color("&aDa dua " + amount + " Huy Hieu Chuc Nghiep cho " + target.getName()));
        target.sendMessage(ColorUtil.color("&aBan nhan duoc &e" + amount + "x Huy Hieu Chuc Nghiep&a!"));
    }

    private void handleMenu(Player player) {
        CoreAPI.getCore().getGuiManager().open(player, new ClassSelectGui(plugin));
    }

    private void handleInfo(Player player) {
        PlayerClass pc = plugin.getClassManager().getPlayerClass(player.getUniqueId());
        ElysiumPlayer ep = CoreAPI.getPlayer(player);
        ClassData cd = pc != null ? plugin.getClassManager().getClassData(pc) : null;

        player.sendMessage(ColorUtil.color("&c=== Combat Info ==="));
        player.sendMessage(ColorUtil.color("  &7Class: " + (cd != null ? cd.getDisplayName() : "&cChua chon — /combat")));
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
        player.sendMessage(ColorUtil.color("  &8/combat — mo menu chon class"));
    }

    private void handleStreak(Player player) {
        int streak = plugin.getKillNotifyManager().getKillstreak(player.getUniqueId());
        player.sendMessage(ColorUtil.color("&e⚔ Killstreak cua ban: &f&l" + streak));
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ColorUtil.color("&c=== ElysiumCombat ==="));
        sender.sendMessage(ColorUtil.color("  &7/combat &f- Mo menu chon class"));
        sender.sendMessage(ColorUtil.color("  &7/combat info &f- Xem thong tin chi tiet"));
        sender.sendMessage(ColorUtil.color("  &7/combat streak &f- Xem killstreak"));
        if (sender.hasPermission("elysium.combat.admin")) {
            sender.sendMessage(ColorUtil.color("  &7/combat giveitem <player> [amount] &f- Phat Huy Hieu Chuc Nghiep"));
            sender.sendMessage(ColorUtil.color("  &7/combat reload &f- Reload config"));
        }
    }
}
