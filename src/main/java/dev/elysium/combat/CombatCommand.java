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
        if (!(sender instanceof Player player)) { sender.sendMessage("Chi player dung duoc!"); return true; }
        if (args.length == 0) {
            // Mac dinh mo class GUI luon
            handleMenu(player); return true;
        }

        switch (args[0].toLowerCase()) {
            case "class", "c", "menu", "m" -> handleMenu(player);
            case "info", "i"               -> handleInfo(player);
            default                        -> sendHelp(player);
        }
        return true;
    }

    private void handleMenu(Player player) {
        // Mo Class Selection GUI
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
        player.sendMessage(ColorUtil.color("  &8/combat — mo menu chon class"));
    }

    private void sendHelp(Player player) {
        player.sendMessage(ColorUtil.color("&c=== ElysiumCombat ==="));
        player.sendMessage(ColorUtil.color("  &7/combat &f- Mo menu chon class"));
        player.sendMessage(ColorUtil.color("  &7/combat info &f- Xem thong tin"));
    }
}
