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
            plugin.reload();
            sender.sendMessage(ColorUtil.color("&a[ElysiumCombat] Da reload thanh cong!"));
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Chi player dung duoc lenh nay!");
            return true;
        }

        if (args.length == 0) { handleMenu(player); return true; }

        switch (args[0].toLowerCase()) {
            case "class", "c", "menu", "m" -> handleMenu(player);
            case "info",  "i"              -> handleInfo(player);
            case "streak"                  -> handleStreak(player);
            default                        -> sendHelp(player);
        }
        return true;
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

    private void sendHelp(Player player) {
        player.sendMessage(ColorUtil.color("&c=== ElysiumCombat ==="));
        player.sendMessage(ColorUtil.color("  &7/combat &f- Mo menu chon class"));
        player.sendMessage(ColorUtil.color("  &7/combat info &f- Xem thong tin chi tiet"));
        player.sendMessage(ColorUtil.color("  &7/combat streak &f- Xem killstreak"));
        player.sendMessage(ColorUtil.color("  &7/combat reload &f- Reload config &8(Admin)"));
    }
}
