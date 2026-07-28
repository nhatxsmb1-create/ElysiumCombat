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
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * GUI chon class.
 * Mo bang: /combat menu
 *
 * Lan dau chon class: MIEN PHI
 * Doi class: ton item theo config trong classes.yml
 */
public class ClassSelectGui extends ElysiumGui {

    private final ElysiumCombat combat;

    public ClassSelectGui(ElysiumCombat combat) {
        super("&c⚔ Chon Class", 27);
        this.combat = combat;
    }

    @Override
    public void build(Player viewer) {
        fill(ItemBuilder.filler());

        PlayerClass current = combat.getClassManager().getPlayerClass(viewer.getUniqueId());
        boolean firstTime   = current == PlayerClass.NONE;

        // Header
        fill(4, new ItemBuilder(Material.BOOK)
            .name("&c⚔ Chon Class")
            .lore(
                firstTime ? "&aBan chua co class — &fLAN DAU MIEN PHI!" : "&7Class hien tai: " + getDisplayName(current, viewer),
                "",
                "&7Chon class phu hop voi phong cach cua ban.",
                "&8Doi class se ton item."
            ).build());

        // 4 class slots: 10, 12, 14, 16
        int[] slots = {10, 12, 14, 16};
        PlayerClass[] classes = {PlayerClass.WARRIOR, PlayerClass.MAGE, PlayerClass.ARCHER, PlayerClass.ROGUE};

        for (int i = 0; i < classes.length; i++) {
            PlayerClass pc  = classes[i];
            ClassData cd    = combat.getClassManager().getClassData(pc);
            if (cd == null) continue;

            boolean isActive = pc == current;
            List<ItemStack> costs = getCosts(pc);
            boolean canAfford     = firstTime || costs.isEmpty() || hasCost(viewer, costs);

            ItemBuilder builder = new ItemBuilder(Material.valueOf(cd.getItem().toUpperCase()))
                .name((isActive ? "&a✔ " : "") + cd.getDisplayName())
                .lore(buildLore(cd, costs, isActive, firstTime, canAfford));

            if (isActive) builder.glow();

            final PlayerClass finalPc = pc;
            setButton(slots[i], new GuiButton(builder.build(), e -> {
                Player player = (Player) e.getWhoClicked();

                // Da chon roi
                if (finalPc == combat.getClassManager().getPlayerClass(player.getUniqueId())) {
                    player.sendMessage(ColorUtil.color("&7Ban dang dung class nay roi!"));
                    return;
                }

                List<ItemStack> changeCosts = getCosts(finalPc);
                boolean isFree = combat.getClassManager().getPlayerClass(player.getUniqueId()) == PlayerClass.NONE;

                // Kiem tra du item
                if (!isFree && !changeCosts.isEmpty() && !hasCost(player, changeCosts)) {
                    player.sendMessage(ColorUtil.color("&cKhong du item doi class!"));
                    player.closeInventory();
                    return;
                }

                // Fire event
                ElysiumPlayer ep = CoreAPI.getPlayer(player);
                String oldClass  = ep != null ? ep.getPlayerClass() : "NONE";
                ElysiumClassChangeEvent event = new ElysiumClassChangeEvent(player, ep, oldClass, finalPc.name());
                Bukkit.getPluginManager().callEvent(event);
                if (event.isCancelled()) {
                    player.sendMessage(ColorUtil.color("&cKhong the doi class luc nay!"));
                    return;
                }

                // Tru item neu khong mien phi
                if (!isFree && !changeCosts.isEmpty()) takeCost(player, changeCosts);

                // Apply class
                if (ep != null) ep.setPlayerClass(finalPc.name());
                combat.getClassManager().setPlayerClass(player.getUniqueId(), finalPc);
                combat.getStatsManager().apply(player);
                combat.getClassManager().giveSkillItems(player);

                CoreAPI.awardAchievement(player, AchievementType.CLASS_CHOSEN);

                ClassData cd2 = combat.getClassManager().getClassData(finalPc);
                player.sendMessage(ColorUtil.color("&a[CLASS] Ban da chon: " + cd2.getDisplayName()));
                player.sendMessage(ColorUtil.color("&73 skill item da xuat hien o slot 7-8-9!"));
                player.closeInventory();

                // Rebuild GUI voi class moi
                combat.getServer().getScheduler().runTaskLater(combat, () ->
                    CoreAPI.getCore().getGuiManager().open(player, new ClassSelectGui(combat)), 2L);
            }));
        }

        // Footer huong dan
        fill(22, new ItemBuilder(Material.PAPER)
            .name("&7Huong dan")
            .lore(
                "&7• Click de chon class",
                firstTime ? "&a• Lan dau: MIEN PHI" : "&7• Doi class: ton item (xem tren)",
                "&7• Skill tu dong cap nhat khi doi class"
            ).build());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private List<String> buildLore(ClassData cd, List<ItemStack> costs,
                                    boolean isActive, boolean firstTime, boolean canAfford) {
        List<String> lore = new ArrayList<>();
        lore.add(ColorUtil.color("&7" + cd.getDescription()));
        lore.add("");
        lore.add(ColorUtil.color("&7HP: &c+" + cd.getBonusHp()
            + "  &7Sat thuong: &c+" + cd.getBonusDamage()));
        lore.add(ColorUtil.color("&7Giam sat thuong: &a" + cd.getDefense() + "%"
            + "  &7Mana: &b+" + cd.getManaRegen() + "/2s"));
        lore.add("");

        if (isActive) {
            lore.add(ColorUtil.color("&a✔ Dang su dung"));
        } else if (firstTime) {
            lore.add(ColorUtil.color("&a✦ Mien phi (lan dau)"));
            lore.add(ColorUtil.color("&eClick de chon!"));
        } else if (costs.isEmpty()) {
            lore.add(ColorUtil.color("&a✦ Mien phi doi class"));
            lore.add(ColorUtil.color("&eClick de chon!"));
        } else {
            lore.add(ColorUtil.color("&7Chi phi doi class:"));
            for (ItemStack cost : costs) {
                lore.add(ColorUtil.color("  &7- &f" + cost.getAmount() + "x " + cost.getType().name()));
            }
            lore.add("");
            lore.add(canAfford
                ? ColorUtil.color("&eClick de chon!")
                : ColorUtil.color("&cKhong du item!"));
        }
        return lore;
    }

    private String getDisplayName(PlayerClass pc, Player p) {
        ClassData cd = combat.getClassManager().getClassData(pc);
        return cd != null ? cd.getDisplayName() : pc.name();
    }

    /** Doc chi phi doi class tu classes.yml */
    private List<ItemStack> getCosts(PlayerClass pc) {
        List<ItemStack> result = new ArrayList<>();
        try {
            File file = new File(combat.getDataFolder(), "classes.yml");
            ConfigurationSection root = YamlConfiguration.loadConfiguration(file)
                .getConfigurationSection("classes." + pc.name() + ".change-cost");
            if (root == null) return result;
            for (String key : root.getKeys(false)) {
                ConfigurationSection c = root.getConfigurationSection(key);
                if (c == null) continue;
                Material mat = Material.matchMaterial(c.getString("material","STONE"));
                int amount   = c.getInt("amount", 1);
                if (mat != null) result.add(new ItemStack(mat, amount));
            }
        } catch (Exception ignored) {}
        return result;
    }

    private boolean hasCost(Player player, List<ItemStack> costs) {
        for (ItemStack cost : costs) {
            int count = 0;
            for (ItemStack slot : player.getInventory().getContents()) {
                if (slot != null && slot.getType() == cost.getType()) count += slot.getAmount();
            }
            if (count < cost.getAmount()) return false;
        }
        return true;
    }

    private void takeCost(Player player, List<ItemStack> costs) {
        for (ItemStack cost : costs) {
            int remaining = cost.getAmount();
            for (int i = 0; i < player.getInventory().getSize() && remaining > 0; i++) {
                ItemStack slot = player.getInventory().getItem(i);
                if (slot == null || slot.getType() != cost.getType()) continue;
                if (slot.getAmount() <= remaining) {
                    remaining -= slot.getAmount();
                    player.getInventory().setItem(i, null);
                } else {
                    slot.setAmount(slot.getAmount() - remaining);
                    remaining = 0;
                }
            }
        }
    }
                  }
