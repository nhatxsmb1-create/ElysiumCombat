package dev.elysium.combat;

import dev.elysium.combat.clazz.ClassData;
import dev.elysium.combat.clazz.PlayerClass;
import dev.elysium.combat.skill.Skill;
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
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Chi player dung duoc!"); return true;
        }
        if (args.length == 0) { sendHelp(player); return true; }

        switch (args[0].toLowerCase()) {
            case "class", "c"   -> handleClass(player, args);
            case "info", "i"    -> handleInfo(player);
            case "skill", "s"   -> handleSkill(player, args);
            default             -> sendHelp(player);
        }
        return true;
    }

    // ── Class ─────────────────────────────────────────────────────────────────

    private void handleClass(Player player, String[] args) {
        if (args.length < 2) {
            // Hien thi danh sach class
            player.sendMessage(ColorUtil.color("&c=== Chon Class ==="));
            for (ClassData cd : plugin.getClassManager().getAllClasses()) {
                PlayerClass current = plugin.getClassManager().getPlayerClass(player.getUniqueId());
                boolean isCurrent = current != null && current.name().equals(cd.getId());
                String marker = isCurrent ? " &a[Dang dung]" : "";
                player.sendMessage(ColorUtil.color("  &7/combat class &e" + cd.getId().toLowerCase()
                    + " &f- " + cd.getDisplayName() + marker));
                player.sendMessage(ColorUtil.color("    &8" + cd.getDescription()));
            }
            return;
        }

        String className = args[1].toUpperCase();
        PlayerClass pc;
        try { pc = PlayerClass.valueOf(className); }
        catch (IllegalArgumentException e) {
            player.sendMessage(ColorUtil.color("&cClass khong ton tai! Xem: /combat class"));
            return;
        }

        ClassData cd = plugin.getClassManager().getClassData(pc);
        if (cd == null) { player.sendMessage(ColorUtil.color("&cClass chua duoc config!")); return; }

        // Set class
        ElysiumPlayer ep = CoreAPI.getPlayer(player);
        if (ep != null) ep.setPlayerClass(pc.name());
        plugin.getClassManager().setPlayerClass(player.getUniqueId(), pc);
        plugin.getStatsManager().apply(player);

        player.sendMessage(ColorUtil.color("&a[CLASS] Ban da chon: " + cd.getDisplayName()));
        player.sendMessage(ColorUtil.color("&7HP: &c+" + cd.getBonusHp()
            + " &7| Dam: &c+" + cd.getBonusDamage()
            + " &7| Giam ST: &c" + cd.getDefense() + "%"
            + " &7| Mana regen: &b+" + cd.getManaRegen()));
    }

    // ── Info ──────────────────────────────────────────────────────────────────

    private void handleInfo(Player player) {
        PlayerClass pc = plugin.getClassManager().getPlayerClass(player.getUniqueId());
        ElysiumPlayer ep = CoreAPI.getPlayer(player);

        player.sendMessage(ColorUtil.color("&c=== Combat Info ==="));
        if (pc == null || pc == PlayerClass.NONE) {
            player.sendMessage(ColorUtil.color("  &7Class: &cChua chon - /combat class"));
        } else {
            ClassData cd = plugin.getClassManager().getClassData(pc);
            player.sendMessage(ColorUtil.color("  &7Class: " + (cd != null ? cd.getDisplayName() : pc.name())));
        }
        if (ep != null) {
            player.sendMessage(ColorUtil.color("  &7Mana: &b" + ep.getMana() + "/" + ep.getMaxMana()));
            player.sendMessage(ColorUtil.color("  &7Level: &e" + ep.getLevel()));
        }
        player.sendMessage(ColorUtil.color("  &7HP: &c" + String.format("%.1f", player.getHealth())
            + "/" + String.format("%.1f", player.getMaxHealth())));
        player.sendMessage(ColorUtil.color("  &8Kich hoat skill: &7F (skill 1) | /combat skill 2 | /combat skill 3"));
    }

    // ── Skill ─────────────────────────────────────────────────────────────────

    private void handleSkill(Player player, String[] args) {
        if (args.length < 2) {
            PlayerClass pc = plugin.getClassManager().getPlayerClass(player.getUniqueId());
            if (pc == null || pc == PlayerClass.NONE) {
                player.sendMessage(ColorUtil.color("&cHay chon class truoc: /combat class"));
                return;
            }
            player.sendMessage(ColorUtil.color("&c=== Skills (" + pc.name() + ") ==="));
            for (int i = 1; i <= 3; i++) {
                Skill s = plugin.getSkillManager().getSkill(pc, i);
                if (s == null) continue;
                long cd = CoreAPI.getCore().getCooldownManager().remainingSeconds(player.getUniqueId(), pc + "_skill" + i);
                String cdStr = cd > 0 ? " &8[CD: " + cd + "s]" : " &a[San sang]";
                player.sendMessage(ColorUtil.color("  &7Skill " + i + ": &f" + s.getName()
                    + " &8| Mana: &b" + s.getManaCost() + cdStr));
                player.sendMessage(ColorUtil.color("    &8" + s.getDescription()));
            }
            player.sendMessage(ColorUtil.color("  &8/combat skill <1|2|3> de kich hoat"));
            return;
        }

        try {
            int slot = Integer.parseInt(args[1]);
            if (slot < 1 || slot > 3) throw new NumberFormatException();
            plugin.getSkillManager().activate(player, slot);
        } catch (NumberFormatException e) {
            player.sendMessage(ColorUtil.color("&cDung: /combat skill <1|2|3>"));
        }
    }

    private void sendHelp(Player player) {
        player.sendMessage(ColorUtil.color("&c=== ElysiumCombat ==="));
        player.sendMessage(ColorUtil.color("  &7/combat class &f- Xem va chon class"));
        player.sendMessage(ColorUtil.color("  &7/combat info &f- Xem thong tin combat"));
        player.sendMessage(ColorUtil.color("  &7/combat skill &f- Xem va dung skill"));
    }
              }
