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
        super("&8&l» &c&lCHỌN LỚP NHÂN VẬT", 54);
        this.combat = combat;
    }

    @Override
    public void build(Player viewer) {
        fill(new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).name(" ").build());

        PlayerClass current = combat.getClassManager().getPlayerClass(viewer.getUniqueId());
        boolean firstTime   = current == PlayerClass.NONE;

        // ── Header ────────────────────────────────────────────────────────────
        fill(4, new ItemBuilder(Material.NETHER_STAR)
            .name("&e&lHỆ THỐNG CLASS")
            .lore(
                "&7Chọn Class để tối ưu hóa vũ khí của bạn.",
                "&7Mỗi Class sẽ có phong cách chiến đấu riêng biệt.",
                "",
                firstTime ? "&a✔ Lần chọn đầu tiên hoàn toàn MIỄN PHÍ!" : "&7Bạn đang là: " + getDisplayName(current)
            ).build());

        // Lấy vật phẩm đổi class từ config chung
        ItemStack globalCost = combat.getCombatConfig().getChangeClassItem();
        boolean hasGlobalCost = firstTime || hasCost(viewer, globalCost);

        // ── 5 Class (hang 2, slot 11 12 13 14 15) ───────────────────────────────
        int[] classSlots = {11, 12, 13, 14, 15};
        PlayerClass[] classes = {
            PlayerClass.WARRIOR, PlayerClass.MAGE,
            PlayerClass.RANGER,  PlayerClass.ASSASSIN,
            PlayerClass.SUPPORT
        };

        for (int i = 0; i < classes.length; i++) {
            PlayerClass pc = classes[i];
            ClassData cd   = combat.getClassManager().getClassData(pc);
            if (cd == null) continue;

            boolean isActive  = pc == current;

            ItemBuilder builder = new ItemBuilder(Material.valueOf(cd.getItem().toUpperCase()))
                .name((isActive ? "&a&l✔ " : "") + cd.getDisplayName())
                .lore(buildClassLore(cd, isActive, firstTime, hasGlobalCost, globalCost));

            if (isActive) builder.glow();

            final PlayerClass finalPc = pc;
            setButton(classSlots[i], new GuiButton(builder.build(), e -> {
                Player player = (Player) e.getWhoClicked();

                if (finalPc == combat.getClassManager().getPlayerClass(player.getUniqueId())) {
                    player.sendMessage(ColorUtil.color("&cBạn đang sử dụng Class này rồi!"));
                    return;
                }

                boolean free = combat.getClassManager().getPlayerClass(player.getUniqueId()) == PlayerClass.NONE;

                if (!free && !hasCost(player, globalCost)) {
                    player.sendMessage(ColorUtil.color("&cBạn không có đủ &6✦ Huy Hiệu Chức Nghiệp &cđể đổi Class!"));
                    player.closeInventory();
                    return;
                }

                ElysiumPlayer ep = CoreAPI.getPlayer(player);
                String oldClass  = ep != null ? ep.getPlayerClass() : "NONE";

                ElysiumClassChangeEvent event = new ElysiumClassChangeEvent(
                    player, ep, oldClass, finalPc.name());
                Bukkit.getPluginManager().callEvent(event);
                if (event.isCancelled()) {
                    player.sendMessage(ColorUtil.color("&cKhông thể đổi Class lúc này!"));
                    return;
                }

                if (!free) takeCost(player, globalCost);

                if (ep != null) ep.setPlayerClass(finalPc.name());
                combat.getClassManager().setPlayerClass(player.getUniqueId(), finalPc);
                combat.getStatsManager().apply(player);
                combat.getClassManager().giveSkillItems(player);
                CoreAPI.awardAchievement(player, AchievementType.CLASS_CHOSEN);

                player.sendMessage(ColorUtil.color("&aBạn đã trở thành " + cd.getDisplayName() + "&a!"));
                player.closeInventory();
                
                combat.getServer().getScheduler().runTaskLater(combat, () ->
                    CoreAPI.getCore().getGuiManager().open(player, new ClassSelectGui(combat)), 2L);
            }));
        }

        // ── Divider: hang 3 (slot 18-26) ─────────────────────────────────────
        for (int s = 18; s <= 26; s++) {
            fill(s, new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name(" ").build());
        }
        
        fill(22, new ItemBuilder(Material.END_CRYSTAL)
            .name("&6&l✦ THỨC TỈNH &8(Phase 3)")
            .lore(
                "&7Mở khóa sức mạnh tiềm ẩn của Class.",
                "&7Mỗi Class sẽ có một hướng Thức Tỉnh",
                "&7với kỹ năng và hiệu ứng độc quyền.",
                "",
                "&eClick để xem trước tính năng!"
            ).build());
            
        setButton(22, new GuiButton(new ItemBuilder(Material.END_CRYSTAL)
            .name("&6&l✦ THỨC TỈNH &8(Phase 3)")
            .lore(
                "&7Mở khóa sức mạnh tiềm ẩn của Class.",
                "&7Mỗi Class sẽ có một hướng Thức Tỉnh",
                "&7với kỹ năng và hiệu ứng độc quyền.",
                "",
                "&eClick để xem trước tính năng!"
            ).build(), e -> {
                Player player = (Player) e.getWhoClicked();
                CoreAPI.getCore().getGuiManager().open(player, new AwakenGui(combat));
            }));

        // ── 5 Slot Thuc Tinh (hang 4) ────────────────────────────
        int[] awakenSlots  = {29, 30, 31, 32, 33};
        String[] awakenNames = {
            "&c&l⚔ Thức Tỉnh Thần Chiến",
            "&9&l✦ Thức Tỉnh Pháp Thần",
            "&a&l➶ Thức Tỉnh Thần Xạ",
            "&5&l✦ Thức Tỉnh Bóng Tối",
            "&e&l🛡 Thức Tỉnh Hộ Vệ"
        };
        Material[] awakenMats = {
            Material.NETHERITE_SWORD, Material.END_CRYSTAL,
            Material.BOW, Material.PHANTOM_MEMBRANE,
            Material.TOTEM_OF_UNDYING
        };

        for (int i = 0; i < 5; i++) {
            final PlayerClass pc = classes[i];
            boolean isCurrentClass = pc == current;

            ItemBuilder b = new ItemBuilder(awakenMats[i])
                .name(awakenNames[i])
                .lore(
                    "",
                    isCurrentClass ? "&e⚡ Đây là Thức Tỉnh của Class hiện tại" : "&8Đây không phải Class hiện tại của bạn",
                    "&eClick để xem trước tính năng!",
                    "&c🔒 Chưa mở khóa"
                );

            setButton(awakenSlots[i], new GuiButton(b.build(), e -> {
                Player player = (Player) e.getWhoClicked();
                CoreAPI.getCore().getGuiManager().open(player, new AwakenGui(combat));
            }));
        }

        // ── Footer ────────────────────────────────────────────────────────────
        fill(49, new ItemBuilder(Material.PAPER)
            .name("&f&lHướng Dẫn")
            .lore(
                "&7• Click vào biểu tượng Class để chọn.",
                "&7• Lần đầu tiên chọn Class được &aMiễn phí&7.",
                "&7• Các lần đổi sau cần &6✦ Huy Hiệu Chức Nghiệp&7.",
                "&7• Thức Tỉnh sẽ ra mắt trong Phase 3."
            ).build());
    }

    // ── Lore builders ─────────────────────────────────────────────────────────

    private List<String> buildClassLore(ClassData cd, boolean isActive,
                                         boolean firstTime, boolean canAfford,
                                         ItemStack cost) {
        List<String> lore = new ArrayList<>();
        lore.add(ColorUtil.color("&7" + cd.getDescription()));
        lore.add("");
        
        lore.add(ColorUtil.color("&6&l► Vũ Khí Tối Ưu:"));
        for (String w : cd.getOptimizedWeapons()) {
            lore.add(ColorUtil.color("  &8▪ " + w));
        }
        lore.add("");

        if (isActive) {
            lore.add(ColorUtil.color("&a&l✔ BẠN ĐANG SỬ DỤNG CLASS NÀY"));
        } else if (firstTime) {
            lore.add(ColorUtil.color("&a&l✦ MIỄN PHÍ (LẦN ĐẦU)"));
            lore.add(ColorUtil.color("&eClick để chọn!"));
        } else {
            lore.add(ColorUtil.color("&c&l► CHI PHÍ ĐỔI CLASS:"));
            if (cost != null && cost.hasItemMeta() && cost.getItemMeta().hasDisplayName()) {
                lore.add(ColorUtil.color("  &8▪ &f" + cost.getAmount() + "x " + cost.getItemMeta().getDisplayName()));
            } else if (cost != null) {
                lore.add(ColorUtil.color("  &8▪ &f" + cost.getAmount() + "x " + cost.getType().name()));
            }
            lore.add("");
            lore.add(canAfford
                ? ColorUtil.color("&eClick để đổi Class!")
                : ColorUtil.color("&cBạn không đủ vật phẩm!"));
        }
        return lore;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String getDisplayName(PlayerClass pc) {
        ClassData cd = combat.getClassManager().getClassData(pc);
        return cd != null ? cd.getDisplayName() : pc.name();
    }

    private boolean hasCost(Player p, ItemStack cost) {
        if (cost == null) return true;
        int count = 0;
        for (ItemStack s : p.getInventory().getContents()) {
            if (s != null && isChangeItem(s, cost)) {
                count += s.getAmount();
            }
        }
        return count >= cost.getAmount();
    }

    private void takeCost(Player p, ItemStack cost) {
        if (cost == null) return;
        int remaining = cost.getAmount();
        for (int i = 0; i < p.getInventory().getSize() && remaining > 0; i++) {
            ItemStack s = p.getInventory().getItem(i);
            if (s == null || !isChangeItem(s, cost)) continue;
            if (s.getAmount() <= remaining) {
                remaining -= s.getAmount();
                p.getInventory().setItem(i, null);
            } else {
                s.setAmount(s.getAmount() - remaining);
                remaining = 0;
            }
        }
    }
    
    private boolean isChangeItem(ItemStack item, ItemStack required) {
        if (item.getType() != required.getType()) return false;
        if (!required.hasItemMeta()) return true; // If config has no meta, accept any item of this type
        if (!item.hasItemMeta()) return false;
        
        String reqName = required.getItemMeta().getDisplayName();
        String itemName = item.getItemMeta().getDisplayName();
        
        if (reqName != null && itemName != null) {
            return reqName.equals(itemName);
        }
        return false;
    }
}
