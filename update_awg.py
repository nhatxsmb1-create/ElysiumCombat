import os

def write_file(path, content):
    with open(path, 'w', encoding='utf-8') as f:
        f.write(content)

awg = '''package dev.elysium.combat.gui;

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
                .name("&c&lCHƯA LẮP LÕI")
                .lore(
                    "&7Bạn cần phải lắp Lõi Kỹ Năng",
                    "&7thì mới có thể Thức Tỉnh được."
                ).build(), e -> {
                    viewer.closeInventory();
                }));
            return;
        }

        // ── Hiển thị Class hiện tại (Slot 11) ─────────────────────────────────
        String className = combat.getClassManager().getClassData(current).getDisplayName();
        Material classMat = Material.valueOf(combat.getClassManager().getClassData(current).getItem().toUpperCase());
        if (classMat == null) classMat = Material.NETHER_STAR;

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
                "&7và Aura độc quyền của Hệ phái.",
                "",
                "&c&l🔒 CHƯA RA MẮT (PHASE 3)"
            ).build());

        // ── Nút Đóng (Slot 26) ────────────────────────────────────────────
        setButton(26, new GuiButton(new ItemBuilder(Material.BARRIER)
            .name("&cĐóng")
            .build(), e -> {
                viewer.closeInventory();
            }));
    }

    private String getAwakenName(PlayerClass pc) {
        switch (pc) {
            case WARRIOR: return "Thức Tỉnh: Lõi Cuồng Chiến";
            case MAGE: return "Thức Tỉnh: Lõi Pháp Thần";
            case RANGER: return "Thức Tỉnh: Lõi Phong Thần";
            case ASSASSIN: return "Thức Tỉnh: Lõi Sát Nhân";
            case SUPPORT: return "Thức Tỉnh: Lõi Hộ Vệ";
            default: return "Thức Tỉnh Lõi Kỹ Năng";
        }
    }
}
'''
write_file('src/main/java/dev/elysium/combat/gui/AwakenGui.java', awg)
print("Updated AwakenGui.java")