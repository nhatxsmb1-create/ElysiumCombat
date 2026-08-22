package dev.elysium.combat.gui;

import dev.elysium.combat.ElysiumCombat;
import dev.elysium.combat.clazz.PlayerClass;
import dev.elysium.core.api.CoreAPI;
import dev.elysium.core.gui.ElysiumGui;
import dev.elysium.core.gui.GuiButton;
import dev.elysium.core.gui.ItemBuilder;
import dev.elysium.core.util.ColorUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class AwakenGui extends ElysiumGui {

    private final ElysiumCombat combat;

    public AwakenGui(ElysiumCombat combat) {
        super("&8&l» &5&lTHỨC TỈNH CHỨC NGHIỆP", 27);
        this.combat = combat;
    }

    @Override
    public void build(Player viewer) {
        fill(new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).name(" ").build());

        PlayerClass current = combat.getClassManager().getPlayerClass(viewer.getUniqueId());

        if (current == PlayerClass.NONE) {
            setButton(13, new GuiButton(new ItemBuilder(Material.BARRIER)
                .name("&c&lCHƯA CHỌN CLASS")
                .lore(
                    "&7Bạn cần chọn một Class trước khi",
                    "&7có thể tìm hiểu về Thức Tỉnh."
                ).build(), e -> {
                    viewer.closeInventory();
                    CoreAPI.getCore().getGuiManager().open(viewer, new ClassSelectGui(combat));
                }));
            return;
        }

        // ── Hiển thị Class hiện tại (Slot 11) ─────────────────────────────────
        String className = combat.getClassManager().getClassData(current).getDisplayName();
        Material classMat = Material.valueOf(combat.getClassManager().getClassData(current).getItem().toUpperCase());

        fill(11, new ItemBuilder(classMat)
            .name("&a&l" + className)
            .lore("&7Đang ở trạng thái cơ bản.")
            .glow()
            .build());

        // ── Mũi tên chỉ hướng (Slot 13) ───────────────────────────────────────
        fill(13, new ItemBuilder(Material.CHAIN)
            .name("&c&lĐANG PHONG ẤN")
            .lore(
                "&7Sức mạnh thực sự vẫn đang ngủ say.",
                "&7Yêu cầu: &cLevel 50 &8(Dự kiến)",
                "&7Yêu cầu: &6Mảnh Linh Hồn &8(Dự kiến)"
            ).build());

        // ── Kết quả Thức Tỉnh (Slot 15) ───────────────────────────────────────
        String awakenName = getAwakenName(current);
        fill(15, new ItemBuilder(Material.BEDROCK)
            .name("&5&l" + awakenName)
            .lore(
                "&7Mở khóa bộ Kỹ Năng Tối Thượng",
                "&7và Aura độc quyền của Class.",
                "",
                "&c&l🔒 CHƯA RA MẮT (PHASE 3)"
            ).build());

        // ── Nút Quay Lại (Slot 26) ────────────────────────────────────────────
        setButton(26, new GuiButton(new ItemBuilder(Material.ARROW)
            .name("&cQuay lại")
            .build(), e -> {
                CoreAPI.getCore().getGuiManager().open(viewer, new ClassSelectGui(combat));
            }));
    }

    private String getAwakenName(PlayerClass pc) {
        switch (pc) {
            case WARRIOR: return "Thức Tỉnh: Thần Chiến";
            case MAGE: return "Thức Tỉnh: Pháp Thần";
            case RANGER: return "Thức Tỉnh: Thần Xạ";
            case ASSASSIN: return "Thức Tỉnh: Bóng Tối";
            case SUPPORT: return "Thức Tỉnh: Hộ Vệ";
            default: return "Thức Tỉnh";
        }
    }
}
