package dev.elysium.combat.gui;

import dev.elysium.combat.ElysiumCombat;
import dev.elysium.combat.clazz.ClassData;
import dev.elysium.combat.clazz.PlayerClass;
import dev.elysium.core.achievement.AchievementType;
import dev.elysium.core.api.CoreAPI;
import dev.elysium.core.event.ElysiumClassChangeEvent;
import dev.elysium.core.gui.ElysiumGui;
import dev.elysium.core.gui.GuiButton;
import dev.elysium.core.gui.ItemBuilder;
import dev.elysium.core.player.ElysiumPlayer;
import dev.elysium.core.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ClassSelectGui extends ElysiumGui {

    private final ElysiumCombat combat;

    public ClassSelectGui(ElysiumCombat combat) {
        super("&c⚔ Chon Class", 54);  // Mo rong len 54 (6 hang) de co hang Thuc Tinh
        this.combat = combat;
    }

    @Override
    public void build(Player viewer) {
        fill(ItemBuilder.filler());

        PlayerClass current = combat.getClassManager().getPlayerClass(viewer.getUniqueId());
        boolean firstTime   = current == PlayerClass.NONE;
        ItemStack cost      = getChangeCost();

        // ── Header ────────────────────────────────────────────────────────────
        fill(4, new ItemBuilder(Material.BOOK)
            .name("&c⚔ Chon Class")
            .lore(
                firstTime ? "&aChua co class — LAN DAU MIEN PHI!" : "&7Dang dung: " + getDisplayName(current),
                "",
                firstTime ? "&7Chon class de bat dau!" : (cost != null
                    ? "&7Doi class ton: &f" + cost.getAmount() + "x " + cost.getType().name()
                    : "&7Doi class mien phi")
            ).build());

        // ── 4 Class (hang 2, slot 10 12 14 16) ───────────────────────────────
        int[] classSlots = {10, 12, 14, 16};
        PlayerClass[] classes = {
            PlayerClass.WARRIOR, PlayerClass.MAGE,
            PlayerClass.ARCHER,  PlayerClass.ROGUE
        };

        for (int i = 0; i < classes.length; i++) {
            PlayerClass pc = classes[i];
            ClassData cd   = combat.getClassManager().getClassData(pc);
            if (cd == null) continue;

            boolean isActive  = pc == current;
            boolean canAfford = firstTime || hasCost(viewer, cost);

            ItemBuilder builder = new ItemBuilder(Material.valueOf(cd.getItem().toUpperCase()))
                .name((isActive ? "&a✔ " : "") + cd.getDisplayName())
                .lore(buildClassLore(cd, isActive, firstTime, canAfford, cost));

            if (isActive) builder.glow();

            final PlayerClass finalPc = pc;
            setButton(classSlots[i], new GuiButton(builder.build(), e -> {
                Player player = (Player) e.getWhoClicked();

                if (finalPc == combat.getClassManager().getPlayerClass(player.getUniqueId())) {
                    player.sendMessage(ColorUtil.color("&7Ban dang dung class nay roi!"));
                    return;
                }

                boolean free       = combat.getClassManager()
                    .getPlayerClass(player.getUniqueId()) == PlayerClass.NONE;
                ItemStack changeCost = getChangeCost();

                if (!free && !hasCost(player, changeCost)) {
                    player.sendMessage(ColorUtil.color("&cCan &f"
                        + (changeCost != null ? changeCost.getAmount() + "x "
                        + changeCost.getType().name() : "?")
                        + " &cde doi class!"));
                    player.closeInventory();
                    return;
                }

                ElysiumPlayer ep = CoreAPI.getPlayer(player);
                String oldClass  = ep != null ? ep.getPlayerClass() : "NONE";

                ElysiumClassChangeEvent event = new ElysiumClassChangeEvent(
                    player, ep, oldClass, finalPc.name());
                Bukkit.getPluginManager().callEvent(event);
                if (event.isCancelled()) {
                    player.sendMessage(ColorUtil.color("&cKhong the doi class luc nay!"));
                    return;
                }

                if (!free) takeCost(player, changeCost);

                if (ep != null) ep.setPlayerClass(finalPc.name());
                combat.getClassManager().setPlayerClass(player.getUniqueId(), finalPc);
                combat.getStatsManager().apply(player);
                combat.getClassManager().giveSkillItems(player);
                CoreAPI.awardAchievement(player, AchievementType.CLASS_CHOSEN);

                ClassData cd2 = combat.getClassManager().getClassData(finalPc);
                player.sendMessage(ColorUtil.color(
                    "&a[CLASS] Da chon: " + cd2.getDisplayName()));
                player.closeInventory();

                combat.getServer().getScheduler().runTaskLater(combat, () ->
                    CoreAPI.getCore().getGuiManager().open(
                        player, new ClassSelectGui(combat)), 2L);
            }));
        }

        // ── Divider: hang 3 (slot 18-26) ─────────────────────────────────────
        for (int s = 18; s <= 26; s++) {
            fill(s, new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE)
                .name("&8&m                    ")
                .build());
        }

        // ── Nhan Thuc Tinh o giua divider ─────────────────────────────────────
        fill(22, new ItemBuilder(Material.NETHER_STAR)
            .name("&6✦ Thuc Tinh &8(Sap ra mat)")
            .lore(
                "&7Mo khoa dang nang cap tiem an cua class.",
                "",
                "&8Tinh nang nay se duoc mo khoa trong tuong lai.",
                "&8Hay tich luy suc manh de chuan bi."
            ).build());

        // ── 4 Slot Thuc Tinh — locked, 1 cho moi class (hang 4-5) ────────────
        int[] awakenSlots  = {28, 31, 34, 37}; // tuong ung voi WARRIOR MAGE ARCHER ROGUE
        String[] awakenNames = {
            "&c⚔ Thuc Tinh Chien Si",
            "&9✦ Thuc Tinh Phap Su",
            "&a➶ Thuc Tinh Xa Thu",
            "&5✦ Thuc Tinh Am Sat"
        };
        Material[] awakenMats = {
            Material.NETHERITE_SWORD,
            Material.END_CRYSTAL,
            Material.BOW,
            Material.PHANTOM_MEMBRANE
        };
        String[][] awakenLore = {
            {
                "&7Chien si bat thanh Than Chien.",
                "&7Nang cap: &cSuc manh thuan tuy, khong co gioi han.",
                "",
                "&7Yeu cau: &8[Chua xac dinh]",
                "&8&oSap ra mat trong Phase 3..."
            },
            {
                "&7Phap su bat thanh Phap Than.",
                "&7Nang cap: &9Mana vo tan, phep thuat huy diet.",
                "",
                "&7Yeu cau: &8[Chua xac dinh]",
                "&8&oSap ra mat trong Phase 3..."
            },
            {
                "&7Xa thu bat thanh Than Xa.",
                "&7Nang cap: &aToc do va chinh xac tuyet doi.",
                "",
                "&7Yeu cau: &8[Chua xac dinh]",
                "&8&oSap ra mat trong Phase 3..."
            },
            {
                "&7Am sat bat thanh Bong Toi.",
                "&7Nang cap: &5An minh hoan hao, mot don chet nguoi.",
                "",
                "&7Yeu cau: &8[Chua xac dinh]",
                "&8&oSap ra mat trong Phase 3..."
            }
        };

        for (int i = 0; i < 4; i++) {
            final PlayerClass pc = classes[i];
            boolean isCurrentClass = pc == current;

            ItemBuilder b = new ItemBuilder(awakenMats[i])
                .name(awakenNames[i])
                .lore(buildAwakenLore(awakenLore[i], isCurrentClass));

            // Khong glow, khong click — chi la preview
            setButton(awakenSlots[i], new GuiButton(b.build(), e -> {
                Player player = (Player) e.getWhoClicked();
                player.sendMessage(ColorUtil.color(
                    "&6[Thuc Tinh] &7Tinh nang nay chua duoc mo khoa. Hay cho Phase 3!"));
            }));
        }

        // ── Footer ────────────────────────────────────────────────────────────
        fill(49, new ItemBuilder(Material.PAPER)
            .name("&7Huong dan")
            .lore(
                "&7• Click de chon class",
                firstTime ? "&a• Lan dau: MIEN PHI" :
                    (cost != null ? "&7• Can: &f" + cost.getAmount()
                        + "x " + cost.getType().name() : "&7• Mien phi"),
                "&7• Thuc Tinh se mo khoa trong tuong lai"
            ).build());
    }

    // ── Lore builders ─────────────────────────────────────────────────────────

    private List<String> buildClassLore(ClassData cd, boolean isActive,
                                         boolean firstTime, boolean canAfford,
                                         ItemStack cost) {
        List<String> lore = new ArrayList<>();
        lore.add(ColorUtil.color("&7" + cd.getDescription()));
        lore.add("");
        lore.add(ColorUtil.color("&7HP: &c+" + cd.getBonusHp()
            + "  &7Damage: &c+" + cd.getBonusDamage()));
        lore.add(ColorUtil.color("&7Defense: &a" + cd.getDefense()
            + "%  &7Mana: &b+" + cd.getManaRegen() + "/2s"));
        lore.add("");
        if (isActive) {
            lore.add(ColorUtil.color("&a✔ Dang su dung"));
        } else if (firstTime || cost == null) {
            lore.add(ColorUtil.color("&a✦ Mien phi (lan dau)"));
            lore.add(ColorUtil.color("&eClick de chon!"));
        } else {
            lore.add(ColorUtil.color("&7Can: &f"
                + cost.getAmount() + "x " + cost.getType().name()));
            lore.add(canAfford
                ? ColorUtil.color("&eClick de chon!")
                : ColorUtil.color("&cKhong du item!"));
        }
        return lore;
    }

    private List<String> buildAwakenLore(String[] lines, boolean isCurrentClass) {
        List<String> lore = new ArrayList<>();
        for (String line : lines) lore.add(ColorUtil.color(line));
        lore.add("");
        lore.add(isCurrentClass
            ? ColorUtil.color("&e⚡ Day la Thuc Tinh cua class ban dang dung")
            : ColorUtil.color("&8Khong phai class hien tai cua ban"));
        lore.add(ColorUtil.color("&c🔒 Chua mo khoa"));
        return lore;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String getDisplayName(PlayerClass pc) {
        ClassData cd = combat.getClassManager().getClassData(pc);
        return cd != null ? cd.getDisplayName() : pc.name();
    }

    private ItemStack getChangeCost() {
        var cfg = combat.getConfig();
        if (!cfg.getBoolean("class-change.enabled", true)) return null;
        String mat = cfg.getString("class-change.item.material", "NETHER_STAR");
        int amt    = cfg.getInt("class-change.item.amount", 1);
        Material m = Material.matchMaterial(mat);
        return m != null ? new ItemStack(m, amt) : null;
    }

    private boolean hasCost(Player p, ItemStack cost) {
        if (cost == null) return true;
        int count = 0;
        for (ItemStack s : p.getInventory().getContents())
            if (s != null && s.getType() == cost.getType()) count += s.getAmount();
        return count >= cost.getAmount();
    }

    private void takeCost(Player p, ItemStack cost) {
        if (cost == null) return;
        int remaining = cost.getAmount();
        for (int i = 0; i < p.getInventory().getSize() && remaining > 0; i++) {
            ItemStack s = p.getInventory().getItem(i);
            if (s == null || s.getType() != cost.getType()) continue;
            if (s.getAmount() <= remaining) {
                remaining -= s.getAmount();
                p.getInventory().setItem(i, null);
            } else {
                s.setAmount(s.getAmount() - remaining);
                remaining = 0;
            }
        }
    }
}
